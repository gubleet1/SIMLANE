package ch.simlane.ui.components;

import ch.simlane.editor.CarIndicator;
import ch.simlane.editor.Tile;
import ch.simlane.editor.Traffic;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.tme.Engine;
import ch.simlane.ui.ComponentPainter;

import javax.swing.*;
import java.awt.*;

public class TrafficPanel extends JPanel implements StateChangeListener {

    private Traffic traffic;

    private int gridRows;
    private int gridCols;

    public TrafficPanel(Traffic traffic) {
        this.traffic = traffic;
        initialize();
    }

    public void setGridSize(int rows, int cols) {
        gridRows = rows;
        gridCols = cols;
    }

    private void initialize() {
        traffic.addStateChangeListener(this);
        setOpaque(false);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (traffic.isEnabled()) {
            paintTraffic(graphics2D);
        }
    }

    private void paintTraffic(Graphics2D g) {
        int gridSize = getWidth() / gridCols;
        // paint all car indicators
        for (CarIndicator carIndicator : traffic.getTraffic()) {
            // calculate the car indicator position
            double x = (carIndicator.getX() + 1.0) * gridSize;
            double y = (carIndicator.getY() + 1.0) * gridSize;
            double radius = gridSize * ((Engine.CAR_LENGTH / 2) / Tile.TILE_LENGTH);
            Color color = carIndicator.getType().getColor();
            // paint the car indicator
            ComponentPainter.paintCarIndicator(g, x - radius, y - radius, radius, color);
        }
    }

    public void stateChange(StateChangeEvent event) {
        repaint();
    }
}
