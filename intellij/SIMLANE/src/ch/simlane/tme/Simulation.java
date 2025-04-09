package ch.simlane.tme;

import ch.simlane.tme.components.Car;
import ch.simlane.tme.components.Intersection;
import ch.simlane.tme.components.Lane;
import ch.simlane.tme.state.SimulationState;

public class Simulation {

    private Model model;

    private SimulationState state;

    private boolean finished;
    private long t; // milliseconds

    public Simulation(Model model) {
        this.model = model;
        initialize();
    }

    private void initialize() {
        model.getIntersections().forEach(Intersection::initialize);
        try {
            nextStep(0);
        } catch (SimulationException e) {
            throw new IllegalStateException("Simulation initialization failed.");
        }
    }

    public long getTime() {
        return t;
    }

    public void nextStep(long dt) throws SimulationException {
        t += dt;
        updateIntersections(dt);
        updateCars(dt);
        validate();
        updateState();
        for (Car car : model.getCars()) {
            if (car.getState() != Car.CAR_STATE_ARRIVED) {
                return;
            }
        }
        finished = true;
    }

    private void updateIntersections(long dt) {
        model.getIntersections().forEach(intersection -> intersection.update(dt));
    }

    private void updateCars(long dt) throws SimulationException {
        for (Car car : model.getCars()) {
            car.update(dt);
        }
    }

    private void validate() throws SimulationException {
        for (Car car : model.getCars()) {
            car.validate();
        }
        for (Lane lane : model.getLanes()) {
            lane.validate();
        }
    }

    private void updateState() {
        state = new SimulationState(this, model.getCars(), model.getLanes());
    }

    public boolean hasNextStep() {
        return !finished;
    }

    public SimulationState getState() {
        return state;
    }
}
