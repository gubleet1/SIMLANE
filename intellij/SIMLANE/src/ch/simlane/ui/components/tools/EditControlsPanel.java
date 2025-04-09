package ch.simlane.ui.components.tools;

import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.editor.tools.EditControls;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import java.awt.*;

public class EditControlsPanel extends JPanel implements StateChangeListener {

    public static final String TOOLS_ACTION_COMMAND_CLEAR_TILE = "CLEAR_TILE";
    public static final String TOOLS_ACTION_COMMAND_CLEAR_MAP = "CLEAR_MAP";

    private EditControls editControls;

    private JLabel title;
    private TileConfiguratorPanel tileConfiguratorPanel;
    private JButton clearTile;
    private JButton clearMap;

    public EditControlsPanel(EditControls editControls) {
        this.editControls = editControls;
        initialize();
    }

    private void initialize() {
        editControls.addStateChangeListener(this);
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        createTitle();
        tileConfiguratorPanel = new TileConfiguratorPanel(editControls.getTileConfigurator());
        createButtons();
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(title)
                        .addComponent(tileConfiguratorPanel)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Integer.MAX_VALUE)
                                .addComponent(clearTile)
                                .addGap(0, 0, Integer.MAX_VALUE)
                                .addComponent(clearMap)
                                .addGap(0, 0, Integer.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(title)
                        .addComponent(tileConfiguratorPanel)
                        .addGap(5)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(clearTile)
                                .addComponent(clearMap))
                        .addGap(20)
        );
        layout.linkSize(SwingConstants.HORIZONTAL, clearTile, clearMap);
        updateButtons();
    }

    @Override
    public GroupLayout getLayout() {
        return (GroupLayout) super.getLayout();
    }

    public boolean adjustSize(int size) {
        if (tileConfiguratorPanel.adjustSize(size)) {
            getLayout().invalidateLayout(this);
            revalidate();
            return true;
        }
        return false;
    }

    private void createTitle() {
        // create the title
        title = new JLabel("Tile Configurator");
        title.setFont(new Font("Arial", Font.PLAIN, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    private void createButtons() {
        // create the clear tile button
        clearTile = new JButton("Clear Tile");
        clearTile.setActionCommand(TOOLS_ACTION_COMMAND_CLEAR_TILE);
        clearTile.setFocusable(false);
        clearTile.setFont(new Font("Arial", Font.PLAIN, 12));
        // create the clear map button
        clearMap = new JButton("Clear Map");
        clearMap.setActionCommand(TOOLS_ACTION_COMMAND_CLEAR_MAP);
        clearMap.setFocusable(false);
        clearMap.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void updateButtons() {
        boolean enabled = editControls.isEnabled();
        clearTile.setEnabled(enabled);
        clearMap.setEnabled(enabled);
    }

    @Override
    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(EditControls.ENABLED_CHANGED_EVENT)) {
            EventQueue.invokeLater(() -> {
                updateButtons();
                repaint();
            });
        }
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        tileConfiguratorPanel.addSimlaneUIListener(listener);
        clearTile.addActionListener(listener);
        clearMap.addActionListener(listener);
    }
}
