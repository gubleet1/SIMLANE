package ch.simlane.tme.components;

import ch.simlane.tme.SimulationException;

import java.util.LinkedList;
import java.util.List;

public class Lane {

    private static final double MIN_LANE_LENGTH = 10;

    private LaneGroup laneGroup;

    private Connector end;

    // the length of the lane in meters
    private double l;

    // a list of the cars that are located on this lane (used for lookahead)
    private List<Car> cars;

    public Lane(double l, LaneGroup laneGroup, Connector end) {
        if (l < MIN_LANE_LENGTH) {
            throw new IllegalStateException("The specified length is below the minimum required length.");
        }
        this.l = l;
        this.laneGroup = laneGroup;
        this.end = end;
        cars = new LinkedList<>();
        end.addIn(this);
        laneGroup.addLane(this);
    }

    public void reset() {
        cars.clear();
    }

    public Connector getStart() {
        return laneGroup.getStart();
    }

    public Connector getEnd() {
        return end;
    }

    void setEnd(Connector end) {
        this.end = end;
    }

    public TrafficLight getState() {
        return laneGroup.getState();
    }

    public boolean isBlocked() {
        return laneGroup.isBlocked();
    }

    public boolean isEmpty() {
        return cars.isEmpty();
    }

    public double getLength() {
        return l;
    }

    public double availableDistanceFromCar(Car car) {
        int index = cars.indexOf(car);
        if (index == -1) {
            return availableDistanceFromStart();
        } else {
            if (index + 1 < cars.size()) {
                double nextCarPos = cars.get(index + 1).getPos();
                return (nextCarPos - car.getPos()) * l;
            }
            return (1 - car.getPos()) * l;
        }
    }

    public double availableDistanceFromStart() {
        if (isEmpty()) {
            return l;
        }
        return cars.get(0).getPos() * l;
    }

    public double distanceFromCarToEnd(Car car) {
        int index = cars.indexOf(car);
        if (index == -1) {
            return l;
        } else {
            return (1 - car.getPos()) * l;
        }
    }

    public boolean isLastCar(Car car) {
        int index = cars.indexOf(car);
        if (index == -1) {
            throw new IllegalStateException("The specified car is not on this lane.");
        }
        return index == cars.size() - 1;
    }

    public Car getFirstCar() {
        if (isEmpty()) {
            throw new IllegalStateException("This lane does not contain any cars.");
        }
        return cars.get(0);
    }

    public Car getCarAhead(Car car) {
        if (isLastCar(car)) {
            throw new IllegalStateException("The specified car is the last car on this lane.");
        }
        return cars.get(cars.indexOf(car) + 1);
    }

    public void add(Car car) throws SimulationException {
        if (getState() == TrafficLight.RED) {
            throw new SimulationException("Red traffic light violated.");
        }
        car.setPos(0);
        cars.add(0, car);
    }

    public void remove(Car car) {
        if (!cars.contains(car)) {
            throw new IllegalStateException("The specified car is not on this lane.");
        }
        if (car.getPos() != 1) {
            throw new IllegalStateException("The specified car has not yet reached the end of this lane.");
        }
        cars.remove(car);
    }

    public double move(Car car, double dx) {
        int index = cars.indexOf(car);
        if (index == -1) {
            throw new IllegalStateException("The specified car is not on this lane.");
        }
        double pos = car.getPos();
        double dxMax = (1 - pos) * l;
        if (dx >= dxMax) {
            pos = 1;
            dx -= dxMax;
        } else {
            pos += dx / l;
            dx = 0;
        }
        if (pos > 1) {
            pos = 1;
        }
        car.setPos(pos);
        return dx;
    }

    public void validate() throws SimulationException {
        if (isEmpty()) {
            return;
        }
        double pos = Double.NEGATIVE_INFINITY;
        for (Car car : cars) {
            if (pos >= car.getPos()) {
                throw new SimulationException("Inconsistent lane state detected.");
            }
            pos = car.getPos();
        }
    }
}
