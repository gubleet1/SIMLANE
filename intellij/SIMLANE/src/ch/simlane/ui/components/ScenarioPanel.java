package ch.simlane.ui.components;

import ch.simlane.editor.Editor;
import ch.simlane.editor.scenario.EndPoint;
import ch.simlane.editor.scenario.Scenario;
import ch.simlane.editor.scenario.StartPoint;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

import static ch.simlane.editor.scenario.ScenarioPoint.ScenarioPointLocation;

public class ScenarioPanel extends JPanel {

    private Scenario scenario;

    private List<ScenarioTilePanel> tiles;
    private int gridRows;
    private int gridCols;

    public ScenarioPanel(Scenario scenario) {
        this.scenario = scenario;
        initialize();
    }

    private void initialize() {
        gridRows = scenario.getMapRows() + 2;
        gridCols = scenario.getMapCols() + 2;
        setLayout(new ScenarioTileLayout());
        setOpaque(false);
        createScenarioTilePanels();
    }

    private void createScenarioTilePanels() {
        tiles = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            int side = Editor.getSideFromArrayIndex(i);
            int length = 0;
            switch (side) {
                case Editor.SIDE_TOP:
                case Editor.SIDE_BOTTOM:
                    length = scenario.getMapCols();
                    break;
                case Editor.SIDE_LEFT:
                case Editor.SIDE_RIGHT:
                    length = scenario.getMapRows();
                    break;
            }
            for (int pos = 0; pos < length; pos++) {
                ScenarioPointLocation location = new ScenarioPointLocation(side, pos);
                StartPoint startPoint = scenario.getStartPoint(location);
                EndPoint endPoint = scenario.getEndPoint(location);
                if (startPoint != null || endPoint != null) {
                    ScenarioTilePanel tile;
                    tile = new ScenarioTilePanel(startPoint, endPoint);
                    tile.setSide(side);
                    switch (side) {
                        case Editor.SIDE_TOP:
                            tile.setRow(0);
                            tile.setCol(pos + 1);
                            break;
                        case Editor.SIDE_LEFT:
                            tile.setRow(pos + 1);
                            tile.setCol(0);
                            break;
                        case Editor.SIDE_RIGHT:
                            tile.setRow(pos + 1);
                            tile.setCol(gridCols - 1);
                            break;
                        case Editor.SIDE_BOTTOM:
                            tile.setRow(gridRows - 1);
                            tile.setCol(pos + 1);
                            break;
                        default:
                            throw new IllegalArgumentException(i + " is not a valid side array index.");
                    }
                    tiles.add(tile);
                    add(tile);
                }
            }
        }
    }

    private class ScenarioTileLayout implements LayoutManager {

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
            return new Dimension(0, 0);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container parent) {
            int gridSize = parent.getWidth() / gridCols;
            int componentCount = parent.getComponentCount();
            for (int i = 0; i < componentCount; i++) {
                Component component = parent.getComponent(i);
                if (component instanceof ScenarioTilePanel) {
                    int row = ((ScenarioTilePanel) component).getRow();
                    int col = ((ScenarioTilePanel) component).getCol();
                    component.setBounds(col * gridSize, row * gridSize, gridSize, gridSize);
                }
            }
        }
    }
}
