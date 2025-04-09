package ch.simlane;

import ch.simlane.editor.CarType;
import ch.simlane.editor.Editor;
import ch.simlane.editor.Map;
import ch.simlane.editor.Tile;
import ch.simlane.editor.scenario.Scenario;
import ch.simlane.editor.scenario.ScenarioPoint;
import ch.simlane.editor.scenario.StartPoint;
import ch.simlane.tme.Engine;
import ch.simlane.tme.Model;
import ch.simlane.tme.components.*;

import java.util.*;

import static ch.simlane.editor.Tile.LANE_STATE_SELECTED;
import static ch.simlane.editor.Tile.TileLocation;
import static ch.simlane.tme.components.Connector.ConnectorLocation;

public class ModelLoader implements Runnable {

    private static final int INDEX_FROM = 0;
    private static final int INDEX_TO = 1;

    private Map map;
    private Scenario scenario;

    private Model model;
    private Engine tme;

    private HashMap<ConnectorLocation, Connector> unconnected;
    private HashMap<ScenarioPoint, Connector> scenarioPointConnectors;

    public ModelLoader(Map map, Scenario scenario, Engine tme) {
        this.map = map;
        this.scenario = scenario;
        this.tme = tme;
    }

    @Override
    public void run() {
        try {
            createModel();
        } catch (ModelLoaderException e) {
            if (Simlane.DEBUG) {
                e.printStackTrace();
            }
            model = null;
        }
        tme.loadModel(model);
    }

    public void createModel() throws ModelLoaderException {
        model = new Model();
        createScenarioPointConnectors();
        createLaneAndConnectorGraph();
        createCars();
    }

    private void createScenarioPointConnectors() {
        scenarioPointConnectors = new HashMap<>();
        for (ScenarioPoint scenarioPoint : scenario.getScenarioPoints()) {
            Connector connector = createScenarioPointConnector(scenarioPoint);
            scenarioPointConnectors.put(scenarioPoint, connector);
        }
    }

    private Connector createScenarioPointConnector(ScenarioPoint scenarioPoint) {
        int side = scenarioPoint.getSide();
        TileLocation scenarioPointLocation = map.getScenarioPointLocation(scenarioPoint);
        TileLocation adjacentMapLocation = scenarioPointLocation.getLocationOn(Editor.getOppositeSide(side));
        TileLocation refIn;
        TileLocation refOut;
        int type;
        if (scenarioPoint.getType() == ScenarioPoint.TYPE_START) {
            refIn = scenarioPointLocation;
            refOut = adjacentMapLocation;
            type = Connector.CONNECTOR_TYPE_START;
        } else {
            refIn = adjacentMapLocation;
            refOut = scenarioPointLocation;
            type = Connector.CONNECTOR_TYPE_END;
        }
        return new Connector(refIn, refOut, type);
    }

    private void createLaneAndConnectorGraph() throws ModelLoaderException {
        unconnected = new HashMap<>();
        for (Connector connector : scenarioPointConnectors.values()) {
            unconnected.put(connector.getLocation(), connector);
        }
        traverseMap();
        assert unconnected.isEmpty();
    }

    private void traverseMap() throws ModelLoaderException {
        for (int row = 0; row < map.getNumRows(); row++) {
            for (int col = 0; col < map.getNumCols(); col++) {
                processTile(map.getTile(row, col));
            }
        }
    }

    private void processTile(Tile tile) throws ModelLoaderException {
        // create temporary connectors
        Connector[][] connectors = new Connector[4][2];
        TileLocation tileLocation = tile.getLocation();
        for (int i = 0; i < 4; i++) {
            int side = Editor.getSideFromArrayIndex(i);
            TileLocation adjacentTileLocation = tileLocation.getLocationOn(side);
            connectors[i][INDEX_FROM] = new Connector(adjacentTileLocation, tileLocation);
            connectors[i][INDEX_TO] = new Connector(tileLocation, adjacentTileLocation);
        }
        // create lanes
        LaneGroup[] laneGroups = new LaneGroup[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tile.getLaneStateByIndex(i, j) == LANE_STATE_SELECTED) {
                    if (laneGroups[i] == null) {
                        laneGroups[i] = new LaneGroup(connectors[i][INDEX_FROM]);
                    }
                    double l = tile.getLaneLengthByIndex(i, j);
                    model.addLane(new Lane(l, laneGroups[i], connectors[j][INDEX_TO]));
                }
            }
        }
        // determine intersecting lane groups
        boolean intersecting = false;
        for (int i = 0; i < 4; i++) {
            intersecting:
            for (int j = i + 1; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    if (tile.getLaneStateByIndex(i, k) == LANE_STATE_SELECTED) {
                        for (int l = 0; l < 4; l++) {
                            if (tile.getLaneStateByIndex(j, l) == LANE_STATE_SELECTED) {
                                if (tile.lanesIntersectByIndex(i, k, j, l)) {
                                    intersecting = true;
                                    LaneGroup.setIntersecting(laneGroups[i], laneGroups[j]);
                                    continue intersecting;
                                }
                            }
                        }
                    }
                }
            }
        }
        // create intersection
        if (intersecting) {
            List<LaneGroup> groups = new LinkedList<>(Arrays.asList(laneGroups));
            groups.removeAll(Collections.singleton(null));
            model.addIntersection(new Intersection(groups));
        }
        // connect connectors
        for (int i = 0; i < 4; i++) {
            if (connectSideOfTile(Editor.getSideFromArrayIndex(i), tile)) {
                for (int j = 0; j < 2; j++) {
                    Connector t = connectors[i][j];
                    Connector c = unconnected.remove(t.getLocation());
                    if (t.isConnectable()) {
                        if (c != null) {
                            c.connectWith(t);
                        } else {
                            String side = Editor.convertToString(Editor.getSideFromArrayIndex(i));
                            throw new ModelLoaderException("No matching unconnected connector found when processing side " +
                                    side + " of " + tile);
                        }
                    } else {
                        if (c != null) {
                            String side = Editor.convertToString(Editor.getSideFromArrayIndex(i));
                            throw new ModelLoaderException("Unmatched unconnected connector found when processing side " +
                                    side + " of " + tile);
                        }
                    }
                }
            } else {
                for (int j = 0; j < 2; j++) {
                    Connector t = connectors[i][j];
                    if (t.isConnectable()) {
                        unconnected.put(t.getLocation(), t);
                    }
                }
            }
        }
    }

    private boolean connectSideOfTile(int side, Tile tile) {
        if (side == Editor.SIDE_TOP || side == Editor.SIDE_LEFT) {
            return true;
        }
        if (side == Editor.SIDE_RIGHT && tile.getCol() == map.getNumCols() - 1) {
            return true;
        }
        if (side == Editor.SIDE_BOTTOM && tile.getRow() == map.getNumRows() - 1) {
            return true;
        }
        return false;
    }

    private void createCars() {
        for (StartPoint startPoint : scenario.getStartPoints()) {
            for (CarType carType : startPoint.getInboundTraffic().keySet()) {
                int count = startPoint.getInboundTraffic().get(carType);
                for (int i = 0; i < count; i++) {
                    Car car = new Car(carType);
                    car.setStartConnector(scenarioPointConnectors.get(startPoint));
                    car.setEndConnector(scenarioPointConnectors.get(scenario.getEndPoint(carType)));
                    model.addCar(car);
                }
            }
        }
    }

    private static class ModelLoaderException extends Exception {

        ModelLoaderException(String message) {
            super(message);
        }
    }
}
