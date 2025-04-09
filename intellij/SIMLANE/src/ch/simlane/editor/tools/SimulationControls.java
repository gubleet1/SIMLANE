package ch.simlane.editor.tools;

import ch.simlane.editor.event.ObservableStateObject;
import ch.simlane.editor.event.StateChangeEvent;

import java.time.Duration;

public class SimulationControls extends ObservableStateObject {

    public static final String STATE_CHANGED_EVENT = "SimulationControls.STATE_CHANGED_EVENT";
    public static final String TIME_CHANGED_EVENT = "SimulationControls.TIME_CHANGED_EVENT";

    public static final int STATE_DISABLED = 9000;
    public static final int STATE_VALIDATING = 9001;
    public static final int STATE_READY = 9002;
    public static final int STATE_RUNNING = 9003;
    public static final int STATE_PAUSED = 9004;
    public static final int STATE_FINISHED = 9005;
    public static final int STATE_FAILED = 9006;

    private int state;

    private Duration time;

    public SimulationControls() {
        time = Duration.ZERO;
        state = STATE_DISABLED;
    }

    public Duration getTime() {
        return time;
    }

    public void setTime(Duration time) {
        this.time = time;
        fireStateChange(new StateChangeEvent(TIME_CHANGED_EVENT));
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        fireStateChange(new StateChangeEvent(STATE_CHANGED_EVENT));
    }
}
