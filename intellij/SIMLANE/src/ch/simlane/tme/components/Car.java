package ch.simlane.tme.components;

import ch.simlane.tme.Engine;
import ch.simlane.tme.SimulationException;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static ch.simlane.tme.components.TrafficLight.*;

public class Car {

    public static final int CAR_STATE_NEW = 4000;
    public static final int CAR_STATE_ACTIVE = 4001;
    public static final int CAR_STATE_ARRIVED = 4002;

    private static final double CC0 = Engine.MIN_CAR_DISTANCE; // standstill distance - m
    private static final double CC1 = 1.2; // spacing time - s
    private static final double CC2 = 8.0; // following variation - m
    private static final double CC3 = -4.0; // threshold for entering 'following' - s
    private static final double CC4 = -1.5; // negative 'following' threshold - m/s
    private static final double CC5 = 2.1; // positive 'following' threshold - m/s
    private static final double CC6 = 6.0e-4; // speed dependency of oscillation - rad/s
    private static final double CC7 = 0.25; // oscillation acceleration - m/s^2
    private static final double CC8 = 2.0; // standstill acceleration - m/s^2
    private static final double CC9 = 1.5; // acceleration at 80 km/h - m/s^2

    private static Random random;

    /*
     * A reference to an arbitrary object that represents the car outside the tme.
     * In the case of SIMLANE this is simply a CarType as we don't need to distinguish
     * individual cars.
     */
    private Object ref;

    private Connector startConnector;
    private Connector endConnector;
    private List<Connector> path;
    private int pathIndex;

    private int state;

    // the lane on which this car is located
    private Lane lane;
    // the position on the lane between 0 and 1
    private double pos;
    // the current speed in m/s
    private double v;
    // the current acceleration in m^2/s
    private double a;
    // the lane ahead on which an orange traffic light is detected
    private Lane orangeLightLane;
    // the decision made (red or green) for the detected orange traffic light
    private TrafficLight orangeLightInterpretation;

    // the distance in m moved in the last update
    private double dx;
    // the distance in m to the car ahead
    private double dxCarAhead;
    // the car ahead as determined by the previous look ahead
    private Car carAhead;

    public Car(Object ref) {
        this.ref = ref;
        state = CAR_STATE_NEW;
    }

    private static double random() {
        if (random == null) {
            random = new Random();
        }
        return random.nextDouble();
    }

    public void reset() {
        state = CAR_STATE_NEW;
        pathIndex = 0;
        lane = null;
        pos = 0;
        v = 0;
        a = 0;
        orangeLightLane = null;
        orangeLightInterpretation = null;
        dx = 0;
        dxCarAhead = 0;
        carAhead = null;
    }

    public Connector getStartConnector() {
        return startConnector;
    }

    public void setStartConnector(Connector startConnector) {
        this.startConnector = startConnector;
    }

    public Connector getEndConnector() {
        return endConnector;
    }

    public void setEndConnector(Connector endConnector) {
        this.endConnector = endConnector;
    }

    public void setPath(List<Connector> path) {
        if (path.size() < 2) {
            throw new IllegalStateException("The specified path contains less than two entries.");
        }
        if (path.get(0) != startConnector || path.get(path.size() - 1) != endConnector) {
            throw new IllegalStateException("The specified path does not match the start and end points");
        }
        this.path = path;
    }

    public int getState() {
        return state;
    }

    public Lane getLane() {
        return lane;
    }

    public double getPos() {
        return pos;
    }

    public void setPos(double pos) {
        this.pos = pos;
    }

    public Object getRef() {
        return ref;
    }

    public void update(long dt) throws SimulationException {
        if (state == CAR_STATE_NEW) {
            double dx = Engine.LOOK_AHEAD_CUTOFF;
            List<Obstacle> obstacles = lookAhead();
            for (Obstacle o : obstacles) {
                if (o.type == Obstacle.OBSTACLE_TYPE_CAR) {
                    o.dx -= Engine.MIN_CAR_DISTANCE;
                }
                dx = Math.min(dx, o.dx);
            }
            if (dx >= 0) {
                lane = getLaneInPath(pathIndex);
                lane.add(this);
                state = CAR_STATE_ACTIVE;
            }
        } else if (state == CAR_STATE_ACTIVE) {
            if (endOfPathReached()) {
                lane.remove(this);
                pathIndex++;
                lane = null;
                pos = 0;
                dx = 0;
                state = CAR_STATE_ARRIVED;
            } else {
                if (dt <= 0) {
                    return;
                }
                List<Obstacle> obstacles = lookAhead();
                calculateAcceleration(obstacles);
                move(dt);
            }
        }
    }

    private void move(long dt) throws SimulationException {
        v += a * (dt / 1000.0);
        v = Math.max(v, 0);
        double dx = v * (dt / 1000.0);
        this.dx = dx;
        while (dx > 0) {
            dx = lane.move(this, dx);
            if (endOfPathReached()) {
                this.dx -= dx;
                return;
            }
            if (dx > 0) {
                lane.remove(this);
                pathIndex++;
                lane = getLaneInPath(pathIndex);
                lane.add(this);
                if (lane == orangeLightLane) {
                    orangeLightLane = null;
                    orangeLightInterpretation = null;
                }
            }
        }
    }

    public void validate() throws SimulationException {
        if (state != CAR_STATE_ACTIVE) {
            return;
        }
        if (carAhead == null || carAhead.getState() == CAR_STATE_ARRIVED) {
            return;
        }
        if (dxCarAhead - (dx - carAhead.dx) <= 0) {
            throw new SimulationException("Car crash detected.");
        }
    }

    private List<Obstacle> lookAhead() {
        this.carAhead = null;
        this.dxCarAhead = 0;
        List<Obstacle> obstacles = new LinkedList<>();
        Obstacle carAhead;
        Obstacle redLightAhead = null;
        double dx;
        double cutoffShift = 0;
        Lane lane = this.lane;
        boolean isCurrentLane = true;
        if (state == CAR_STATE_NEW) {
            lane = getLaneInPath(pathIndex);
            isCurrentLane = false;
        }
        dx = -Engine.CAR_LENGTH;
        carAhead = lookAheadForCar(lane, dx, isCurrentLane);
        if (!isCurrentLane) {
            redLightAhead = lookAheadForRedLight(lane, dx);
        }
        dx += lane.distanceFromCarToEnd(this);
        for (int i = pathIndex + 1; pathContainsLane(i); i++) {
            if (carAhead != null) {
                cutoffShift = Engine.CAR_LENGTH * 0.5;
            }
            if (dx + cutoffShift > Engine.LOOK_AHEAD_CUTOFF) {
                break;
            }
            lane = getLaneInPath(i);
            if (carAhead == null) {
                carAhead = lookAheadForCar(lane, dx, false);
            }
            if (redLightAhead == null) {
                redLightAhead = lookAheadForRedLight(lane, dx);
            }
            if (carAhead != null && redLightAhead != null) {
                break;
            }
            dx += lane.getLength();
        }
        if (carAhead != null) {
            obstacles.add(carAhead);
        }
        if (redLightAhead != null) {
            obstacles.add(redLightAhead);
        }
        for (Obstacle o : obstacles) {
            if (o.dx > Engine.LOOK_AHEAD_CUTOFF) {
                obstacles.remove(o);
            }
        }
        if (obstacles.isEmpty()) {
            obstacles.add(new Obstacle(Engine.LOOK_AHEAD_CUTOFF, Obstacle.OBSTACLE_TYPE_UNKNOWN));
        }
        return obstacles;
    }

    private Obstacle lookAheadForCar(Lane lane, double dx, boolean isCurrentLane) {
        Car carAhead;
        if (isCurrentLane) {
            if (lane.isLastCar(this)) {
                return null;
            }
            carAhead = lane.getCarAhead(this);
            dx += lane.availableDistanceFromCar(this);
        } else {
            if (lane.isEmpty()) {
                return null;
            }
            carAhead = lane.getFirstCar();
            dx += lane.availableDistanceFromStart();
        }
        this.carAhead = carAhead;
        this.dxCarAhead = dx;
        return new Obstacle(dx, carAhead.v, carAhead.a);
    }

    private Obstacle lookAheadForRedLight(Lane lane, double dx) {
        dx += Engine.CAR_LENGTH * 0.5;
        TrafficLight light = interpretTrafficLight(lane, dx);
        if (light == RED) {
            return new Obstacle(dx, Obstacle.OBSTACLE_TYPE_RED_TRAFFIC_LIGHT);
        }
        return null;
    }

    private TrafficLight interpretTrafficLight(Lane lane, double dx) {
        TrafficLight light = lane.getState();
        if (lane.isBlocked()) {
            light = RED;
        }
        if (orangeLightLane != null && (light != ORANGE || lane != orangeLightLane)) {
            orangeLightLane = null;
            orangeLightInterpretation = null;
        }
        if (light == ORANGE) {
            orangeLightLane = lane;
            if (orangeLightInterpretation != RED) {
                orangeLightInterpretation = interpretOrangeLight(dx);
            }
            light = orangeLightInterpretation;
        }
        return light;
    }

    private TrafficLight interpretOrangeLight(double dx) {
        if (v <= 0) {
            return RED;
        }
        if (dx <= 0) {
            return GREEN;
        }
        double dxMin = (-v * v * 0.5) / Engine.MAX_COMFORTABLE_DECELERATION;
        return dxMin < dx ? RED : GREEN;
    }

    private void calculateAcceleration(List<Obstacle> obstacles) {
        double a = Double.MAX_VALUE;
        for (Obstacle obstacle : obstacles) {
            a = Math.min(a, calculateAcceleration(obstacle));
        }
        this.a = a;
    }

    private double calculateAcceleration(Obstacle obstacle) {
        double a = 0;
        double dx = obstacle.dx;
        double dv = obstacle.v - v;
        // calculate thresholds
        double sdxc, sdxo;
        double sdxv;
        double sdvc, sdvo;
        if (obstacle.type == Obstacle.OBSTACLE_TYPE_CAR) {
            if (obstacle.v == 0) {
                sdxc = CC0;
            } else {
                sdxc = CC0 + CC1 * ((dv >= 0 || obstacle.a < -1) ? v : obstacle.v + dv * (random() - 0.5));
            }
            sdxo = sdxc + CC2;
            double sdv = CC6 * dx * dx;
            sdvc = (obstacle.v > 0) ? CC4 - sdv : 0;
            sdvo = (v > CC5) ? sdv + CC5 : sdv;
        } else {
            sdxc = 0;
            sdxo = 0;
            sdvc = 0;
            sdvo = 0;
        }
        sdxv = sdxo + CC3 * (dv - CC4);
        // determine driving mode
        if (dv < sdvo && dx <= sdxc) {
            // decelerate - increase distance
            if (v > 0) {
                if (dv < 0) {
                    if (dx > CC0) {
                        a = Math.min(obstacle.a + dv * dv / (CC0 - dx), this.a);
                    } else {
                        a = Math.min(obstacle.a + 0.5 * (dv - sdvo), this.a);
                    }
                }
                if (a > -CC7) {
                    a = -CC7;
                } else {
                    a = Math.max(a, -10 + 0.5 * Math.sqrt(v));
                }
            }
        } else if (dv < sdvc && dx < sdxv) {
            // decelerate - decrease distance
            a = Math.max(0.5 * dv * dv / (-dx + sdxc - 0.1), -10 + 0.5 * Math.sqrt(v));
        } else if (dv < sdvo && dx < sdxo) {
            // follow
            if (this.a <= 0) {
                a = Math.min(this.a, -CC7);
            } else {
                a = Math.max(this.a, CC7);
                a = Math.min(a, Engine.SPEED_LIMIT - v);
            }
        } else {
            // accelerate
            if (dx > sdxc) {
                a = CC8 + CC9 * Math.min(v, 22) + random();
                if (dx < sdxo) {
                    a = Math.min(dv * dv / (sdxo - dx), a);
                }
                a = Math.min(a, Engine.SPEED_LIMIT - v);
            }
        }
        return a;
    }

    private boolean endOfPathReached() {
        return lane == getLastLaneInPath() && pos == 1;
    }

    private boolean pathContainsLane(int index) {
        return index + 1 < path.size();
    }

    private Lane getLaneInPath(int index) {
        return path.get(index).getLaneTo(path.get(index + 1));
    }

    private Lane getLastLaneInPath() {
        int index = path.size() - 2;
        return getLaneInPath(index);
    }

    private static class Obstacle {

        static final int OBSTACLE_TYPE_CAR = 7000;
        static final int OBSTACLE_TYPE_RED_TRAFFIC_LIGHT = 7001;
        static final int OBSTACLE_TYPE_UNKNOWN = 7002;

        private int type;

        private double dx;
        private double v;
        private double a;

        Obstacle(double dx, double v, double a) {
            this.dx = dx;
            this.v = v;
            this.a = a;
            type = OBSTACLE_TYPE_CAR;
        }

        Obstacle(double dx, int type) {
            this.dx = dx;
            this.v = 0;
            this.a = 0;
            this.type = type;
        }
    }
}
