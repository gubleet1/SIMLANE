package ch.simlane.editor;

import ch.simlane.editor.event.ObservableStateObject;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.scenario.Scenario;
import ch.simlane.utils.JSONScenarios;

public class Editor extends ObservableStateObject {

    public static final int SIDE_TOP = 1000;
    public static final int SIDE_LEFT = 1001;
    public static final int SIDE_RIGHT = 1002;
    public static final int SIDE_BOTTOM = 1003;

    public static final int CORNER_TOP_LEFT = 2000;
    public static final int CORNER_TOP_RIGHT = 2001;
    public static final int CORNER_BOTTOM_LEFT = 2002;
    public static final int CORNER_BOTTOM_RIGHT = 2003;

    public static final int ARRAY_INDEX_TOP = 0;
    public static final int ARRAY_INDEX_LEFT = 1;
    public static final int ARRAY_INDEX_RIGHT = 2;
    public static final int ARRAY_INDEX_BOTTOM = 3;

    public static final int[] SIDES = {
            SIDE_TOP,
            SIDE_LEFT,
            SIDE_RIGHT,
            SIDE_BOTTOM
    };

    public static final int[] CORNER_AND_SIDE_ORDER = {
            CORNER_TOP_LEFT,
            SIDE_TOP,
            CORNER_TOP_RIGHT,
            SIDE_RIGHT,
            CORNER_BOTTOM_RIGHT,
            SIDE_BOTTOM,
            CORNER_BOTTOM_LEFT,
            SIDE_LEFT
    };

    public static final int LANE_TYPE_STRAIGHT = 13000;
    public static final int LANE_TYPE_LEFT_TURN = 13001;
    public static final int LANE_TYPE_RIGHT_TURN = 13002;
    public static final int LANE_TYPE_U_TURN = 13003;

    public static final String SCENARIO_CHANGED_EVENT = "Editor.SCENARIO_CHANGED_EVENT";
    public static final String MAP_CHANGED_EVENT = "Editor.MAP_CHANGED_EVENT";

    private static final String SCENARIOS_DEFINITION_FILE = "/simlane/scenarios.json";
    private static final String DEFAULT_SCENARIO_NAME = "simple";

    private static final String SIDE_TOP_STRING = "top";
    private static final String SIDE_LEFT_STRING = "left";
    private static final String SIDE_RIGHT_STRING = "right";
    private static final String SIDE_BOTTOM_STRING = "bottom";

    private Traffic traffic;
    private Scenario scenario;
    private Map map;

    private Tools tools;

    private JSONScenarios JSONScenarios;

    public Editor() {
        traffic = new Traffic();
        JSONScenarios = new JSONScenarios(SCENARIOS_DEFINITION_FILE);
        // loadScenario(String name) creates the scenario and a corresponding map
        loadScenario(DEFAULT_SCENARIO_NAME);
        tools = new Tools();
    }

    public static int convertToSide(String side) {
        switch (side) {
            case Editor.SIDE_TOP_STRING:
                return Editor.SIDE_TOP;
            case Editor.SIDE_LEFT_STRING:
                return Editor.SIDE_LEFT;
            case Editor.SIDE_RIGHT_STRING:
                return Editor.SIDE_RIGHT;
            case Editor.SIDE_BOTTOM_STRING:
                return Editor.SIDE_BOTTOM;
            default:
                throw new IllegalArgumentException("\"" + side + "\" is not a valid side.");
        }
    }

    public static String convertToString(int side) {
        switch (side) {
            case Editor.SIDE_TOP:
                return Editor.SIDE_TOP_STRING;
            case Editor.SIDE_LEFT:
                return Editor.SIDE_LEFT_STRING;
            case Editor.SIDE_RIGHT:
                return Editor.SIDE_RIGHT_STRING;
            case Editor.SIDE_BOTTOM:
                return Editor.SIDE_BOTTOM_STRING;
            default:
                throw new IllegalArgumentException(side + " is not a valid side.");
        }
    }

    public static boolean isValidSide(int side) {
        return side == SIDE_TOP || side == SIDE_LEFT || side == SIDE_RIGHT || side == SIDE_BOTTOM;
    }

    public static int getArrayIndexFromSide(int side) {
        switch (side) {
            case Editor.SIDE_TOP:
                return Editor.ARRAY_INDEX_TOP;
            case Editor.SIDE_LEFT:
                return Editor.ARRAY_INDEX_LEFT;
            case Editor.SIDE_RIGHT:
                return Editor.ARRAY_INDEX_RIGHT;
            case Editor.SIDE_BOTTOM:
                return Editor.ARRAY_INDEX_BOTTOM;
            default:
                throw new IllegalArgumentException(side + " is not a valid side.");
        }
    }

    public static int getSideFromArrayIndex(int index) {
        switch (index) {
            case Editor.ARRAY_INDEX_TOP:
                return Editor.SIDE_TOP;
            case Editor.ARRAY_INDEX_LEFT:
                return Editor.SIDE_LEFT;
            case Editor.ARRAY_INDEX_RIGHT:
                return Editor.SIDE_RIGHT;
            case Editor.ARRAY_INDEX_BOTTOM:
                return Editor.SIDE_BOTTOM;
            default:
                throw new IllegalArgumentException(index + " is not a valid side array index.");
        }
    }

    private static int getOrderIndexFromLocation(int location) {
        for (int i = 0; i < 8; i++) {
            if (location == CORNER_AND_SIDE_ORDER[i]) {
                return i;
            }
        }
        throw new IllegalArgumentException(location + " is not a valid location.");
    }

    public static int getOppositeSide(int side) {
        if (!isValidSide(side)) {
            throw new IllegalArgumentException(side + " is not a valid side.");
        }
        int sideOrderIndex = getOrderIndexFromLocation(side);
        return CORNER_AND_SIDE_ORDER[(sideOrderIndex + 4) % 8];
    }

    public static boolean isCornerBetween(int side1, int side2) {
        if (!isValidSide(side1) || !isValidSide(side2)) {
            throw new IllegalArgumentException("An invalid side was specified.");
        }
        int side1OrderIndex = getOrderIndexFromLocation(side1);
        int side2OrderIndex = getOrderIndexFromLocation(side2);
        return (side1OrderIndex + 2) % 8 == side2OrderIndex || (side2OrderIndex + 2) % 8 == side1OrderIndex;
    }

    public static int getCornerBetween(int side1, int side2) {
        if (!isValidSide(side1) || !isValidSide(side2)) {
            throw new IllegalArgumentException("An invalid side was specified.");
        }
        if (isCornerBetween(side1, side2)) {
            int side1OrderIndex = getOrderIndexFromLocation(side1);
            int side2OrderIndex = getOrderIndexFromLocation(side2);
            if ((side1OrderIndex + 2) % 8 == side2OrderIndex) {
                return CORNER_AND_SIDE_ORDER[(side1OrderIndex + 1) % 8];
            } else {
                return CORNER_AND_SIDE_ORDER[(side2OrderIndex + 1) % 8];
            }
        }
        throw new IllegalArgumentException("There is no corner between the two specified sides.");
    }

    public static boolean isArrangedClockwise(int side1, int side2) {
        if (!isValidSide(side1) || !isValidSide(side2)) {
            throw new IllegalArgumentException("An invalid side was specified.");
        }
        if (isCornerBetween(side1, side2)) {
            int side1OrderIndex = getOrderIndexFromLocation(side1);
            int side2OrderIndex = getOrderIndexFromLocation(side2);
            return ((side1OrderIndex + 2) % 8) == side2OrderIndex;
        }
        throw new IllegalArgumentException("There is no corner between the two specified sides.");
    }

    public static int getLaneType(int sideFrom, int sideTo) {
        if (Editor.isCornerBetween(sideFrom, sideTo)) {
            // 90 degree turn
            if (Editor.isArrangedClockwise(sideFrom, sideTo)) {
                // left turn (big)
                return LANE_TYPE_LEFT_TURN;
            } else {
                // right turn (small)
                return LANE_TYPE_RIGHT_TURN;
            }
        } else if (Editor.getOppositeSide(sideFrom) == sideTo) {
            // straight
            return LANE_TYPE_STRAIGHT;
        } else {
            // U-Turn
            return LANE_TYPE_U_TURN;
        }
    }

    public void loadScenario(String name) {
        if (scenario != null) {
            if (scenario.getName().equals(name)) {
                // the scenario is already loaded
                return;
            }
        }
        scenario = JSONScenarios.getScenario(name);
        // load an empty map with the size of the scenario
        map = new Map(scenario.getMapRows(), scenario.getMapCols());
        if (tools != null) {
            tools.getEditControls().getTileConfigurator().clearSelection();
        }
        fireStateChange(new StateChangeEvent(SCENARIO_CHANGED_EVENT));
    }

    public void loadMap(Map map) {
        this.map = map;
        if (tools != null) {
            tools.getEditControls().getTileConfigurator().clearSelection();
        }
        fireStateChange(new StateChangeEvent(MAP_CHANGED_EVENT));
    }

    public void setDisplaySimulationStateEnabled(boolean enabled) {
        traffic.setEnabled(enabled);
        map.getTiles().forEach(tile -> tile.setTrafficLightsEnabled(enabled));
    }

    public Traffic getTraffic() {
        return traffic;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public Map getMap() {
        return map;
    }

    public Tools getTools() {
        return tools;
    }

    public JSONScenarios getJSONScenarios() {
        return JSONScenarios;
    }
}
