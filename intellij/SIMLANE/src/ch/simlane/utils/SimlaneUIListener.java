package ch.simlane.utils;

import ch.simlane.ModelLoader;
import ch.simlane.Simlane;
import ch.simlane.editor.Editor;
import ch.simlane.editor.Map;
import ch.simlane.editor.Tile;
import ch.simlane.editor.scenario.Scenario;
import ch.simlane.editor.tools.EditControls;
import ch.simlane.editor.tools.SimulationControls;
import ch.simlane.editor.tools.SystemOutput;
import ch.simlane.editor.tools.TileConfigurator;
import ch.simlane.tme.Engine;
import ch.simlane.ui.SimlaneUI;
import ch.simlane.ui.components.EditorLayersPanel;
import ch.simlane.ui.components.MapPanel;
import ch.simlane.ui.components.TilePanel;
import ch.simlane.ui.components.tools.EditControlsPanel;
import ch.simlane.ui.components.tools.SimulationControlsPanel;
import ch.simlane.ui.components.tools.SystemOutputPanel;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static ch.simlane.editor.tools.SystemOutput.*;
import static ch.simlane.ui.components.tools.TileConfiguratorPanel.LaneCheckBox;

public class SimlaneUIListener implements ActionListener, MouseListener {

    private Editor editor;
    private EditControls editControls;
    private SimulationControls simulationControls;
    private SystemOutput systemOutput;
    private TileConfigurator tileConfigurator;

    private Engine tme;

    private SimlaneUI ui;

    private boolean tileSelectMode;
    private boolean tileSelectActionIsRemove;

    public SimlaneUIListener(Simlane simlane) {
        editor = simlane.getEditor();
        editControls = editor.getTools().getEditControls();
        simulationControls = editor.getTools().getSimulationControls();
        systemOutput = editor.getTools().getSystemOutput();
        tileConfigurator = editControls.getTileConfigurator();
        tme = simlane.getTme();
        ui = simlane.getUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof LaneCheckBox) {
            LaneCheckBox checkBox = (LaneCheckBox) source;
            laneCheckBoxClicked(checkBox);
            return;
        }
        if (source instanceof JButton) {
            JButton button = (JButton) source;
            String command = button.getActionCommand();
            switch (command) {
                case EditControlsPanel.TOOLS_ACTION_COMMAND_CLEAR_TILE:
                    clearTile();
                    break;
                case EditControlsPanel.TOOLS_ACTION_COMMAND_CLEAR_MAP:
                    clearMap();
                    break;
                case SimulationControlsPanel.TOOLS_ACTION_COMMAND_VALIDATE_MAP:
                    validateMap();
                    break;
                case SimulationControlsPanel.TOOLS_ACTION_COMMAND_EDIT_MAP:
                    editMap();
                    break;
                case SimulationControlsPanel.TOOLS_ACTION_COMMAND_START_SIMULATION:
                    startSimulation();
                    break;
                case SimulationControlsPanel.TOOLS_ACTION_COMMAND_PAUSE_SIMULATION:
                    pauseSimulation();
                    break;
                case SimulationControlsPanel.TOOLS_ACTION_COMMAND_RESET_SIMULATION:
                    resetSimulation();
                    break;
                case SystemOutputPanel.TOOLS_ACTION_COMMAND_CLEAR_LOG:
                    clearLog();
                    break;
            }
            return;
        }
        if (source instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) source;
            Container parent = menuItem.getParent();
            if (parent instanceof JPopupMenu) {
                JPopupMenu popupMenu = (JPopupMenu) parent;
                JMenu menu = (JMenu) popupMenu.getInvoker();
                switch (menu.getActionCommand()) {
                    case SimlaneUI.MENU_ACTION_COMMAND_FILE:
                        fileMenuCommand(e.getActionCommand());
                        break;
                    case SimlaneUI.MENU_ACTION_COMMAND_SCENARIOS:
                        scenarioMenuCommand(e.getActionCommand());
                        break;
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof MapPanel || source instanceof EditorLayersPanel) {
            tileConfigurator.clearSelection();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof TilePanel) {
            if (editControls.isEnabled()) {
                Tile tile = ((TilePanel) source).getTile();
                boolean remove = false;
                if (e.isControlDown()) {
                    remove = tileConfigurator.isSelected(tile);
                }
                tileSelectModeEnable(remove);
                if (e.isControlDown()) {
                    modifyTileSelection(tile);
                    return;
                }
                tileConfigurator.setSelectedTile(tile);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        tileSelectModeDisable();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof TilePanel) {
            Tile tile = ((TilePanel) source).getTile();
            if (tileSelectMode) {
                modifyTileSelection(tile);
            }
            return;
        }
        if (source instanceof MapPanel) {
            tileSelectModeDisable();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private void tileSelectModeEnable(boolean remove) {
        tileSelectMode = true;
        tileSelectActionIsRemove = remove;
    }

    private void tileSelectModeDisable() {
        if (tileSelectMode) {
            tileSelectMode = false;
        }
    }

    private void modifyTileSelection(Tile tile) {
        if (tileSelectActionIsRemove) {
            tileConfigurator.removeSelectedTile(tile);
            return;
        }
        tileConfigurator.addSelectedTile(tile);
    }

    private void laneCheckBoxClicked(LaneCheckBox checkBox) {
        int sideFrom = checkBox.getSideFrom();
        int sideTo = checkBox.getSideTo();
        int state = checkBox.isSelected() ? Tile.LANE_STATE_SELECTED : Tile.LANE_STATE_UNSELECTED;
        tileConfigurator.setLaneState(sideFrom, sideTo, state);
    }

    private void clearTile() {
        tileConfigurator.setLaneState(Tile.LANE_STATE_UNSELECTED);
    }

    private void clearMap() {
        for (Tile tile : editor.getMap().getTiles()) {
            tile.setLaneState(Tile.LANE_STATE_UNSELECTED);
        }
        tileConfigurator.updateLaneState();
    }

    private void validateMap() {
        int state = simulationControls.getState();
        if (state != SimulationControls.STATE_DISABLED) {
            return;
        }
        systemOutput.log(VALIDATION_STARTED_MESSAGE, MESSAGE_TYPE_INFO);
        editControls.setEnabled(false);
        simulationControls.setState(SimulationControls.STATE_VALIDATING);
        Map map = editor.getMap();
        Scenario scenario = editor.getScenario();
        new Thread(new ModelLoader(map, scenario, tme)).start();
    }

    private void editMap() {
        int state = simulationControls.getState();
        if (state == SimulationControls.STATE_READY || state == SimulationControls.STATE_PAUSED ||
                state == SimulationControls.STATE_FINISHED || state == SimulationControls.STATE_FAILED) {
            editor.setDisplaySimulationStateEnabled(false);
            editor.getTraffic().clearTraffic();
            editor.getMap().getTiles().forEach(Tile::clearTrafficLights);
            simulationControls.setTime(Duration.ZERO);
            tme.reset();
            systemOutput.log(EDIT_MAP_MESSAGE, MESSAGE_TYPE_INFO);
            simulationControls.setState(SimulationControls.STATE_DISABLED);
            editControls.setEnabled(true);
        }
    }

    private void startSimulation() {
        int state = simulationControls.getState();
        if (state == SimulationControls.STATE_READY || state == SimulationControls.STATE_PAUSED) {
            tme.startSimulation();
        }
    }

    private void pauseSimulation() {
        int state = simulationControls.getState();
        if (state == SimulationControls.STATE_RUNNING) {
            tme.pauseSimulation();
        }
    }

    private void resetSimulation() {
        int state = simulationControls.getState();
        if (state == SimulationControls.STATE_PAUSED || state == SimulationControls.STATE_FINISHED ||
                state == SimulationControls.STATE_FAILED) {
            tme.resetSimulation();
        }
    }

    private void clearLog() {
        systemOutput.clear();
    }

    private void fileMenuCommand(String command) {
        switch (command) {
            case SimlaneUI.MENU_ITEM_ACTION_COMMAND_LOAD_MAP:
                loadMap();
                break;
            case SimlaneUI.MENU_ITEM_ACTION_COMMAND_SAVE_MAP:
                saveMap();
                break;
            case SimlaneUI.MENU_ITEM_ACTION_COMMAND_EXIT:
                exitSimlane();
                break;
        }
    }

    private void loadMap() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open");
        fileChooser.setFileFilter(new SimlaneFileFilter());
        int selection = fileChooser.showOpenDialog(ui.getFrame());
        if (selection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JSONMap jsonMap;
            try {
                jsonMap = new JSONMap(IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8));
            } catch (Exception e) {
                systemOutput.log(MAP_LOAD_FAILED_MESSAGE, MESSAGE_TYPE_ERROR);
                return;
            }
            Map map = jsonMap.getMap();
            Scenario scenario = editor.getScenario();
            if (scenario.getMapRows() != map.getNumRows() || scenario.getMapCols() != map.getNumCols()) {
                systemOutput.log(MAP_SIZE_MISMATCH_MESSAGE, MESSAGE_TYPE_ERROR);
                return;
            }
            if (!scenario.getName().equals(jsonMap.getScenario())) {
                systemOutput.log(MAP_SCENARIO_MISMATCH_MESSAGE, MESSAGE_TYPE_WARNING);
            }
            editor.loadMap(map);
            systemOutput.log(MAP_LOAD_SUCCESSFUL_MESSAGE, MESSAGE_TYPE_SUCCESS);
        }
    }

    private void saveMap() {
        JFileChooser fileChooser = new JFileChooser() {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (getDialogType() == SAVE_DIALOG) {
                    if (f.getName().indexOf('.') == -1) {
                        f = new File(f.getPath() + "." + Simlane.FILE_EXTENSION);
                        setSelectedFile(f);
                    }
                    if (f.exists()) {
                        String message = f.getName() + " already exists. Do you want to replace it?";
                        String title = "Confirm Save As";
                        int result = JOptionPane.showConfirmDialog(this, message,
                                title, JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            super.approveSelection();
                        }
                        return;
                    }
                }
                super.approveSelection();
            }
        };
        fileChooser.setDialogTitle("Save As");
        fileChooser.setFileFilter(new SimlaneFileFilter());
        int selection = fileChooser.showSaveDialog(ui.getFrame());
        if (selection == JFileChooser.APPROVE_OPTION) {
            Map map = editor.getMap();
            String scenario = editor.getScenario().getName();
            File file = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(new JSONMap(map, scenario).getJSON());
            } catch (Exception e) {
                systemOutput.log(MAP_SAVE_FAILED_MESSAGE, MESSAGE_TYPE_ERROR);
                return;
            }
            systemOutput.log(MAP_SAVE_SUCCESSFUL_MESSAGE, MESSAGE_TYPE_SUCCESS);
        }
    }

    private void exitSimlane() {
        System.exit(0);
    }

    private void scenarioMenuCommand(String command) {
        editor.loadScenario(command);
    }
}
