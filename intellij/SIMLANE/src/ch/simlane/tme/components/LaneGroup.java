package ch.simlane.tme.components;

import java.util.LinkedList;
import java.util.List;

public class LaneGroup {

    private Intersection intersection;
    private List<LaneGroup> intersectingGroups;
    private int priority;

    private TrafficLight state;
    private boolean blocked;
    private List<Lane> lanes;

    private Connector start;

    public LaneGroup(Connector start) {
        this.start = start;
        state = TrafficLight.GREEN;
        lanes = new LinkedList<>();
        intersectingGroups = new LinkedList<>();
        start.setOut(this);
    }

    public static void setIntersecting(LaneGroup group1, LaneGroup group2) {
        group1.addIntersectingGroup(group2);
        group2.addIntersectingGroup(group1);
    }

    public void reset() {
        state = TrafficLight.GREEN;
        blocked = false;
        priority = 0;
    }

    public Connector getStart() {
        return start;
    }

    void setStart(Connector start) {
        this.start = start;
    }

    public List<Lane> getLanes() {
        return lanes;
    }

    void addLane(Lane lane) {
        if (lanes.contains(lane)) {
            return;
        }
        lanes.add(lane);
    }

    public TrafficLight getState() {
        return state;
    }

    public void setState(TrafficLight state) {
        this.state = state;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isBlocked(boolean update) {
        if (update) {
            blocked = false;
            if (state != TrafficLight.RED) {
                for (LaneGroup group : intersectingGroups) {
                    if (group.containsCars()) {
                        blocked = true;
                        break;
                    }
                }
            }
        }
        return blocked;
    }

    private boolean containsCars() {
        for (Lane lane : lanes) {
            if (!lane.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    void setIntersection(Intersection intersection) {
        this.intersection = intersection;
    }

    boolean intersectsWith(LaneGroup group) {
        return intersectingGroups.contains(group);
    }

    private void addIntersectingGroup(LaneGroup group) {
        intersectingGroups.add(group);
    }

    int getPriority() {
        return priority;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }
}
