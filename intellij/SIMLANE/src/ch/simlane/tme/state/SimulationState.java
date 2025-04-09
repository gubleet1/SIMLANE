package ch.simlane.tme.state;

import ch.simlane.tme.Simulation;
import ch.simlane.tme.components.Car;
import ch.simlane.tme.components.Lane;

import java.util.LinkedList;
import java.util.List;

public class SimulationState {

    private List<CarState> cars;
    private List<LaneState> lanes;

    private long t; // milliseconds

    public SimulationState(Simulation simulation, List<Car> cars, List<Lane> lanes) {
        this.cars = new LinkedList<>();
        this.lanes = new LinkedList<>();
        t = simulation.getTime();
        initialize(cars, lanes);
    }

    private void initialize(List<Car> cars, List<Lane> lanes) {
        for (Car car : cars) {
            if (car.getState() == Car.CAR_STATE_ACTIVE) {
                this.cars.add(new CarState(car));
            }
        }
        for (Lane lane : lanes) {
            this.lanes.add(new LaneState(lane));
        }
    }

    public List<CarState> getCars() {
        return cars;
    }

    public List<LaneState> getLanes() {
        return lanes;
    }

    public long getTime() {
        return t;
    }
}
