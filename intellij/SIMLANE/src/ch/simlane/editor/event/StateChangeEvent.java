package ch.simlane.editor.event;

public class StateChangeEvent {

    private String type;

    public StateChangeEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
