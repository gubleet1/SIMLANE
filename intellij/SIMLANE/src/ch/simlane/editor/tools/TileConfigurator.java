package ch.simlane.editor.tools;

import ch.simlane.editor.Editor;
import ch.simlane.editor.Tile;
import ch.simlane.editor.event.ObservableStateObject;
import ch.simlane.editor.event.StateChangeEvent;

import java.util.LinkedList;
import java.util.List;

public class TileConfigurator extends ObservableStateObject {

    public static final String TILE_SELECTION_CHANGED_EVENT = "TileConfigurator.TILE_SELECTION_CHANGED_EVENT";
    public static final String LANE_STATE_CHANGED_EVENT = "TileConfigurator.LANE_STATE_CHANGED_EVENT";

    public static final int LANE_STATE_PARTIALLY_SELECTED = 3003;
    private static final int LANE_STATE_UNDEFINED = 3004;

    // the currently selected tiles
    private List<Tile> selectedTiles;
    // representation of the lanes on all selected tiles using LANE_STATE_? constants
    private int[][] laneState;

    public TileConfigurator() {
        selectedTiles = new LinkedList<>();
        // initialize the laneState array
        laneState = new int[4][4];
        clearLaneState();
    }

    public void clearSelection() {
        if (selectedTiles.isEmpty()) {
            return;
        }
        selectedTiles.clear();
        // update the lane state
        updateLaneState();
        fireStateChange(new StateChangeEvent(TILE_SELECTION_CHANGED_EVENT));
    }

    public void setSelectedTile(Tile tile) {
        selectedTiles.clear();
        selectedTiles.add(tile);
        // update the lane state
        updateLaneState();
        fireStateChange(new StateChangeEvent(TILE_SELECTION_CHANGED_EVENT));
    }

    public void addSelectedTile(Tile tile) {
        if (selectedTiles.contains(tile)) {
            return;
        }
        selectedTiles.add(tile);
        // add the tile to the lane state
        if (addToLaneState(tile)) {
            fireStateChange(new StateChangeEvent(LANE_STATE_CHANGED_EVENT));
        }
        fireStateChange(new StateChangeEvent(TILE_SELECTION_CHANGED_EVENT));
    }

    public void removeSelectedTile(Tile tile) {
        if (selectedTiles.contains(tile)) {
            selectedTiles.remove(tile);
            // update the lane state
            updateLaneState();
            fireStateChange(new StateChangeEvent(TILE_SELECTION_CHANGED_EVENT));
        }
    }

    public List<Tile> getSelectedTiles() {
        return selectedTiles;
    }

    public boolean isSelected(Tile tile) {
        return selectedTiles.contains(tile);
    }

    public int[][] getLaneState() {
        return laneState;
    }

    public void setLaneState(int state) {
        for (Tile tile : selectedTiles) {
            tile.setLaneState(state);
        }
        updateLaneState();
    }

    public int getLaneStateByIndex(int indexFrom, int indexTo) {
        return laneState[indexFrom][indexTo];
    }

    public void setLaneState(int sideFrom, int sideTo, int state) {
        int indexFrom = Editor.getArrayIndexFromSide(sideFrom);
        int indexTo = Editor.getArrayIndexFromSide(sideTo);
        if (laneState[indexFrom][indexTo] == state) {
            return;
        }
        for (Tile tile : selectedTiles) {
            if (tile.getLaneState(sideFrom, sideTo) == Tile.LANE_STATE_DISABLED) {
                continue;
            }
            tile.setLaneState(sideFrom, sideTo, state);
        }
        laneState[indexFrom][indexTo] = state;
        fireStateChange(new StateChangeEvent(LANE_STATE_CHANGED_EVENT));
    }

    public void updateLaneState() {
        clearLaneState();
        for (Tile tile : selectedTiles) {
            addToLaneState(tile);
        }
        fireStateChange(new StateChangeEvent(LANE_STATE_CHANGED_EVENT));
    }

    // returns true if the lane state was changed by this method
    private boolean addToLaneState(Tile tile) {
        boolean updated = false;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int tileLaneState = tile.getLaneStateByIndex(i, j);
                if (laneState[i][j] == tileLaneState || laneState[i][j] == LANE_STATE_PARTIALLY_SELECTED) {
                    // the lane state does not need to change
                    continue;
                }
                // the lane state will change
                updated = true;
                if (laneState[i][j] == LANE_STATE_UNDEFINED || laneState[i][j] == Tile.LANE_STATE_DISABLED) {
                    // set the lane state to the tile lane state
                    laneState[i][j] = tileLaneState;
                    continue;
                }
                // the lane state changes to partially selected
                laneState[i][j] = LANE_STATE_PARTIALLY_SELECTED;
            }
        }
        return updated;
    }

    private void clearLaneState() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                laneState[i][j] = LANE_STATE_UNDEFINED;
            }
        }
    }
}
