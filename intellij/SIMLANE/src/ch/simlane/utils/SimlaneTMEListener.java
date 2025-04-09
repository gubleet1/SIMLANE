package ch.simlane.utils;

import ch.simlane.Simlane;
import ch.simlane.SimulationStateParser;
import ch.simlane.editor.Editor;
import ch.simlane.editor.tools.EditControls;
import ch.simlane.editor.tools.SimulationControls;
import ch.simlane.editor.tools.SystemOutput;
import ch.simlane.tme.Engine;
import ch.simlane.tme.TMEEvent;
import ch.simlane.tme.TMEListener;

import static ch.simlane.editor.tools.SystemOutput.*;

public class SimlaneTMEListener implements TMEListener {

    private Editor editor;
    private EditControls editControls;
    private SimulationControls simulationControls;
    private SystemOutput systemOutput;

    private Engine tme;
    private SimulationStateParser simulationStateParser;

    public SimlaneTMEListener(Simlane simlane) {
        editor = simlane.getEditor();
        editControls = editor.getTools().getEditControls();
        simulationControls = editor.getTools().getSimulationControls();
        systemOutput = editor.getTools().getSystemOutput();
        tme = simlane.getTme();
        simulationStateParser = new SimulationStateParser(editor);
    }

    @Override
    public void tmeEvent(TMEEvent event) {
        switch (event.getType()) {
            case Engine.SIMULATION_STATE_CHANGED_EVENT:
                simulationStateChanged();
                break;
            case Engine.MODEL_VALIDATION_SUCCESSFUL_EVENT:
                modelValidationSuccessful();
                break;
            case Engine.MODEL_VALIDATION_FAILED_EVENT:
                modelValidationFailed();
                break;
            case Engine.SIMULATION_READY_EVENT:
                simulationReady();
                break;
            case Engine.SIMULATION_STARTED_EVENT:
                simulationStarted();
                break;
            case Engine.SIMULATION_PAUSED_EVENT:
                simulationPaused();
                break;
            case Engine.SIMULATION_FINISHED_EVENT:
                simulationFinished();
                break;
            case Engine.SIMULATION_FAILED_EVENT:
                simulationFailed();
                break;
        }
    }

    private void simulationStateChanged() {
        int state = simulationControls.getState();
        if (state == SimulationControls.STATE_DISABLED) {
            return;
        }
        // synchronously parsing the simulation state
        simulationStateParser.parse(tme.getSimulationState());
    }

    private void modelValidationSuccessful() {
        systemOutput.log(VALIDATION_SUCCESSFUL_MESSAGE, MESSAGE_TYPE_SUCCESS);
    }

    private void modelValidationFailed() {
        systemOutput.log(VALIDATION_FAILED_MESSAGE, MESSAGE_TYPE_WARNING);
        simulationControls.setState(SimulationControls.STATE_DISABLED);
        editControls.setEnabled(true);
    }

    private void simulationReady() {
        int state = simulationControls.getState();
        if (state == SimulationControls.STATE_VALIDATING) {
            editor.setDisplaySimulationStateEnabled(true);
        }
        systemOutput.log(SIMULATION_READY_MESSAGE, MESSAGE_TYPE_INFO);
        simulationControls.setState(SimulationControls.STATE_READY);
    }

    private void simulationStarted() {
        int state = simulationControls.getState();
        if (state == SimulationControls.STATE_READY) {
            systemOutput.log(SIMULATION_STARTED_MESSAGE, MESSAGE_TYPE_INFO);
        }
        if (state == SimulationControls.STATE_PAUSED) {
            systemOutput.log(SIMULATION_RESUMED_MESSAGE, MESSAGE_TYPE_INFO);
        }
        simulationControls.setState(SimulationControls.STATE_RUNNING);
    }

    private void simulationPaused() {
        systemOutput.log(SIMULATION_PAUSED_MESSAGE, MESSAGE_TYPE_INFO);
        simulationControls.setState(SimulationControls.STATE_PAUSED);
    }

    private void simulationFinished() {
        systemOutput.log(SIMULATION_FINISHED_MESSAGE, MESSAGE_TYPE_SUCCESS);
        simulationControls.setState(SimulationControls.STATE_FINISHED);
    }

    private void simulationFailed() {
        systemOutput.log(SIMULATION_FAILED_MESSAGE, MESSAGE_TYPE_ERROR);
        simulationControls.setState(SimulationControls.STATE_FAILED);
    }
}
