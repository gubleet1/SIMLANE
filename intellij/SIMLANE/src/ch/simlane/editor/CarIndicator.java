package ch.simlane.editor;

public class CarIndicator {

    // the top left corner of the top left most map tile is (0, 0) the bottom right corner of this tile is (1, 1)
    private double x;
    private double y;
    private CarType type;

    public CarIndicator(double x, double y, CarType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public CarType getType() {
        return type;
    }
}
