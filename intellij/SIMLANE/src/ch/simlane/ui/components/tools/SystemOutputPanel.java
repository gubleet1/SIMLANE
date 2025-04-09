package ch.simlane.ui.components.tools;

import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.editor.tools.SystemOutput;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

public class SystemOutputPanel extends JPanel implements StateChangeListener {

    public static final String TOOLS_ACTION_COMMAND_CLEAR_LOG = "CLEAR_LOG";

    private SystemOutput systemOutput;

    private JLabel title;
    private JScrollPane scrollPane;
    private JTextPane textPane;
    private JButton clearButton;

    public SystemOutputPanel(SystemOutput systemOutput) {
        this.systemOutput = systemOutput;
        initialize();
    }

    private void initialize() {
        systemOutput.addStateChangeListener(this);
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        createTitle();
        createTextPane();
        createScrollPane();
        createClearButton();
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10)
                                .addComponent(title, 0, DEFAULT_SIZE, Integer.MAX_VALUE)
                                .addComponent(clearButton, 0, 85, 85)
                                .addGap(10))
                        .addComponent(scrollPane, 0, 0, Integer.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(title)
                                .addComponent(clearButton))
                        .addComponent(scrollPane, 150, 150, Integer.MAX_VALUE)
        );
    }

    private void createTitle() {
        // create the title
        title = new JLabel("System Output");
        title.setFont(new Font("Arial", Font.PLAIN, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    private void createTextPane() {
        // create the log pane
        textPane = new JTextPane(systemOutput.getDocument());
        textPane.setEditable(false);
    }

    private void createScrollPane() {
        // create the scroll pane
        scrollPane = new JScrollPane(textPane);
        Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 10, 10, 10);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(emptyBorder, lineBorder));
    }

    private void createClearButton() {
        // create the clear button
        clearButton = new JButton("Clear");
        clearButton.setActionCommand(TOOLS_ACTION_COMMAND_CLEAR_LOG);
        clearButton.setFocusable(false);
        clearButton.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void updateScrollPane() {
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

    @Override
    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(SystemOutput.LOG_CHANGED_EVENT)) {
            EventQueue.invokeLater(this::updateScrollPane);
        }
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        clearButton.addActionListener(listener);
    }
}
