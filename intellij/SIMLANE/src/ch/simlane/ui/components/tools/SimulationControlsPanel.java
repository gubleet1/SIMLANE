package ch.simlane.ui.components.tools;

import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.editor.tools.SimulationControls;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

import static ch.simlane.editor.tools.SimulationControls.*;

public class SimulationControlsPanel extends JPanel implements StateChangeListener {

    public static final String TOOLS_ACTION_COMMAND_VALIDATE_MAP = "VALIDATE_MAP";
    public static final String TOOLS_ACTION_COMMAND_EDIT_MAP = "EDIT_MAP";
    public static final String TOOLS_ACTION_COMMAND_START_SIMULATION = "START_SIMULATION";
    public static final String TOOLS_ACTION_COMMAND_PAUSE_SIMULATION = "PAUSE_SIMULATION";
    public static final String TOOLS_ACTION_COMMAND_RESET_SIMULATION = "RESET_SIMULATION";

    private static final String TIME_LABEL = "Time";

    private SimulationControls simulationControls;

    private JLabel title;
    private JButton validateMap;
    private JButton editMap;
    private JButton start;
    private JButton reset;
    private JLabel timeLabel;

    public SimulationControlsPanel(SimulationControls simulationControls) {
        this.simulationControls = simulationControls;
        initialize();
    }

    private void initialize() {
        simulationControls.addStateChangeListener(this);
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        createTitle();
        createButtons();
        createTimeLabel();
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10)
                                .addComponent(title))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10)
                                .addComponent(validateMap, 0, 120, 120)
                                .addGap(0, 0, Integer.MAX_VALUE)
                                .addComponent(editMap, 0, 120, 120)
                                .addGap(10))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10)
                                .addComponent(start, 0, 85, 85)
                                .addGap(0, 0, Integer.MAX_VALUE)
                                .addComponent(timeLabel)
                                .addGap(0, 0, Integer.MAX_VALUE)
                                .addComponent(reset, 0, 85, 85)
                                .addGap(10))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(10)
                        .addComponent(title)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(validateMap)
                                .addComponent(editMap))
                        .addGap(15)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(start)
                                .addComponent(timeLabel)
                                .addComponent(reset))
                        .addGap(20)
        );
        updateTime();
        updateButtons();
    }

    private void createTitle() {
        // create the title
        title = new JLabel("Simulation");
        title.setFont(new Font("Arial", Font.PLAIN, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    private void createButtons() {
        // create the validate map button
        validateMap = new JButton("Validate Map");
        validateMap.setActionCommand(TOOLS_ACTION_COMMAND_VALIDATE_MAP);
        validateMap.setFocusable(false);
        validateMap.setFont(new Font("Arial", Font.PLAIN, 12));
        // create the edit map button
        editMap = new JButton("Edit Map");
        editMap.setActionCommand(TOOLS_ACTION_COMMAND_EDIT_MAP);
        editMap.setFocusable(false);
        editMap.setFont(new Font("Arial", Font.PLAIN, 12));
        // create the start button
        start = new JButton("Start");
        start.setActionCommand(TOOLS_ACTION_COMMAND_START_SIMULATION);
        start.setFocusable(false);
        start.setFont(new Font("Arial", Font.PLAIN, 12));
        // create the reset button
        reset = new JButton("Reset");
        reset.setActionCommand(TOOLS_ACTION_COMMAND_RESET_SIMULATION);
        reset.setFocusable(false);
        reset.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void createTimeLabel() {
        // create the time label
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    }

    private void updateButtons() {
        int state = simulationControls.getState();
        for (JButton button : new JButton[]{validateMap, editMap, start, reset}) {
            boolean enabled = false;
            if (button.equals(validateMap)) {
                if (state == STATE_DISABLED) {
                    enabled = true;
                }
            } else if (button.equals(editMap)) {
                if (state == STATE_READY || state == STATE_PAUSED ||
                        state == STATE_FINISHED || state == STATE_FAILED) {
                    enabled = true;
                }
            } else if (button.equals(start)) {
                if (state == STATE_READY || state == STATE_RUNNING || state == STATE_PAUSED) {
                    enabled = true;
                    if (state == STATE_RUNNING) {
                        button.setText("Pause");
                        button.setActionCommand(TOOLS_ACTION_COMMAND_PAUSE_SIMULATION);
                    } else {
                        if (state == STATE_READY) {
                            button.setText("Start");
                        } else {
                            button.setText("Resume");
                        }
                        button.setActionCommand(TOOLS_ACTION_COMMAND_START_SIMULATION);
                    }
                } else {
                    button.setText("Start");
                    button.setActionCommand(TOOLS_ACTION_COMMAND_START_SIMULATION);
                }
            } else if (button.equals(reset)) {
                if (state == STATE_PAUSED || state == STATE_FINISHED || state == STATE_FAILED) {
                    enabled = true;
                }
            }
            button.setEnabled(enabled);
        }
    }

    private void updateTime() {
        Duration time = simulationControls.getTime();
        long minutes = time.toMinutes();
        int seconds = time.toSecondsPart();
        String text = TIME_LABEL + " " + minutes + ":" + String.format("%02d", seconds);
        timeLabel.setText(text);
    }

    @Override
    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(SimulationControls.STATE_CHANGED_EVENT)) {
            EventQueue.invokeLater(() -> {
                updateButtons();
                repaint();
            });
        }
        if (event.getType().equals(SimulationControls.TIME_CHANGED_EVENT)) {
            EventQueue.invokeLater(() -> {
                updateTime();
                repaint();
            });
        }
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        validateMap.addActionListener(listener);
        editMap.addActionListener(listener);
        start.addActionListener(listener);
        reset.addActionListener(listener);
    }
}
