package ch.simlane.ui.components;

import ch.simlane.editor.Tools;
import ch.simlane.ui.components.tools.EditControlsPanel;
import ch.simlane.ui.components.tools.SimulationControlsPanel;
import ch.simlane.ui.components.tools.SystemOutputPanel;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import java.awt.*;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

public class EditorToolsPanel extends JPanel {

    private Tools tools;

    private EditControlsPanel editControlsPanel;
    private SimulationControlsPanel simulationControlsPanel;
    private SystemOutputPanel systemOutputPanel;

    public EditorToolsPanel(Tools tools) {
        this.tools = tools;
        initialize();
    }

    private void initialize() {
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        editControlsPanel = new EditControlsPanel(tools.getEditControls());
        simulationControlsPanel = new SimulationControlsPanel(tools.getSimulationControls());
        systemOutputPanel = new SystemOutputPanel(tools.getSystemOutput());
        JSeparator separator1 = new JSeparator();
        JSeparator separator2 = new JSeparator();
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(editControlsPanel)
                        .addComponent(separator1)
                        .addComponent(simulationControlsPanel)
                        .addComponent(separator2)
                        .addComponent(systemOutputPanel)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(editControlsPanel, 0, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(separator1, 0, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(simulationControlsPanel, 0, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(separator2, 0, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(systemOutputPanel)
        );
    }

    @Override
    public GroupLayout getLayout() {
        return (GroupLayout) super.getLayout();
    }

    public void adjustSize(int size) {
        Insets i = getInsets();
        size -= i.left + i.right;
        if (editControlsPanel.adjustSize(size)) {
            getLayout().invalidateLayout(this);
            revalidate();
        }
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        editControlsPanel.addSimlaneUIListener(listener);
        simulationControlsPanel.addSimlaneUIListener(listener);
        systemOutputPanel.addSimlaneUIListener(listener);
    }
}
