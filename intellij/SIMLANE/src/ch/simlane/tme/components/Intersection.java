package ch.simlane.tme.components;

import java.util.*;

public class Intersection {

    private TrafficController controller;
    private List<LaneGroup> blocked;

    private List<LaneGroup> laneGroups;
    private List<LaneGroup> initialOrder;

    public Intersection(List<LaneGroup> laneGroups) {
        blocked = new LinkedList<>();
        laneGroups.forEach(group -> group.setIntersection(this));
        this.laneGroups = laneGroups;
        initialOrder = new LinkedList<>(laneGroups);
    }

    public void initialize() {
        laneGroups.forEach(group -> group.setState(TrafficLight.RED));
        controller = new TrafficController();
        updateBlocked();
    }

    public void reset() {
        controller = null;
        blocked.clear();
        for (LaneGroup group : laneGroups) {
            group.reset();
        }
        laneGroups = initialOrder;
    }

    public void update(long dt) {
        controller.update(dt);
        updateBlocked();
    }

    private void updateBlocked() {
        for (LaneGroup group : blocked) {
            if (!group.isBlocked(true)) {
                blocked.remove(group);
            }
        }
    }

    private class TrafficController {

        private static final int PHASE_ACTIVE = 7000;
        private static final int PHASE_TRAP = 7001;
        private static final int PHASE_ALL_RED = 7002;

        private static final double TIME_PHASE_ACTIVE = 10; // s
        private static final double TIME_PHASE_TRAP = 3; // s
        private static final double TIME_PHASE_ALL_RED = 2; // s

        private Set<LaneGroup> green;
        private Set<LaneGroup> orange;
        private Set<LaneGroup> red;

        private List<LaneGroup> nextActive;

        private int phase;

        private double tPhase; // s
        private long t; // milliseconds

        private TrafficController() {
            green = new HashSet<>();
            orange = new HashSet<>();
            red = new HashSet<>(laneGroups);
            nextActive = getNextActive();
            switchToActive();
        }

        private void update(long dt) {
            t += dt;
            if ((t / 1000.0) >= tPhase) {
                t = 0;
                switch (phase) {
                    case PHASE_ACTIVE:
                        switchToTrap();
                        break;
                    case PHASE_TRAP:
                        switchToAllRed();
                        break;
                    case PHASE_ALL_RED:
                        switchToActive();
                        break;
                }
            }
        }

        private void switchToActive() {
            phase = PHASE_ACTIVE;
            tPhase = TIME_PHASE_ACTIVE;
            Set<LaneGroup> groups = new HashSet<>(nextActive);
            groups.removeAll(green);
            red.removeAll(groups);
            green.addAll(groups);
            nextActive = getNextActive();
            // change state
            groups.forEach(group -> group.setState(TrafficLight.GREEN));
            // potentially blocked lanes
            blocked.addAll(groups);
        }

        private void switchToTrap() {
            phase = PHASE_TRAP;
            tPhase = TIME_PHASE_TRAP;
            Set<LaneGroup> groups = new HashSet<>(green);
            groups.removeAll(nextActive);
            green.removeAll(groups);
            orange.addAll(groups);
            // change state
            groups.forEach(group -> group.setState(TrafficLight.ORANGE));
        }

        private void switchToAllRed() {
            phase = PHASE_ALL_RED;
            tPhase = TIME_PHASE_ALL_RED;
            Set<LaneGroup> groups = new HashSet<>(orange);
            orange.removeAll(groups);
            red.addAll(groups);
            // change state
            groups.forEach(group -> group.setState(TrafficLight.RED));
        }

        private List<LaneGroup> getNextActive() {
            List<LaneGroup> nextActive = new LinkedList<>();
            boolean decPriority = true;
            for (LaneGroup candidate : laneGroups) {
                boolean add = true;
                int priority = candidate.getPriority();
                for (LaneGroup group : nextActive) {
                    if (candidate.intersectsWith(group)) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    if (priority < laneGroups.size()) {
                        priority++;
                        candidate.setPriority(priority);
                    }
                    nextActive.add(candidate);
                }
                if (priority == 0) {
                    decPriority = false;
                }
            }
            if (decPriority) {
                for (LaneGroup group : laneGroups) {
                    int priority = group.getPriority();
                    priority--;
                    group.setPriority(priority);
                }
            }
            LaneGroup group = laneGroups.remove(0);
            laneGroups.add(laneGroups.size(), group);
            laneGroups.sort(Comparator.comparingInt(LaneGroup::getPriority));
            return nextActive;
        }
    }
}
