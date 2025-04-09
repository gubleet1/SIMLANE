package ch.simlane.ui.components;

import ch.simlane.editor.Editor;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.ui.SimlaneUI;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import java.awt.*;

public class EditorLayersPanel extends JPanel implements StateChangeListener {

    private Editor editor;

    private JLayeredPane layeredPane;

    private GlassPanel glassPanel;
    private TrafficPanel trafficPanel;
    private ScenarioPanel scenarioPanel;
    private MapPanel mapPanel;

    private SimlaneUIListener simlaneUIListener;

    public EditorLayersPanel(Editor editor) {
        this.editor = editor;
        initialize();
    }

    private void initialize() {
        editor.addStateChangeListener(this);
        setLayout(new GridLayout());
        createEditorLayeredPane();
        add(layeredPane);
    }

    private void createEditorLayeredPane() {
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new LayerLayout());
        createEditorLayers();
        initEditorLayers();
        layeredPane.add(glassPanel, Integer.valueOf(3));
        layeredPane.add(trafficPanel, Integer.valueOf(2));
        layeredPane.add(scenarioPanel, Integer.valueOf(1));
        layeredPane.add(mapPanel, Integer.valueOf(0));
    }

    private void createEditorLayers() {
        glassPanel = new GlassPanel(editor);
        trafficPanel = new TrafficPanel(editor.getTraffic());
        scenarioPanel = new ScenarioPanel(editor.getScenario());
        mapPanel = new MapPanel(editor.getMap());
    }

    private void initEditorLayers() {
        // set the grid size for the traffic panel and the glass panel
        int rows = mapPanel.getGridRows();
        int cols = mapPanel.getGridCols();
        glassPanel.setGridSize(rows, cols);
        trafficPanel.setGridSize(rows, cols);
    }

    private void changeMapPanel() {
        layeredPane.remove(mapPanel);
        mapPanel = new MapPanel(editor.getMap());
        mapPanel.addSimlaneUIListener(simlaneUIListener);
        layeredPane.add(mapPanel, Integer.valueOf(0));
    }

    private void changeScenarioPanel() {
        layeredPane.remove(scenarioPanel);
        scenarioPanel = new ScenarioPanel(editor.getScenario());
        layeredPane.add(scenarioPanel, Integer.valueOf(1));
        // a scenario panel change requires the layers to be initialized
        initEditorLayers();
    }

    private Rectangle getLayerBounds() {
        Rectangle bounds = new Rectangle();
        int layeredPaneWidth = layeredPane.getWidth();
        int layeredPaneHeight = layeredPane.getHeight();
        int gridRows = mapPanel.getGridRows();
        int gridCols = mapPanel.getGridCols();
        double layeredPaneAspectRatio = layeredPaneWidth / (double) layeredPaneHeight;
        double gridAspectRatio = gridCols / (double) gridRows;
        int gridSize;
        if (gridAspectRatio >= layeredPaneAspectRatio) {
            gridSize = layeredPaneWidth / gridCols;
        } else {
            gridSize = layeredPaneHeight / gridRows;
        }
        bounds.width = gridSize * gridCols;
        bounds.height = gridSize * gridRows;
        bounds.x = (layeredPaneWidth - bounds.width) / 2;
        bounds.y = (layeredPaneHeight - bounds.height) / 2;
        return bounds;
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        simlaneUIListener = listener;
        // add listener to the area surrounding the map panel
        addMouseListener(listener);
        // add listener to the map panel
        mapPanel.addSimlaneUIListener(listener);
    }

    @Override
    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(Editor.SCENARIO_CHANGED_EVENT)) {
            EventQueue.invokeLater(() -> {
                changeMapPanel();
                changeScenarioPanel();
                revalidate();
                repaint();
            });
        } else if (event.getType().equals(Editor.MAP_CHANGED_EVENT)) {
            EventQueue.invokeLater(() -> {
                changeMapPanel();
                revalidate();
                repaint();
            });
        }
    }

    private class LayerLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
            // nothing
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            // nothing
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return SimlaneUI.PREFERRED_MAP_SIZE;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return SimlaneUI.MINIMUM_MAP_SIZE;
        }

        @Override
        public void layoutContainer(Container parent) {
            Rectangle bounds = getLayerBounds();
            int componentCount = parent.getComponentCount();
            for (int i = 0; i < componentCount; i++) {
                Component component = parent.getComponent(i);
                component.setBounds(bounds);
            }
        }
    }
}
