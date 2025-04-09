package ch.simlane.editor.event;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObservableStateObject {

    private ExecutorService executor;
    private List<StateChangeListener> stateChangeListeners;

    public ObservableStateObject() {
        executor = Executors.newSingleThreadExecutor();
        stateChangeListeners = new LinkedList<>();
    }

    public void addStateChangeListener(StateChangeListener listener) {
        if (!stateChangeListeners.contains(listener)) {
            stateChangeListeners.add(listener);
        }
    }

    public void fireStateChange(StateChangeEvent event) {
        for (StateChangeListener listener : stateChangeListeners) {
            executor.execute(() -> listener.stateChange(event));
        }
    }
}
