package ch.simlane.ui.components.tools;

import ch.simlane.editor.Editor;
import ch.simlane.editor.Tile;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.editor.tools.TileConfigurator;
import ch.simlane.ui.ComponentPainter;
import ch.simlane.ui.SimlaneUI;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

public class TileConfiguratorPanel extends JPanel implements StateChangeListener {

    private static final Dimension PREFERRED_SIZE = new Dimension(200, 200);

    private static final int[] LANE_STATE_PAINT_ORDER = {
            Tile.LANE_STATE_DISABLED,
            Tile.LANE_STATE_UNSELECTED,
            TileConfigurator.LANE_STATE_PARTIALLY_SELECTED,
            Tile.LANE_STATE_SELECTED
    };

    private static final int DRAW_AREA_MARGIN = 10;
    private static final double GRID_INSET = 0.1;

    private TileConfigurator tileConfigurator;

    private LaneCheckBox[][] laneCheckBoxes;
    private JLabel emptySelectionLabel;

    public TileConfiguratorPanel(TileConfigurator tileConfigurator) {
        this.tileConfigurator = tileConfigurator;
        laneCheckBoxes = new LaneCheckBox[4][4];
        initialize();
    }

    private void initialize() {
        setLayout(new TileConfiguratorLayout());
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    int sideFrom = Editor.getSideFromArrayIndex(i);
                    int sideTo = Editor.getSideFromArrayIndex(j);
                    laneCheckBoxes[i][j] = new LaneCheckBox(sideFrom, sideTo);
                    add(laneCheckBoxes[i][j]);
                }
            }
        }
        emptySelectionLabel = new JLabel("no tile selected");
        emptySelectionLabel.setFont(new Font("Arial", Font.BOLD, 15));
        emptySelectionLabel.setForeground(Color.RED);
        add(emptySelectionLabel);
        tileConfigurator.addStateChangeListener(this);
        updateLaneCheckBoxes();
    }

    private void updateLaneCheckBoxes() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    int state = tileConfigurator.getLaneStateByIndex(i, j);
                    if (state == Tile.LANE_STATE_DISABLED) {
                        laneCheckBoxes[i][j].setEnabled(false);
                        laneCheckBoxes[i][j].setSelected(false);
                        continue;
                    }
                    laneCheckBoxes[i][j].setEnabled(true);
                    if (state == Tile.LANE_STATE_SELECTED) {
                        laneCheckBoxes[i][j].setSelected(true);
                        continue;
                    }
                    laneCheckBoxes[i][j].setSelected(false);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (!isSelectionEmpty()) {
            // paint the lanes
            paintLanes(graphics2D);
        }
        // paint the grid section
        paintGridSection(graphics2D);
    }

    private void paintLanes(Graphics2D g) {
        int size = getWidth() - (2 * DRAW_AREA_MARGIN);
        double gridSize = size / (1.0 + (2 * GRID_INSET));
        double x = (GRID_INSET * gridSize) + DRAW_AREA_MARGIN;
        double y = (GRID_INSET * gridSize) + DRAW_AREA_MARGIN;
        Stroke strokeSolid = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        Stroke strokeDashed = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[]{10.0f, 3.0f}, 0.0f);
        int[][] laneState = tileConfigurator.getLaneState();
        for (int k = 0; k < LANE_STATE_PAINT_ORDER.length; k++) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    // U-turn is not supported, so we don't draw the lane for it
                    if (i != j) {
                        if (laneState[i][j] == LANE_STATE_PAINT_ORDER[k]) {
                            int sideFrom = Editor.getSideFromArrayIndex(i);
                            int sideTo = Editor.getSideFromArrayIndex(j);
                            Stroke s;
                            Color c;
                            switch (laneState[i][j]) {
                                case Tile.LANE_STATE_UNSELECTED:
                                    s = strokeDashed;
                                    c = ComponentPainter.LANE_UNSELECTED_COLOR;
                                    break;
                                case Tile.LANE_STATE_SELECTED:
                                    s = strokeSolid;
                                    c = ComponentPainter.LANE_SELECTED_COLOR;
                                    break;
                                case Tile.LANE_STATE_DISABLED:
                                    // disabled lanes are not yet supported (except for U-turn lanes)
                                    throw new IllegalStateException("Disabled lanes are not yet supported.");
                                case TileConfigurator.LANE_STATE_PARTIALLY_SELECTED:
                                    s = strokeSolid;
                                    c = ComponentPainter.LANE_PARTIALLY_SELECTED_COLOR;
                                    break;
                                default:
                                    throw new IllegalStateException("The lane state array contained an invalid state.");
                            }
                            // paint the lane
                            ComponentPainter.paintLane(g, x, y, gridSize, sideFrom, sideTo, s, c);
                        }
                    }
                }
            }
        }
    }

    private void paintGridSection(Graphics2D g) {
        // the map tile borders (grid) are drawn with pixel precision (no antialiasing)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (int side : Editor.SIDES) {
            paintGridSectionSide(g, side);
        }
        // turn antialiasing back on
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void paintGridSectionSide(Graphics2D g, int side) {
        int size = getWidth() - (2 * DRAW_AREA_MARGIN);
        double gridSize = size / (1.0 + (2 * GRID_INSET));
        // top left corner of the grid section
        double x1 = 0.0;
        double y1 = 0.0;
        // bottom right corner of the grid section
        double x2 = (1.0 + (2 * GRID_INSET));
        double y2 = (1.0 + (2 * GRID_INSET));
        switch (side) {
            case Editor.SIDE_TOP:
                y1 += GRID_INSET;
                y2 = y1;
                break;
            case Editor.SIDE_LEFT:
                x1 += GRID_INSET;
                x2 = x1;
                break;
            case Editor.SIDE_RIGHT:
                x1 += (1.0 + GRID_INSET);
                x2 = x1;
                break;
            case Editor.SIDE_BOTTOM:
                y1 += (1.0 + GRID_INSET);
                y2 = y1;
                break;
            default:
                throw new IllegalArgumentException(side + " is not a valid side.");
        }
        // scale and translate the coordinates
        x1 = (x1 * gridSize) + DRAW_AREA_MARGIN;
        y1 = (y1 * gridSize) + DRAW_AREA_MARGIN;
        x2 = (x2 * gridSize) + DRAW_AREA_MARGIN;
        y2 = (y2 * gridSize) + DRAW_AREA_MARGIN;
        // create and draw the border
        Line2D border = new Line2D.Double(x1, y1, x2, y2);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(Color.DARK_GRAY);
        g.draw(border);
    }

    @Override
    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(TileConfigurator.LANE_STATE_CHANGED_EVENT)) {
            EventQueue.invokeLater(() -> {
                updateLaneCheckBoxes();
                repaint();
            });
        } else if (event.getType().equals(TileConfigurator.TILE_SELECTION_CHANGED_EVENT)) {
            revalidate();
            repaint();
        }
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    laneCheckBoxes[i][j].addActionListener(listener);
                }
            }
        }
    }

    private boolean isSelectionEmpty() {
        return tileConfigurator.getSelectedTiles().isEmpty();
    }

    private Rectangle getLaneCheckboxBounds(LaneCheckBox laneCheckbox) {
        int size = getWidth() - (2 * DRAW_AREA_MARGIN);
        double gridSize = size / (1.0 + (2 * GRID_INSET));
        int sideFrom = laneCheckbox.getSideFrom();
        int sideTo = laneCheckbox.getSideTo();
        double x = GRID_INSET;
        double y = GRID_INSET;
        if (Editor.isCornerBetween(sideFrom, sideTo)) {
            // 90 degree turn
            int corner = Editor.getCornerBetween(sideFrom, sideTo);
            double offset;
            if (Editor.isArrangedClockwise(sideFrom, sideTo)) {
                // left turn (big)
                offset = (1 - SimlaneUI.LANE_SPACING) * 0.75;
            } else {
                // right turn (small)
                offset = SimlaneUI.LANE_SPACING * 0.5;
            }
            switch (corner) {
                case Editor.CORNER_TOP_LEFT:
                    x += offset;
                    y += offset;
                    break;
                case Editor.CORNER_TOP_RIGHT:
                    x = x + 1 - offset;
                    y += offset;
                    break;
                case Editor.CORNER_BOTTOM_LEFT:
                    x += offset;
                    y = y + 1 - offset;
                    break;
                case Editor.CORNER_BOTTOM_RIGHT:
                    x = x + 1 - offset;
                    y = y + 1 - offset;
                    break;
                default:
                    throw new IllegalArgumentException(corner + " is not a valid corner.");
            }
        } else if (Editor.getOppositeSide(sideFrom) == sideTo) {
            // straight
            double offset = SimlaneUI.LANE_SPACING * 0.725;
            switch (sideFrom) {
                case Editor.SIDE_TOP:
                    x += offset;
                    y += 0.5;
                    break;
                case Editor.SIDE_LEFT:
                    x += 0.5;
                    y = y + 1 - offset;
                    break;
                case Editor.SIDE_RIGHT:
                    x += 0.5;
                    y += offset;
                    break;
                case Editor.SIDE_BOTTOM:
                    x = x + 1 - offset;
                    y += 0.5;
                    break;
                default:
                    throw new IllegalArgumentException(sideFrom + " is not a valid side.");
            }
        } else {
            // U-turn (not supported)
            throw new IllegalStateException("The specified lane checkbox has an illegal sideFrom / sideTo value combination.");
        }
        Dimension d = laneCheckbox.getPreferredSize();
        x = Math.round((x * gridSize) - (d.width / 2.0)) + DRAW_AREA_MARGIN;
        y = Math.round((y * gridSize) - (d.height / 2.0)) + DRAW_AREA_MARGIN;
        return new Rectangle((int) x, (int) y, d.width, d.height);
    }

    public boolean adjustSize(int size) {
        if (size != getPreferredSize().height) {
            setPreferredSize(new Dimension(0, size));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, size));
            revalidate();
            return true;
        }
        return false;
    }

    public static class LaneCheckBox extends JCheckBox {

        private int sideFrom;
        private int sideTo;

        private LaneCheckBox(int sideFrom, int sideTo) {
            setOpaque(false);
            this.sideFrom = sideFrom;
            this.sideTo = sideTo;
        }

        public int getSideFrom() {
            return sideFrom;
        }

        public int getSideTo() {
            return sideTo;
        }
    }

    private class TileConfiguratorLayout implements LayoutManager {

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
            return PREFERRED_SIZE;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container parent) {
            int size = parent.getWidth();
            int componentCount = parent.getComponentCount();
            for (int i = 0; i < componentCount; i++) {
                Component component = parent.getComponent(i);
                if (component instanceof LaneCheckBox) {
                    // position the lane checkboxes
                    if (isSelectionEmpty()) {
                        if (component.isVisible()) {
                            component.setVisible(false);
                            component.setBounds(0, 0, 0, 0);
                        }
                        continue;
                    }
                    if (!component.isVisible()) {
                        component.setVisible(true);
                    }
                    Rectangle bounds = getLaneCheckboxBounds((LaneCheckBox) component);
                    component.setBounds(bounds);
                } else if (component.equals(emptySelectionLabel)) {
                    // position the empty selection label
                    if (!isSelectionEmpty()) {
                        if (component.isVisible()) {
                            component.setVisible(false);
                            component.setBounds(0, 0, 0, 0);
                        }
                        continue;
                    }
                    if (!component.isVisible()) {
                        component.setVisible(true);
                    }
                    Dimension d = component.getPreferredSize();
                    int x = (int) Math.round((size - d.width) / 2.0);
                    int y = (int) Math.round((size - d.height) / 2.0);
                    Rectangle bounds = new Rectangle(x, y, d.width, d.height);
                    component.setBounds(bounds);
                }
            }
        }
    }
}
