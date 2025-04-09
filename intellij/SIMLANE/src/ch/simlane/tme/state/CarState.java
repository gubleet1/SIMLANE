package ch.simlane.tme.state;

import ch.simlane.tme.components.Car;

public class CarState {

    private Object ref;
    private LaneState lane;
    private double pos;

    public CarState(Car car) {
        ref = car.getRef();
        lane = new LaneState(car.getLane());
        pos = car.getPos();
    }

    public Object getRef() {
        return ref;
    }

    public LaneState getLane() {
        return lane;
    }

    public double getPos() {
        return pos;
    }
}
