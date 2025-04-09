package ch.simlane.ui.components;

import ch.simlane.editor.Editor;
import ch.simlane.editor.Tile;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.tme.components.TrafficLight;
import ch.simlane.ui.ComponentPainter;
import ch.simlane.ui.SimlaneUI;

import javax.swing.*;
import java.awt.*;

public class TilePanel extends JPanel implements StateChangeListener {

    private static final TrafficLight[] TRAFFIC_LIGHT_PAINT_ORDER = {
            TrafficLight.RED,
            TrafficLight.ORANGE,
            TrafficLight.GREEN
    };

    private Tile tile;

    public TilePanel(Tile tile) {
        this.tile = tile;
        initialize();
    }

    private void initialize() {
        tile.addStateChangeListener(this);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // paint the lanes
        paintLanes(graphics2D);
    }

    private void paintLanes(Graphics2D g) {
        double gridSize = getWidth();
        Stroke s = new BasicStroke(SimlaneUI.LANE_THICKNESS, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        Color c = ComponentPainter.LANE_SELECTED_COLOR;
        int[][] laneState = tile.getLaneState();
        TrafficLight[][] trafficLights = tile.getTrafficLights();
        int l = 1;
        if (tile.isTrafficLightsEnabled()) {
            l = TRAFFIC_LIGHT_PAINT_ORDER.length;
        }
        for (int k = 0; k < l; k++) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    // U-turn is not supported, so we don't draw the lane for it
                    if (i != j) {
                        if (laneState[i][j] == Tile.LANE_STATE_SELECTED) {
                            if (tile.isTrafficLightsEnabled()) {
                                if (trafficLights[i][j] == TRAFFIC_LIGHT_PAINT_ORDER[k]) {
                                    // set the color
                                    switch (trafficLights[i][j]) {
                                        case RED:
                                            c = Color.RED;
                                            break;
                                        case GREEN:
                                            c = Color.GREEN;
                                            break;
                                        case ORANGE:
                                            c = Color.ORANGE;
                                            break;
                                        default:
                                            throw new IllegalStateException("The traffic lights array contained an invalid value.");
                                    }
                                } else {
                                    continue;
                                }
                            }
                            // paint the lane
                            int sideFrom = Editor.getSideFromArrayIndex(i);
                            int sideTo = Editor.getSideFromArrayIndex(j);
                            ComponentPainter.paintLane(g, 0, 0, gridSize, sideFrom, sideTo, s, c);
                        }
                    }
                }
            }
        }
    }

    public Tile getTile() {
        return tile;
    }

    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(Tile.LANE_STATE_CHANGED_EVENT)) {
            repaint();
        }
        if (event.getType().equals(Tile.TRAFFIC_LIGHTS_CHANGED_EVENT)) {
            repaint();
        }
    }
}
