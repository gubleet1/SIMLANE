package ch.simlane.editor.scenario;

import ch.simlane.editor.CarType;

public class EndPoint extends ScenarioPoint {

    private CarType carType;

    public EndPoint(int side, int pos) {
        super(TYPE_END, side, pos);
    }

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }
}
