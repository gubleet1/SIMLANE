package ch.simlane.ui;

import ch.simlane.editor.Editor;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class ComponentPainter {

    public static final int CAR_INDICATOR_MIN_RADIUS = 6;

    public static final Color LANE_UNSELECTED_COLOR = Color.LIGHT_GRAY;
    public static final Color LANE_SELECTED_COLOR = Color.MAGENTA;
    public static final Color LANE_PARTIALLY_SELECTED_COLOR = new Color(255, 153, 255);

    public static boolean isColorDark(Color color) {
        int brightness = (int) Math.sqrt(color.getRed() * color.getRed() * 0.241 +
                color.getGreen() * color.getGreen() * 0.691 +
                color.getBlue() * color.getBlue() * 0.068
        );
        return brightness < 130;
    }

    public static void paintCarIndicator(Graphics2D g, double x, double y, double radius, Color color) {
        Shape c = new Ellipse2D.Double(x, y, radius * 2.0, radius * 2.0);
        g.setColor(color);
        g.fill(c);
        if (radius >= CAR_INDICATOR_MIN_RADIUS) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
            g.draw(c);
        }
    }

    // x, y define the coordinates of the top left corner of the tile within the graphics object g
    // girdSize defines the length of the side of a tile within the graphics object g
    public static void paintLane(Graphics2D g, double x, double y, double gridSize,
                                 int sideFrom, int sideTo, Stroke s, Color c) {
        if (Editor.isCornerBetween(sideFrom, sideTo)) {
            // 90 degree turn
            paintCurvedLane(g, x, y, gridSize, sideFrom, sideTo, s, c);
        } else if (Editor.getOppositeSide(sideFrom) == sideTo) {
            // straight
            paintStraightLane(g, x, y, gridSize, sideFrom, s, c);
        } else {
            // U-Turn (not supported)
            throw new IllegalArgumentException("An illegal sideFrom / sideTo value combination was specified.");
        }
    }

    // x, y define the coordinates of the top left corner of the tile within the graphics object g
    // girdSize defines the length of the side of a tile within the graphics object g
    private static void paintCurvedLane(Graphics2D g, double x, double y, double gridSize,
                                        int sideFrom, int sideTo, Stroke s, Color c) {
        double radius;
        if (Editor.isArrangedClockwise(sideFrom, sideTo)) {
            // left turn (big)
            radius = 1 - SimlaneUI.LANE_SPACING;
        } else {
            // right turn (small)
            radius = SimlaneUI.LANE_SPACING;
        }
        double posX = 0.0;
        double posY = 0.0;
        double size = (radius * 2);
        double angleStart;
        int corner = Editor.getCornerBetween(sideFrom, sideTo);
        switch (corner) {
            case Editor.CORNER_TOP_LEFT:
                posX -= radius;
                posY -= radius;
                angleStart = 270.0;
                break;
            case Editor.CORNER_TOP_RIGHT:
                posX = 1 - radius;
                posY -= radius;
                angleStart = 180.0;
                break;
            case Editor.CORNER_BOTTOM_LEFT:
                posX -= radius;
                posY = 1 - radius;
                angleStart = 0.0;
                break;
            case Editor.CORNER_BOTTOM_RIGHT:
                posX = 1 - radius;
                posY = 1 - radius;
                angleStart = 90.0;
                break;
            default:
                throw new IllegalArgumentException(corner + " is not a valid corner.");
        }
        // scale and translate the coordinates
        posX = (posX * gridSize) + x;
        posY = (posY * gridSize) + y;
        size *= gridSize;
        // create and paint the arc
        Arc2D arc = new Arc2D.Double(posX, posY, size, size, angleStart, 90.0, Arc2D.OPEN);
        g.setStroke(s);
        g.setColor(c);
        g.draw(arc);
    }

    // x, y define the coordinates of the top left corner of the tile within the graphics object g
    // girdSize defines the length of the side of a tile within the graphics object g
    private static void paintStraightLane(Graphics2D g, double x, double y, double gridSize,
                                          int sideFrom, Stroke s, Color c) {
        double x1 = 0.0;
        double y1 = 0.0;
        double x2 = 1.0;
        double y2 = 1.0;
        switch (sideFrom) {
            case Editor.SIDE_TOP:
                x1 = SimlaneUI.LANE_SPACING;
                x2 = SimlaneUI.LANE_SPACING;
                break;
            case Editor.SIDE_LEFT:
                y1 = 1 - SimlaneUI.LANE_SPACING;
                y2 = 1 - SimlaneUI.LANE_SPACING;
                break;
            case Editor.SIDE_RIGHT:
                y1 = SimlaneUI.LANE_SPACING;
                y2 = SimlaneUI.LANE_SPACING;
                break;
            case Editor.SIDE_BOTTOM:
                x1 = 1 - SimlaneUI.LANE_SPACING;
                x2 = 1 - SimlaneUI.LANE_SPACING;
                break;
            default:
                throw new IllegalArgumentException(sideFrom + " is not a valid side.");
        }
        // scale and translate the coordinates
        x1 = (x1 * gridSize) + x;
        y1 = (y1 * gridSize) + y;
        x2 = (x2 * gridSize) + x;
        y2 = (y2 * gridSize) + y;
        // create and paint the line
        Line2D line = new Line2D.Double(x1, y1, x2, y2);
        g.setStroke(s);
        g.setColor(c);
        g.draw(line);
    }
}
