package ch.simlane.tme.components;

import ch.simlane.Simlane;
import ch.simlane.editor.Editor;
import ch.simlane.editor.Map;
import ch.simlane.editor.Tile;
import ch.simlane.tme.Model;
import ch.simlane.tme.Simulation;
import ch.simlane.tme.SimulationException;
import ch.simlane.tme.state.LaneState;
import ch.simlane.tme.state.SimulationState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static ch.simlane.editor.Tile.TileLocation;
import static ch.simlane.tme.components.Connector.ConnectorLocation;

public class IntersectionTest {

    private static Editor editor;
    private static Simulation simulation;

    public static void main(String[] args) {
        // instantiate application
        Simlane simlane = new Simlane();
        // modify editor model to represent an intersection
        editor = simlane.getEditor();
        Map map = editor.getMap();
        Tile tile = map.getTile(2, 2);
        // select the lanes
        tile.setLaneState(Editor.SIDE_TOP, Editor.SIDE_LEFT, Tile.LANE_STATE_SELECTED);
        tile.setLaneState(Editor.SIDE_TOP, Editor.SIDE_RIGHT, Tile.LANE_STATE_SELECTED);
        tile.setLaneState(Editor.SIDE_TOP, Editor.SIDE_BOTTOM, Tile.LANE_STATE_SELECTED);
        tile.setLaneState(Editor.SIDE_LEFT, Editor.SIDE_RIGHT, Tile.LANE_STATE_SELECTED);
        tile.setLaneState(Editor.SIDE_RIGHT, Editor.SIDE_LEFT, Tile.LANE_STATE_SELECTED);
        tile.setLaneState(Editor.SIDE_BOTTOM, Editor.SIDE_TOP, Tile.LANE_STATE_SELECTED);
        tile.setLaneState(Editor.SIDE_BOTTOM, Editor.SIDE_RIGHT, Tile.LANE_STATE_SELECTED);
        // create the model for the tme
        Model model = new Model();
        // connectors
        TileLocation location = tile.getLocation();
        Connector fromTop = new Connector(location.getLocationOn(Editor.SIDE_TOP), location);
        Connector fromLeft = new Connector(location.getLocationOn(Editor.SIDE_LEFT), location);
        Connector fromRight = new Connector(location.getLocationOn(Editor.SIDE_RIGHT), location);
        Connector fromBottom = new Connector(location.getLocationOn(Editor.SIDE_BOTTOM), location);
        Connector toTop = new Connector(location, location.getLocationOn(Editor.SIDE_TOP));
        Connector toLeft = new Connector(location, location.getLocationOn(Editor.SIDE_LEFT));
        Connector toRight = new Connector(location, location.getLocationOn(Editor.SIDE_RIGHT));
        Connector toBottom = new Connector(location, location.getLocationOn(Editor.SIDE_BOTTOM));
        // lane groups
        LaneGroup top = new LaneGroup(fromTop);
        LaneGroup left = new LaneGroup(fromLeft);
        LaneGroup right = new LaneGroup(fromRight);
        LaneGroup bottom = new LaneGroup(fromBottom);
        // lanes
        Lane topToLeft = new Lane(100, top, toLeft);
        model.addLane(topToLeft);
        Lane topToRight = new Lane(100, top, toRight);
        model.addLane(topToRight);
        Lane topToBottom = new Lane(100, top, toBottom);
        model.addLane(topToBottom);
        Lane leftToRight = new Lane(100, left, toRight);
        model.addLane(leftToRight);
        Lane rightToLeft = new Lane(100, right, toLeft);
        model.addLane(rightToLeft);
        Lane bottomToTop = new Lane(100, bottom, toTop);
        model.addLane(bottomToTop);
        Lane bottomToRight = new Lane(100, bottom, toRight);
        model.addLane(bottomToRight);
        // intersecting lane groups
        LaneGroup.setIntersecting(top, left);
        LaneGroup.setIntersecting(top, right);
        LaneGroup.setIntersecting(top, bottom);
        LaneGroup.setIntersecting(left, bottom);
        LaneGroup.setIntersecting(right, bottom);
        // intersection
        LaneGroup[] groups = new LaneGroup[]{top, left, right, bottom};
        Intersection intersection = new Intersection(new LinkedList<>(Arrays.asList(groups)));
        model.addIntersection(intersection);
        // create simulation
        simulation = new Simulation(model);
        // run simulation (without using the tme)
        tile.setTrafficLightsEnabled(true);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture future = scheduler.scheduleAtFixedRate(IntersectionTest::updateSimulation,
                0, 50, TimeUnit.MILLISECONDS);
    }

    private static void updateSimulation() {
        try {
            simulation.nextStep(50);
        } catch (SimulationException e) {
            e.printStackTrace();
        }
        // get the simulation state
        SimulationState state = simulation.getState();
        // sort the lanes (according to the tile they are on)
        HashMap<Tile, List<LaneState>> lanes = new HashMap<>();
        for (LaneState lane : state.getLanes()) {
            ConnectorLocation start = lane.getStart();
            TileLocation location = (TileLocation) start.getRefOut();
            Tile tile = editor.getMap().getTile(location);
            lanes.computeIfAbsent(tile, k -> new LinkedList<>()).add(lane);
        }
        // set the traffic lights for the lanes on each tile
        for (Tile tile : lanes.keySet()) {
            TrafficLight[][] trafficLights = new TrafficLight[4][4];
            for (LaneState lane : lanes.get(tile)) {
                ConnectorLocation start = lane.getStart();
                ConnectorLocation end = lane.getEnd();
                TileLocation location = tile.getLocation();
                int sideFrom = location.getSideOf((TileLocation) start.getRefIn());
                int sideTo = location.getSideOf((TileLocation) end.getRefOut());
                int indexFrom = Editor.getArrayIndexFromSide(sideFrom);
                int indexTo = Editor.getArrayIndexFromSide(sideTo);
                trafficLights[indexFrom][indexTo] = lane.getState();
            }
            tile.setTrafficLights(trafficLights);
        }
    }
}
