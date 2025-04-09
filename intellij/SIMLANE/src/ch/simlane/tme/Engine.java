package ch.simlane.tme;

import ch.simlane.tme.state.SimulationState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for handling the simulation of traffic.
 * Handles simulation exceptions and emits events related to the simulation.
 */
public class Engine {

    public static final boolean DEBUG = true;

    public static final double SPEED_LIMIT = 33; // m/s
    public static final double MIN_CAR_DISTANCE = 1.3; // m
    public static final double CAR_LENGTH = 5.0; // m
    public static final double MAX_COMFORTABLE_DECELERATION = -6.0; // m/s^2
    public static final double LOOK_AHEAD_CUTOFF = 250; // m

    public static final int MODEL_VALIDATION_FAILED_EVENT = 12000;
    public static final int MODEL_VALIDATION_SUCCESSFUL_EVENT = 12001;
    public static final int SIMULATION_READY_EVENT = 12002;
    public static final int SIMULATION_STARTED_EVENT = 12003;
    public static final int SIMULATION_PAUSED_EVENT = 12004;
    public static final int SIMULATION_FINISHED_EVENT = 12005;
    public static final int SIMULATION_FAILED_EVENT = 12006;
    public static final int SIMULATION_STATE_CHANGED_EVENT = 12007;

    private static final int SIMULATION_RESOLUTION = 20; // updates / second
    private static final int SIMULATION_PERIOD = 1000 / SIMULATION_RESOLUTION; // milliseconds / update

    private Model model;
    private Simulation simulation;

    private ScheduledExecutorService scheduler;
    private boolean running;
    private boolean finished;
    private boolean failed;

    private ExecutorService eventExecutor;
    private List<TMEListener> tmeListeners;

    public Engine() {
        eventExecutor = Executors.newSingleThreadExecutor();
        tmeListeners = new LinkedList<>();
    }

    public synchronized void loadModel(Model model) {
        if (running) {
            throw new IllegalStateException("The simulation must be stopped to load a new model.");
        }
        if (scheduler != null) {
            awaitTermination();
        }
        if (model != null && model.validate()) {
            this.model = model;
            emitEvent(new TMEEvent(MODEL_VALIDATION_SUCCESSFUL_EVENT));
        } else {
            emitEvent(new TMEEvent(MODEL_VALIDATION_FAILED_EVENT));
            return;
        }
        simulation = new Simulation(model);
        emitEvent(new TMEEvent(SIMULATION_STATE_CHANGED_EVENT));
        emitEvent(new TMEEvent(SIMULATION_READY_EVENT));
    }

    public synchronized void startSimulation() {
        if (model == null) {
            throw new IllegalStateException("Model is null.");
        }
        if (running) {
            return;
        }
        if (finished || failed) {
            return;
        }
        if (scheduler != null) {
            awaitTermination();
        }
        running = true;
        emitEvent(new TMEEvent(SIMULATION_STARTED_EVENT));
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new SimulationUpdate(), 0, SIMULATION_PERIOD, TimeUnit.MILLISECONDS);
    }

    public void pauseSimulation() {
        if (model == null) {
            throw new IllegalStateException("Model is null.");
        }
        if (stopSimulation()) {
            awaitTermination();
            if (failed) {
                return;
            }
            if (!finished) {
                emitEvent(new TMEEvent(SIMULATION_PAUSED_EVENT));
            }
        }
    }

    private synchronized boolean stopSimulation() {
        if (!running) {
            return false;
        }
        scheduler.shutdown();
        running = false;
        return true;
    }

    public synchronized void resetSimulation() {
        if (model == null) {
            throw new IllegalStateException("Model is null.");
        }
        if (running) {
            return;
        }
        if (scheduler != null) {
            awaitTermination();
        }
        finished = false;
        failed = false;
        model.reset();
        simulation = new Simulation(model);
        emitEvent(new TMEEvent(SIMULATION_STATE_CHANGED_EVENT));
        emitEvent(new TMEEvent(SIMULATION_READY_EVENT));
    }

    public synchronized SimulationState getSimulationState() {
        if (model == null) {
            throw new IllegalStateException("Model is null.");
        }
        return simulation.getState();
    }

    public synchronized void reset() {
        if (running) {
            throw new IllegalStateException("The simulation must be stopped to reset the TME.");
        }
        if (scheduler != null) {
            awaitTermination();
        }
        model = null;
        simulation = null;
        scheduler = null;
        running = false;
        finished = false;
        failed = false;
    }

    public void addTMEListener(TMEListener listener) {
        if (!tmeListeners.contains(listener)) {
            tmeListeners.add(listener);
        }
    }

    private void emitEvent(TMEEvent event) {
        for (TMEListener listener : tmeListeners) {
            eventExecutor.execute(() -> listener.tmeEvent(event));
        }
    }

    private void awaitTermination() {
        try {
            scheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Exception during simulation shutdown.");
        }
    }

    private class SimulationUpdate implements Runnable {

        @Override
        public void run() {
            if (simulation.hasNextStep()) {
                try {
                    simulation.nextStep(SIMULATION_PERIOD);
                } catch (SimulationException e) {
                    if (Engine.DEBUG) {
                        e.printStackTrace();
                    }
                    failed = true;
                    stopSimulation();
                    emitEvent(new TMEEvent(SIMULATION_FAILED_EVENT));
                    return;
                }
                emitEvent(new TMEEvent(SIMULATION_STATE_CHANGED_EVENT));
            } else {
                finished = true;
                stopSimulation();
                emitEvent(new TMEEvent(SIMULATION_FINISHED_EVENT));
            }
        }
    }
}
