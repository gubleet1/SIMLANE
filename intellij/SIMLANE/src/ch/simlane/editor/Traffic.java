package ch.simlane.editor;

import ch.simlane.editor.event.ObservableStateObject;
import ch.simlane.editor.event.StateChangeEvent;

import java.util.LinkedList;
import java.util.List;

public class Traffic extends ObservableStateObject {

    public static final String TRAFFIC_UPDATED_EVENT = "Traffic.TRAFFIC_UPDATED_EVENT";

    // indicators of the cars
    private List<CarIndicator> traffic;
    private boolean enabled;

    public Traffic() {
        traffic = new LinkedList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        fireStateChange(new StateChangeEvent(TRAFFIC_UPDATED_EVENT));
    }

    public List<CarIndicator> getTraffic() {
        return traffic;
    }

    public void setTraffic(List<CarIndicator> traffic) {
        this.traffic = traffic;
        fireStateChange(new StateChangeEvent(TRAFFIC_UPDATED_EVENT));
    }

    public void clearTraffic() {
        traffic = new LinkedList<>();
        fireStateChange(new StateChangeEvent(TRAFFIC_UPDATED_EVENT));
    }
}
