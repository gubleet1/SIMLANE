package ch.simlane.ui.components;

import ch.simlane.editor.CarType;
import ch.simlane.editor.Editor;
import ch.simlane.editor.scenario.EndPoint;
import ch.simlane.editor.scenario.StartPoint;
import ch.simlane.ui.ComponentPainter;
import ch.simlane.ui.SimlaneUI;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;

public class ScenarioTilePanel extends JPanel {

    private static final Point2D[] INBOUND_CAR_INDICATOR_POSITIONS = {
            new Point2D.Double(0.15, 0.4),
            new Point2D.Double(0.4, 0.4),
            new Point2D.Double(0.15, 0.65),
            new Point2D.Double(0.4, 0.65)
    };

    private int row;
    private int col;
    private int side;

    private StartPoint startPoint;
    private EndPoint endPoint;

    public ScenarioTilePanel(StartPoint startPoint, EndPoint endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        initialize();
    }

    private void initialize() {
        setOpaque(false);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = getWidth();
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // set the clipping to respect the borders of the underlying grid
        Shape prevClip = graphics2D.getClip();
        graphics2D.clip(new Rectangle(1, 1, size - 2, size - 2));
        // paint the scenario tile
        paintScenarioTile(graphics2D);
        // restore the previous clip
        graphics2D.setClip(prevClip);
    }

    private void paintScenarioTile(Graphics2D g) {
        int width = getWidth();
        // backup the current transform
        AffineTransform t = g.getTransform();
        // apply the rotation transform to align the tile
        double anchor = width / 2.0;
        g.transform(getRotationTransform(anchor, anchor, false));
        // paint the scenario points
        if (startPoint != null) {
            paintStartPoint(g);
        }
        if (endPoint != null) {
            paintEndPoint(g);
        }
        // restore the previous transform
        g.setTransform(t);
    }

    private void paintStartPoint(Graphics2D g) {
        int size = getWidth();
        // paint the surrounding rectangle
        double x = size * 0.1;
        double y = size * 0.35;
        double w = size * 0.55;
        double h = size * 0.55;
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, size * 0.3, size * 0.3);
        g.setColor(Color.WHITE);
        g.fill(r);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(Color.BLACK);
        g.draw(r);
        // paint the lane leading into the map
        double x1 = size * 0.65;
        double y1 = size * (1 - SimlaneUI.LANE_SPACING);
        double x2 = size;
        double y2 = size * (1 - SimlaneUI.LANE_SPACING);
        Line2D l = new Line2D.Double(x1, y1, x2, y2);
        g.draw(l);
        // paint the arrow
        paintStartPointArrow(g);
        // paint the inbound car indicators
        paintInboundCarIndicators(g);
    }

    private void paintStartPointArrow(Graphics2D g) {
        int size = getWidth();
        // create the arrow glyph shape
        String arrow = "\u2B9E";
        Font font = new Font("Sans Serif", Font.PLAIN, 32);
        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, arrow);
        Shape s = getScaledGlyphShape(gv, size * 0.175);
        // make the arrow thinner
        s = AffineTransform.getScaleInstance(1, 0.7).createTransformedShape(s);
        // calculate the final position
        double x = size * 0.75;
        double y = size * 0.6;
        // move the arrow to the final position
        s = AffineTransform.getTranslateInstance(x, y).createTransformedShape(s);
        // paint the label
        g.setColor(Color.BLACK);
        g.fill(s);
    }

    private void paintInboundCarIndicators(Graphics2D g) {
        int size = getWidth();
        int i = 0;
        for (CarType carType : startPoint.getInboundTraffic().keySet()) {
            if (i == 4) {
                // we can't paint more than 4 inbound car indicators, stop painting
                break;
            }
            // get the inbound car indicator properties
            int count = startPoint.getInboundTraffic().get(carType);
            String label = String.valueOf(count);
            Point2D pos = INBOUND_CAR_INDICATOR_POSITIONS[i];
            double x = size * pos.getX();
            double y = size * pos.getY();
            double r = size * SimlaneUI.CAR_RADIUS;
            // the car indicator is painted by the traffic panel
            ComponentPainter.paintCarIndicator(g, x, y, r, carType.getColor());
            // paint the label
            if (r >= ComponentPainter.CAR_INDICATOR_MIN_RADIUS) {
                Color color = ComponentPainter.isColorDark(carType.getColor()) ? Color.WHITE : Color.BLACK;
                paintInboundCarIndicatorLabel(g, label, x, y, r * 2, color);
            }
            i++;
        }
    }

    private void paintInboundCarIndicatorLabel(Graphics2D g, String label, double posX, double posY, double size, Color c) {
        // create the glyph shape
        Font font = new Font("Monospaced", Font.BOLD, 32);
        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, label);
        Shape s = getScaledGlyphShape(gv, size * 0.7);
        // calculate the final position
        double w = s.getBounds2D().getWidth();
        double h = s.getBounds2D().getHeight();
        posX = posX + ((size - w) / 2.0);
        posY = posY + ((size - h) / 2.0);
        // calculate the anchor point for the counter rotation
        double anchorX = posX + (w / 2.0);
        double anchorY = posY + (h / 2.0);
        // perform the final translate and rotate transform
        AffineTransform t = getRotationTransform(anchorX, anchorY, true);
        t.translate(posX, posY);
        s = t.createTransformedShape(s);
        // paint the label
        g.setColor(c);
        g.fill(s);
    }

    private void paintEndPoint(Graphics2D g) {
        int size = getWidth();
        // paint the circle
        double x = size * ((1 - SimlaneUI.LANE_SPACING) - 0.15);
        double y = size * (SimlaneUI.LANE_SPACING - 0.15);
        double d = size * 0.3;
        Ellipse2D c = new Ellipse2D.Double(x, y, d, d);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(endPoint.getCarType().getColor());
        g.fill(c);
        g.setColor(Color.BLACK);
        g.draw(c);
        // paint the lane leading into the map
        double x1 = size * ((1 - SimlaneUI.LANE_SPACING) + 0.15);
        double y1 = size * SimlaneUI.LANE_SPACING;
        double x2 = size;
        double y2 = size * SimlaneUI.LANE_SPACING;
        Line2D l = new Line2D.Double(x1, y1, x2, y2);
        g.draw(l);
    }

    private Shape getScaledGlyphShape(GlyphVector gv, double size) {
        Shape s = gv.getOutline();
        // get the bounds of the created shape
        Rectangle2D bounds = s.getBounds2D();
        double x = bounds.getX();
        double y = bounds.getY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        // calculate the required scale to fill the available space
        double l = Math.max(w, h);
        double scale = size / l;
        // perform the scale transform
        AffineTransform t = new AffineTransform();
        t.scale(scale, scale);
        t.translate(x * -1, y * -1);
        return t.createTransformedShape(s);
    }

    private AffineTransform getRotationTransform(double anchorX, double anchorY, boolean inverted) {
        int rotation;
        switch (side) {
            case Editor.SIDE_TOP:
                rotation = 1;
                break;
            case Editor.SIDE_LEFT:
                return new AffineTransform();
            case Editor.SIDE_RIGHT:
                rotation = 2;
                break;
            case Editor.SIDE_BOTTOM:
                rotation = -1;
                break;
            default:
                throw new IllegalArgumentException(side + " is not a valid side.");
        }
        if (inverted) {
            rotation *= -1;
        }
        return AffineTransform.getQuadrantRotateInstance(rotation, anchorX, anchorY);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setSide(int side) {
        this.side = side;
    }
}
