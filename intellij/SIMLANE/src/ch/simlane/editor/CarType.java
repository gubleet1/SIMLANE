package ch.simlane.editor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public enum CarType {

    GREEN(Constants.COLOR_GREEN_STRING, Constants.COLOR_GREEN),
    DARK_GREEN(Constants.COLOR_DARK_GREEN_STRING, Constants.COLOR_DARK_GREEN),
    BLUE(Constants.COLOR_BLUE_STRING, Constants.COLOR_BLUE),
    DARK_BLUE(Constants.COLOR_DARK_BLUE_STRING, Constants.COLOR_DARK_BLUE),
    YELLOW(Constants.COLOR_YELLOW_STRING, Constants.COLOR_YELLOW),
    ORANGE(Constants.COLOR_ORANGE_STRING, Constants.COLOR_ORANGE),
    RED(Constants.COLOR_RED_STRING, Constants.COLOR_RED),
    PURPLE(Constants.COLOR_PURPLE_STRING, Constants.COLOR_PURPLE);

    private static final Map<String, CarType> map = new HashMap<>();

    static {
        for (CarType carType : CarType.values()) {
            map.put(carType.getName(), carType);
        }
    }

    private String name;
    private Color color;

    CarType(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public static CarType get(String color) {
        return map.get(color);
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    private static class Constants {
        static final String COLOR_GREEN_STRING = "green";
        static final String COLOR_DARK_GREEN_STRING = "dark-green";
        static final String COLOR_BLUE_STRING = "blue";
        static final String COLOR_DARK_BLUE_STRING = "dark-blue";
        static final String COLOR_YELLOW_STRING = "yellow";
        static final String COLOR_ORANGE_STRING = "orange";
        static final String COLOR_RED_STRING = "red";
        static final String COLOR_PURPLE_STRING = "purple";

        static final Color COLOR_GREEN = new Color(0, 255, 0);
        static final Color COLOR_DARK_GREEN = new Color(0, 136, 0);
        static final Color COLOR_BLUE = new Color(51, 153, 255);
        static final Color COLOR_DARK_BLUE = new Color(0, 0, 255);
        static final Color COLOR_YELLOW = new Color(255, 255, 0);
        static final Color COLOR_ORANGE = new Color(255, 153, 0);
        static final Color COLOR_RED = new Color(255, 0, 51);
        static final Color COLOR_PURPLE = new Color(153, 0, 255);
    }
}
