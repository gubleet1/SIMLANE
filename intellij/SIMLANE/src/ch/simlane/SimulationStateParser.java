package ch.simlane;

import ch.simlane.editor.CarIndicator;
import ch.simlane.editor.CarType;
import ch.simlane.editor.Editor;
import ch.simlane.editor.Tile;
import ch.simlane.tme.components.TrafficLight;
import ch.simlane.tme.state.CarState;
import ch.simlane.tme.state.LaneState;
import ch.simlane.tme.state.SimulationState;
import ch.simlane.ui.SimlaneUI;

import java.awt.geom.Point2D;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static ch.simlane.editor.Tile.TileLocation;
import static ch.simlane.tme.components.Connector.ConnectorLocation;

public class SimulationStateParser {

    private static final int REF_IN = 0;
    private static final int REF_OUT = 1;

    private static final int LANE_SIDE_FROM = 0;
    private static final int LANE_SIDE_TO = 1;

    private Editor editor;

    public SimulationStateParser(Editor editor) {
        this.editor = editor;
    }

    public void parse(SimulationState simulationState) {
        parseLaneState(simulationState.getLanes());
        parseCarState(simulationState.getCars());
        parseMetaData(simulationState);
    }

    private void parseLaneState(List<LaneState> lanes) {
        HashMap<Tile, List<LaneState>> tileLanes = new HashMap<>();
        for (LaneState lane : lanes) {
            Tile tile = getTile(lane);
            tileLanes.computeIfAbsent(tile, k -> new LinkedList<>()).add(lane);
        }
        for (Tile tile : tileLanes.keySet()) {
            TrafficLight[][] trafficLights = new TrafficLight[4][4];
            for (LaneState lane : tileLanes.get(tile)) {
                int sideFromIndex = Editor.getArrayIndexFromSide(getSide(lane, LANE_SIDE_FROM));
                int sideToIndex = Editor.getArrayIndexFromSide(getSide(lane, LANE_SIDE_TO));
                trafficLights[sideFromIndex][sideToIndex] = lane.getState();
            }
            tile.setTrafficLights(trafficLights);
        }
    }

    private Tile getTile(LaneState lane) {
        return editor.getMap().getTile(getTileLocation(lane));
    }

    private TileLocation getTileLocation(LaneState lane) {
        return getTileLocation(lane, LANE_SIDE_FROM, REF_OUT);
    }

    private TileLocation getTileLocation(LaneState lane, int side, int ref) {
        switch (side) {
            case LANE_SIDE_FROM:
                return getTileLocation(lane.getStart(), ref);
            case LANE_SIDE_TO:
                return getTileLocation(lane.getEnd(), ref);
            default:
                throw new IllegalStateException("Invalid lane side value.");
        }
    }

    private TileLocation getTileLocation(ConnectorLocation connectorLocation, int ref) {
        switch (ref) {
            case REF_IN:
                return (TileLocation) connectorLocation.getRefIn();
            case REF_OUT:
                return (TileLocation) connectorLocation.getRefOut();
            default:
                throw new IllegalStateException("Invalid ref value.");
        }
    }

    private int getSide(LaneState lane, int side) {
        TileLocation tileLocation = getTileLocation(lane);
        int ref;
        switch (side) {
            case LANE_SIDE_FROM:
                ref = REF_IN;
                break;
            case LANE_SIDE_TO:
                ref = REF_OUT;
                break;
            default:
                throw new IllegalStateException("Invalid lane side value.");
        }
        TileLocation adjacentTileLocation = getTileLocation(lane, side, ref);
        return tileLocation.getSideOf(adjacentTileLocation);
    }

    private void parseCarState(List<CarState> cars) {
        List<CarIndicator> traffic = new LinkedList<>();
        for (CarState car : cars) {
            traffic.add(getCarIndicator(car));
        }
        editor.getTraffic().setTraffic(traffic);
    }

    private CarIndicator getCarIndicator(CarState car) {
        Point2D.Double point = getPoint(car);
        CarType carType = getCarType(car);
        return new CarIndicator(point.x, point.y, carType);
    }

    private Point2D.Double getPoint(CarState car) {
        Tile tile = getTile(car.getLane());
        Point2D.Double point = getPointOnTile(car);
        point.x += tile.getCol();
        point.y += tile.getRow();
        return point;
    }

    private Point2D.Double getPointOnTile(CarState car) {
        LaneState lane = car.getLane();
        double pos = car.getPos();
        return getPointOnLane(lane, pos);
    }

    private Point2D.Double getPointOnLane(LaneState lane, double pos) {
        int sideFrom = getSide(lane, LANE_SIDE_FROM);
        int sideTo = getSide(lane, LANE_SIDE_TO);
        switch (Editor.getLaneType(sideFrom, sideTo)) {
            case Editor.LANE_TYPE_STRAIGHT:
                return getPointOnStraightLane(lane, pos);
            case Editor.LANE_TYPE_LEFT_TURN:
            case Editor.LANE_TYPE_RIGHT_TURN:
                return getPointOnCurvedLane(lane, pos);
            case Editor.LANE_TYPE_U_TURN:
                throw new UnsupportedOperationException("Lane type U-Turn is not yet supported.");
            default:
                throw new IllegalStateException("Invalid lane type value.");
        }
    }

    private Point2D.Double getPointOnStraightLane(LaneState lane, double pos) {
        Point2D.Double point = new Point2D.Double();
        int sideFrom = getSide(lane, LANE_SIDE_FROM);
        switch (sideFrom) {
            case Editor.SIDE_TOP:
                point.x = SimlaneUI.LANE_SPACING;
                point.y = pos;
                break;
            case Editor.SIDE_LEFT:
                point.x = pos;
                point.y = 1 - SimlaneUI.LANE_SPACING;
                break;
            case Editor.SIDE_RIGHT:
                point.x = 1 - pos;
                point.y = SimlaneUI.LANE_SPACING;
                break;
            case Editor.SIDE_BOTTOM:
                point.x = 1 - SimlaneUI.LANE_SPACING;
                point.y = 1 - pos;
                break;
            default:
                throw new IllegalArgumentException(sideFrom + " is not a valid side.");
        }
        return point;
    }

    private Point2D.Double getPointOnCurvedLane(LaneState lane, double pos) {
        Point2D.Double point = new Point2D.Double();
        int sideFrom = getSide(lane, LANE_SIDE_FROM);
        int sideTo = getSide(lane, LANE_SIDE_TO);
        double radius;
        double radian;
        switch (Editor.getLaneType(sideFrom, sideTo)) {
            case Editor.LANE_TYPE_LEFT_TURN:
                radius = 1 - SimlaneUI.LANE_SPACING;
                radian = (Math.PI / 2) * (1 - pos);
                break;
            case Editor.LANE_TYPE_RIGHT_TURN:
                radius = SimlaneUI.LANE_SPACING;
                radian = (Math.PI / 2) * pos;
                break;
            default:
                throw new IllegalStateException("Lane type is not a curved lane.");
        }
        double sin = Math.sin(radian) * radius;
        double cos = Math.cos(radian) * radius;
        switch (Editor.getCornerBetween(sideFrom, sideTo)) {
            case Editor.CORNER_TOP_LEFT:
                point.x = cos;
                point.y = sin;
                break;
            case Editor.CORNER_TOP_RIGHT:
                point.x = 1 - sin;
                point.y = cos;
                break;
            case Editor.CORNER_BOTTOM_LEFT:
                point.x = sin;
                point.y = 1 - cos;
                break;
            case Editor.CORNER_BOTTOM_RIGHT:
                point.x = 1 - cos;
                point.y = 1 - sin;
                break;
            default:
                throw new IllegalArgumentException("Invalid corner value.");
        }
        return point;
    }

    private CarType getCarType(CarState car) {
        return (CarType) car.getRef();
    }

    private void parseMetaData(SimulationState simulationState) {
        // parse simulation time
        Duration time = Duration.ofMillis(simulationState.getTime());
        editor.getTools().getSimulationControls().setTime(time);
    }
}
