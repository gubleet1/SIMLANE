package ch.simlane.editor.tools;

import ch.simlane.editor.event.ObservableStateObject;
import ch.simlane.editor.event.StateChangeEvent;

import javax.swing.text.*;
import java.awt.*;

public class SystemOutput extends ObservableStateObject {

    public static final String LOG_CHANGED_EVENT = "SystemOutput.LOG_CHANGED_EVENT";

    public static final String SYSTEM_STARTUP_MESSAGE = "SIMLANE started";
    public static final String VALIDATION_STARTED_MESSAGE = "Validating map...";
    public static final String VALIDATION_SUCCESSFUL_MESSAGE = "Map validation successful";
    public static final String VALIDATION_FAILED_MESSAGE = "Map validation failed";
    public static final String SIMULATION_READY_MESSAGE = "Ready for simulation";
    public static final String SIMULATION_STARTED_MESSAGE = "Simulation started";
    public static final String SIMULATION_PAUSED_MESSAGE = "Simulation paused";
    public static final String SIMULATION_RESUMED_MESSAGE = "Simulation resumed";
    public static final String SIMULATION_FINISHED_MESSAGE = "Simulation successful";
    public static final String SIMULATION_FAILED_MESSAGE = "Simulation failed";
    public static final String EDIT_MAP_MESSAGE = "Leaving simulation mode";
    public static final String MAP_SIZE_MISMATCH_MESSAGE = "The size of the map does not match the size of " +
            "the current scenario.";
    public static final String MAP_SCENARIO_MISMATCH_MESSAGE = "The map was created using a different scenario.";
    public static final String MAP_LOAD_SUCCESSFUL_MESSAGE = "Map loaded successfully";
    public static final String MAP_LOAD_FAILED_MESSAGE = "The map file could not be loaded.";
    public static final String MAP_SAVE_SUCCESSFUL_MESSAGE = "Map saved successfully";
    public static final String MAP_SAVE_FAILED_MESSAGE = "The map file could not be created.";

    public static final int MESSAGE_TYPE_INFO = 8000;
    public static final int MESSAGE_TYPE_SUCCESS = 8001;
    public static final int MESSAGE_TYPE_WARNING = 8002;
    public static final int MESSAGE_TYPE_ERROR = 8003;

    private static final Color COLOR_INFO = new Color(0, 123, 255);
    private static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private static final Color COLOR_WARNING = new Color(255, 123, 15);
    private static final Color COLOR_ERROR = new Color(220, 53, 69);

    private static final String LABEL_INFO = "INFO";
    private static final String LABEL_SUCCESS = "OK";
    private static final String LABEL_WARNING = "WARN";
    private static final String LABEL_ERROR = "ERR";

    private StyledDocument document;
    private SimpleAttributeSet textAttributes;
    private SimpleAttributeSet paragraphAttributes;
    private SimpleAttributeSet infoAttributes;
    private SimpleAttributeSet successAttributes;
    private SimpleAttributeSet warningAttributes;
    private SimpleAttributeSet errorAttributes;

    private boolean empty;

    public SystemOutput() {
        document = new DefaultStyledDocument();
        empty = true;
        initialize();
    }

    private void initialize() {
        textAttributes = new SimpleAttributeSet();
        StyleConstants.setFontSize(textAttributes, 13);
        paragraphAttributes = new SimpleAttributeSet();
        TabSet tabSet = new TabSet(new TabStop[]{new TabStop(60)});
        StyleConstants.setTabSet(paragraphAttributes, tabSet);
        SimpleAttributeSet label = new SimpleAttributeSet(textAttributes);
        StyleConstants.setBold(label, true);
        infoAttributes = new SimpleAttributeSet(label);
        StyleConstants.setForeground(infoAttributes, COLOR_INFO);
        successAttributes = new SimpleAttributeSet(label);
        StyleConstants.setForeground(successAttributes, COLOR_SUCCESS);
        warningAttributes = new SimpleAttributeSet(label);
        StyleConstants.setForeground(warningAttributes, COLOR_WARNING);
        errorAttributes = new SimpleAttributeSet(label);
        StyleConstants.setForeground(errorAttributes, COLOR_ERROR);
    }

    public StyledDocument getDocument() {
        return document;
    }

    public synchronized void log(String message, int type) {
        try {
            int length = document.getLength();
            if (!empty) {
                document.insertString(length, "\n", textAttributes);
                length++;
            } else {
                empty = false;
            }
            document.insertString(length, getLabel(type), getLabelAttributes(type));
            document.setParagraphAttributes(length, 0, paragraphAttributes, false);
            length = document.getLength();
            document.insertString(length, message, textAttributes);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        fireStateChange(new StateChangeEvent(LOG_CHANGED_EVENT));
    }

    public synchronized void clear() {
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        empty = true;
        fireStateChange(new StateChangeEvent(LOG_CHANGED_EVENT));
    }

    private String getLabel(int type) {
        String label;
        switch (type) {
            default:
            case MESSAGE_TYPE_INFO:
                label = LABEL_INFO;
                break;
            case MESSAGE_TYPE_SUCCESS:
                label = LABEL_SUCCESS;
                break;
            case MESSAGE_TYPE_WARNING:
                label = LABEL_WARNING;
                break;
            case MESSAGE_TYPE_ERROR:
                label = LABEL_ERROR;
                break;
        }
        return "[" + label + "]\t";
    }

    private AttributeSet getLabelAttributes(int type) {
        switch (type) {
            default:
            case MESSAGE_TYPE_INFO:
                return infoAttributes;
            case MESSAGE_TYPE_SUCCESS:
                return successAttributes;
            case MESSAGE_TYPE_WARNING:
                return warningAttributes;
            case MESSAGE_TYPE_ERROR:
                return errorAttributes;
        }
    }
}
