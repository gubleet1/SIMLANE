package ch.simlane.ui.components;

import ch.simlane.editor.Editor;
import ch.simlane.editor.Map;
import ch.simlane.editor.Tile;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {

    private Map map;

    private TilePanel[][] tiles;
    private int rows;
    private int cols;

    public MapPanel(Map map) {
        this.map = map;
        initialize();
    }

    public int getGridRows() {
        return rows + 2;
    }

    public int getGridCols() {
        return cols + 2;
    }

    private void initialize() {
        rows = map.getNumRows();
        cols = map.getNumCols();
        setLayout(new GridLayout(rows + 2, cols + 2));
        createTilePanels();
    }

    private void createTilePanels() {
        tiles = new TilePanel[rows][cols];
        for (int i = 0; i < cols + 2; i++) {
            if (i == 0) {
                add(new BorderTilePanel(Editor.CORNER_TOP_LEFT));
            } else if (i == cols + 1) {
                add(new BorderTilePanel(Editor.CORNER_TOP_RIGHT));
            } else {
                add(new BorderTilePanel(Editor.SIDE_TOP));
            }
        }
        for (int i = 0; i < rows; i++) {
            add(new BorderTilePanel(Editor.SIDE_LEFT));
            for (int j = 0; j < cols; j++) {
                Tile tile = map.getTile(i, j);
                tiles[i][j] = new TilePanel(tile);
                add(tiles[i][j]);
            }
            add(new BorderTilePanel(Editor.SIDE_RIGHT));
        }
        for (int i = 0; i < cols + 2; i++) {
            if (i == 0) {
                add(new BorderTilePanel(Editor.CORNER_BOTTOM_LEFT));
            } else if (i == cols + 1) {
                add(new BorderTilePanel(Editor.CORNER_BOTTOM_RIGHT));
            } else {
                add(new BorderTilePanel(Editor.SIDE_BOTTOM));
            }
        }
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        // add mouse listener to the map area
        addMouseListener(listener);
        // add mouse listener to each map tile
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                tiles[i][j].addMouseListener(listener);
            }
        }
    }

    static private class BorderTilePanel extends JPanel {

        private int location;
        private JPanel borderPanel;

        BorderTilePanel(int location) {
            this.location = location;
            initialize();
        }

        private void initialize() {
            createBorderPanel();
            switch (location) {
                case Editor.SIDE_TOP:
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    add(Box.createVerticalGlue());
                    add(borderPanel);
                    break;
                case Editor.SIDE_LEFT:
                case Editor.CORNER_TOP_LEFT:
                case Editor.CORNER_BOTTOM_LEFT:
                    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                    add(Box.createHorizontalGlue());
                    add(borderPanel);
                    break;
                case Editor.SIDE_RIGHT:
                case Editor.CORNER_TOP_RIGHT:
                case Editor.CORNER_BOTTOM_RIGHT:
                    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                    add(borderPanel);
                    add(Box.createHorizontalGlue());
                    break;
                case Editor.SIDE_BOTTOM:
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    add(borderPanel);
                    add(Box.createVerticalGlue());
                    break;
                default:
                    throw new IllegalArgumentException(location + " is not a valid location.");
            }
        }

        private void createBorderPanel() {
            borderPanel = new JPanel() {
                @Override
                public Dimension getPreferredSize() {
                    return getDimension();
                }

                @Override
                public Dimension getMaximumSize() {
                    return getDimension();
                }

                private Dimension getDimension() {
                    int width = BorderTilePanel.this.getWidth();
                    int height = BorderTilePanel.this.getHeight();
                    switch (location) {
                        case Editor.SIDE_TOP:
                        case Editor.SIDE_BOTTOM:
                            height /= 4;
                            break;
                        case Editor.SIDE_LEFT:
                        case Editor.SIDE_RIGHT:
                            width /= 4;
                            break;
                        case Editor.CORNER_TOP_LEFT:
                        case Editor.CORNER_TOP_RIGHT:
                        case Editor.CORNER_BOTTOM_LEFT:
                        case Editor.CORNER_BOTTOM_RIGHT:
                            height /= 4;
                            width /= 4;
                            break;
                        default:
                            throw new IllegalArgumentException(location + " is not a valid location.");
                    }
                    return new Dimension(width, height);
                }
            };
            int top = 1;
            int left = 1;
            int right = 1;
            int bottom = 1;
            switch (location) {
                case Editor.SIDE_TOP:
                    top = 0;
                    break;
                case Editor.SIDE_LEFT:
                    left = 0;
                    break;
                case Editor.SIDE_RIGHT:
                    right = 0;
                    break;
                case Editor.SIDE_BOTTOM:
                    bottom = 0;
                    break;
                case Editor.CORNER_TOP_LEFT:
                    top = 0;
                    left = 0;
                    borderPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
                    break;
                case Editor.CORNER_TOP_RIGHT:
                    top = 0;
                    right = 0;
                    borderPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
                    break;
                case Editor.CORNER_BOTTOM_LEFT:
                    left = 0;
                    bottom = 0;
                    borderPanel.setAlignmentY(Component.TOP_ALIGNMENT);
                    break;
                case Editor.CORNER_BOTTOM_RIGHT:
                    right = 0;
                    bottom = 0;
                    borderPanel.setAlignmentY(Component.TOP_ALIGNMENT);
                    break;
                default:
                    throw new IllegalArgumentException(location + " is not a valid location.");
            }
            borderPanel.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.DARK_GRAY));
        }
    }
}
