package ch.simlane.editor;

import ch.simlane.editor.tools.EditControls;
import ch.simlane.editor.tools.SimulationControls;
import ch.simlane.editor.tools.SystemOutput;

public class Tools {

    private EditControls editControls;
    private SimulationControls simulationControls;
    private SystemOutput systemOutput;

    public Tools() {
        editControls = new EditControls();
        simulationControls = new SimulationControls();
        systemOutput = new SystemOutput();
    }

    public EditControls getEditControls() {
        return editControls;
    }

    public SimulationControls getSimulationControls() {
        return simulationControls;
    }

    public SystemOutput getSystemOutput() {
        return systemOutput;
    }
}
