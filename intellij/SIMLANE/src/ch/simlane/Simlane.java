package ch.simlane;

import ch.simlane.editor.Editor;
import ch.simlane.tme.Engine;
import ch.simlane.ui.SimlaneUI;
import ch.simlane.utils.SimlaneTMEListener;
import ch.simlane.utils.SimlaneUIListener;

import static ch.simlane.editor.tools.SystemOutput.MESSAGE_TYPE_INFO;
import static ch.simlane.editor.tools.SystemOutput.SYSTEM_STARTUP_MESSAGE;

// the controller of the MVC architecture
public class Simlane {

    public static final String FILE_EXTENSION = "siml";

    public static final boolean DEBUG = true;

    // the model of the MVC architecture
    private Editor editor;
    // the view of the MVC architecture
    private SimlaneUI ui;

    private Engine tme;

    public Simlane() {
        editor = new Editor();
        ui = new SimlaneUI(editor);
        tme = new Engine();
        ui.addSimlaneUIListener(new SimlaneUIListener(this));
        tme.addTMEListener(new SimlaneTMEListener(this));
        editor.getTools().getSystemOutput().log(SYSTEM_STARTUP_MESSAGE, MESSAGE_TYPE_INFO);
    }

    // main entry point
    public static void main(String[] args) {
        // create SIMLANE application instance
        new Simlane();
    }

    public Editor getEditor() {
        return editor;
    }

    public SimlaneUI getUI() {
        return ui;
    }

    public Engine getTme() {
        return tme;
    }
}
