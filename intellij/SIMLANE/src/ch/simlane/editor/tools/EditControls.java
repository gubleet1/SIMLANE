package ch.simlane.editor.tools;

import ch.simlane.editor.event.ObservableStateObject;
import ch.simlane.editor.event.StateChangeEvent;

public class EditControls extends ObservableStateObject {

    public static final String ENABLED_CHANGED_EVENT = "EditControls.ENABLED_CHANGED_EVENT";

    private TileConfigurator tileConfigurator;

    private boolean enabled;

    public EditControls() {
        tileConfigurator = new TileConfigurator();
        enabled = true;
    }

    public TileConfigurator getTileConfigurator() {
        return tileConfigurator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (!enabled) {
            tileConfigurator.clearSelection();
        }
        fireStateChange(new StateChangeEvent(ENABLED_CHANGED_EVENT));
    }
}
