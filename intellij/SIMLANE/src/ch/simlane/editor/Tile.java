package ch.simlane.editor;

import ch.simlane.editor.event.ObservableStateObject;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.tme.components.TrafficLight;
import ch.simlane.ui.SimlaneUI;

import java.util.Objects;

public class Tile extends ObservableStateObject {

    public static final String LANE_STATE_CHANGED_EVENT = "Tile.LANE_STATE_CHANGED_EVENT";
    public static final String TRAFFIC_LIGHTS_CHANGED_EVENT = "Tile.TRAFFIC_LIGHTS_CHANGED_EVENT";

    public static final int LANE_STATE_UNSELECTED = 3000;
    public static final int LANE_STATE_SELECTED = 3001;
    public static final int LANE_STATE_DISABLED = 3002;

    public static final double TILE_LENGTH = 60; // m

    private static final double LANE_LENGTH_STRAIGHT = TILE_LENGTH; // m
    private static final double LANE_LENGTH_LEFT_TURN = 0.5 * (1 - SimlaneUI.LANE_SPACING) * TILE_LENGTH * Math.PI; // m
    private static final double LANE_LENGTH_RIGHT_TURN = 0.5 * SimlaneUI.LANE_SPACING * TILE_LENGTH * Math.PI; // m
    private static final double LANE_LENGTH_U_TURN = 2.0 * LANE_LENGTH_RIGHT_TURN; // m

    // the location of the tile on the map
    private TileLocation location;

    /**
     * The two dimensional lane arrays contain an entry for each lane on the tile.
     * They use the following format: array[from][to].
     * The different sides map onto the indices from 0 to 3 according to the Editor.ARRAY_INDEX_? constants.
     */
    private int[][] laneState;
    private TrafficLight[][] trafficLights;

    private boolean trafficLightsEnabled;

    public Tile(int row, int col) {
        location = new TileLocation(row, col);
        // initialize an empty tile with no lanes on it
        laneState = new int[4][4];
        trafficLights = new TrafficLight[4][4];
        initializeLaneState();
    }

    private void initializeLaneState() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    // Lanes that lead back to the same side where they started are disabled by default.
                    // This means cars can't make a U-turn on a single tile.
                    laneState[i][j] = LANE_STATE_DISABLED;
                } else {
                    laneState[i][j] = LANE_STATE_UNSELECTED;
                }
            }
        }
    }

    public boolean lanesIntersectByIndex(int indexFrom1, int indexTo1, int indexFrom2, int indexTo2) {
        // lane 1
        int sideFrom1 = Editor.getSideFromArrayIndex(indexFrom1);
        int sideTo1 = Editor.getSideFromArrayIndex(indexTo1);
        int laneType1 = Editor.getLaneType(sideFrom1, sideTo1);
        // lane 2
        int sideFrom2 = Editor.getSideFromArrayIndex(indexFrom2);
        int sideTo2 = Editor.getSideFromArrayIndex(indexTo2);
        int laneType2 = Editor.getLaneType(sideFrom2, sideTo2);
        // determine if lanes intersect
        if (sideFrom1 == sideFrom2 && sideTo1 == sideTo2) {
            throw new IllegalStateException("The specified lanes are identical.");
        }
        if (sideFrom1 == sideTo1 || sideFrom2 == sideTo2) {
            throw new UnsupportedOperationException("U-Turns are not yet supported by this method.");
        }
        if (sideFrom1 == sideFrom2) {
            // lanes with the same start side never intersect
            return false;
        }
        if (sideTo1 == sideTo2) {
            // lanes with the same end side always intersect
            return true;
        }
        if (laneType1 == Editor.LANE_TYPE_LEFT_TURN || laneType2 == Editor.LANE_TYPE_LEFT_TURN) {
            if (laneType1 != Editor.LANE_TYPE_RIGHT_TURN && laneType2 != Editor.LANE_TYPE_RIGHT_TURN) {
                return true;
            }
        }
        if (laneType1 == Editor.LANE_TYPE_STRAIGHT && laneType2 == Editor.LANE_TYPE_STRAIGHT) {
            if (Editor.getOppositeSide(sideFrom1) != sideFrom2) {
                return true;
            }
        }
        return false;
    }

    public double getLaneLengthByIndex(int indexFrom, int indexTo) {
        int sideFrom = Editor.getSideFromArrayIndex(indexFrom);
        int sideTo = Editor.getSideFromArrayIndex(indexTo);
        switch (Editor.getLaneType(sideFrom, sideTo)) {
            case Editor.LANE_TYPE_STRAIGHT:
                return LANE_LENGTH_STRAIGHT;
            case Editor.LANE_TYPE_LEFT_TURN:
                return LANE_LENGTH_LEFT_TURN;
            case Editor.LANE_TYPE_RIGHT_TURN:
                return LANE_LENGTH_RIGHT_TURN;
            case Editor.LANE_TYPE_U_TURN:
                return LANE_LENGTH_U_TURN;
            default:
                throw new IllegalStateException("Invalid lane type value.");
        }
    }

    public int getLaneState(int sideFrom, int sideTo) {
        int indexFrom = Editor.getArrayIndexFromSide(sideFrom);
        int indexTo = Editor.getArrayIndexFromSide(sideTo);
        return laneState[indexFrom][indexTo];
    }

    public int getLaneStateByIndex(int indexFrom, int indexTo) {
        return laneState[indexFrom][indexTo];
    }

    public int[][] getLaneState() {
        return laneState;
    }

    public void setLaneState(int state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (laneState[i][j] == LANE_STATE_DISABLED) {
                    continue;
                }
                laneState[i][j] = state;
            }
        }
        fireStateChange(new StateChangeEvent(LANE_STATE_CHANGED_EVENT));
    }

    public void setLaneState(int sideFrom, int sideTo, int state) {
        int indexFrom = Editor.getArrayIndexFromSide(sideFrom);
        int indexTo = Editor.getArrayIndexFromSide(sideTo);
        laneState[indexFrom][indexTo] = state;
        fireStateChange(new StateChangeEvent(LANE_STATE_CHANGED_EVENT));
    }

    public TrafficLight[][] getTrafficLights() {
        return trafficLights;
    }

    public void setTrafficLights(TrafficLight[][] trafficLights) {
        this.trafficLights = trafficLights;
        if (trafficLightsEnabled) {
            fireStateChange(new StateChangeEvent(TRAFFIC_LIGHTS_CHANGED_EVENT));
        }
    }

    public void clearTrafficLights() {
        trafficLights = new TrafficLight[4][4];
        fireStateChange(new StateChangeEvent(TRAFFIC_LIGHTS_CHANGED_EVENT));
    }

    public boolean isTrafficLightsEnabled() {
        return trafficLightsEnabled;
    }

    public void setTrafficLightsEnabled(boolean enabled) {
        this.trafficLightsEnabled = enabled;
        fireStateChange(new StateChangeEvent(TRAFFIC_LIGHTS_CHANGED_EVENT));
    }

    public int getRow() {
        return location.getRow();
    }

    public int getCol() {
        return location.getCol();
    }

    public TileLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Tile[location:" + location + "]";
    }

    public static class TileLocation {

        // the coordinates of the tile on the grid
        private int row;
        private int col;

        public TileLocation(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getSideOf(TileLocation location) {
            if (location.row == row - 1 && location.col == col) {
                return Editor.SIDE_TOP;
            }
            if (location.row == row && location.col == col - 1) {
                return Editor.SIDE_LEFT;
            }
            if (location.row == row && location.col == col + 1) {
                return Editor.SIDE_RIGHT;
            }
            if (location.row == row + 1 && location.col == col) {
                return Editor.SIDE_BOTTOM;
            }
            throw new IllegalStateException("The specified location is not adjacent to this location.");
        }

        public TileLocation getLocationOn(int side) {
            int row = this.row;
            int col = this.col;
            //noinspection DuplicatedCode
            switch (side) {
                case Editor.SIDE_TOP:
                    row--;
                    break;
                case Editor.SIDE_LEFT:
                    col--;
                    break;
                case Editor.SIDE_RIGHT:
                    col++;
                    break;
                case Editor.SIDE_BOTTOM:
                    row++;
                    break;
                default:
                    throw new IllegalArgumentException(side + " is not a valid side.");
            }
            return new TileLocation(row, col);
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TileLocation)) {
                return false;
            }
            TileLocation other = (TileLocation) o;
            return row == other.row && col == other.col;
        }

        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return "Location[row:" + row + ", col:" + col + "]";
        }
    }
}
