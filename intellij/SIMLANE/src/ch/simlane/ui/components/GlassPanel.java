package ch.simlane.ui.components;

import ch.simlane.editor.Editor;
import ch.simlane.editor.Tile;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.editor.tools.TileConfigurator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

public class GlassPanel extends JPanel implements StateChangeListener {

    private Editor editor;
    private TileConfigurator tileConfigurator;

    private int gridRows;
    private int gridCols;

    public GlassPanel(Editor editor) {
        this.editor = editor;
        tileConfigurator = editor.getTools().getEditControls().getTileConfigurator();
        initialize();
    }

    public void setGridSize(int rows, int cols) {
        gridRows = rows;
        gridCols = cols;
    }

    private void initialize() {
        // listen for tile selection changed events from the tile configurator
        tileConfigurator.addStateChangeListener(this);
        setOpaque(false);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintSelectedTilesBorder(graphics2D);
    }

    private void paintSelectedTilesBorder(Graphics2D g) {
        // the map tile borders (grid) are drawn with pixel precision (no antialiasing)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (Tile tile : tileConfigurator.getSelectedTiles()) {
            paintTileBorder(g, tile);
        }
        // turn antialiasing back on
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void paintTileBorder(Graphics2D g, Tile tile) {
        for (int side : Editor.SIDES) {
            Tile neighbour = editor.getMap().getNeighbourOf(tile, side);
            if (neighbour != null) {
                if (tileConfigurator.isSelected(neighbour)) {
                    // the neighbour on the current side is also selected
                    // don't paint this side of the border
                    continue;
                }
            }
            paintTileBorderSide(g, tile, side);
        }
    }

    private void paintTileBorderSide(Graphics2D g, Tile tile, int side) {
        int gridSize = getWidth() / gridCols;
        // get the map coordinates of the tile
        int row = tile.getRow();
        int col = tile.getCol();
        // top left corner of the tile on the grid
        int x1 = col + 1;
        int y1 = row + 1;
        // bottom right corner of the tile on the grid
        int x2 = col + 2;
        int y2 = row + 2;
        //noinspection DuplicatedCode
        switch (side) {
            case Editor.SIDE_TOP:
                y2--;
                break;
            case Editor.SIDE_LEFT:
                x2--;
                break;
            case Editor.SIDE_RIGHT:
                x1++;
                break;
            case Editor.SIDE_BOTTOM:
                y1++;
                break;
            default:
                throw new IllegalArgumentException(side + " is not a valid side.");
        }
        // transform grid coordinates to pixel coordinates
        x1 *= gridSize;
        y1 *= gridSize;
        x2 *= gridSize;
        y2 *= gridSize;
        // create and draw the border
        Line2D border = new Line2D.Double(x1, y1, x2, y2);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g.setColor(Color.WHITE);
        g.draw(border);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                10.0f, new float[]{10.0f, 15.0f}, 0.0f));
        g.setColor(Color.BLUE);
        g.draw(border);
    }

    @Override
    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(TileConfigurator.TILE_SELECTION_CHANGED_EVENT)) {
            repaint();
        }
    }
}
