package ch.simlane.editor;

import ch.simlane.editor.scenario.ScenarioPoint;

import java.util.LinkedList;
import java.util.List;

import static ch.simlane.editor.Editor.*;
import static ch.simlane.editor.Tile.TileLocation;

public class Map {

    private static final int DEFAULT_MAP_ROWS = 5;
    private static final int DEFAULT_MAP_COLS = 5;
    private static final int MIN_MAP_ROWS = 1;
    private static final int MIN_MAP_COLS = 1;
    private static final int MAX_MAP_ROWS = 10;
    private static final int MAX_MAP_COLS = 10;

    private Tile[][] map;

    // creates a map with the default size
    public Map() {
        this(DEFAULT_MAP_ROWS, DEFAULT_MAP_COLS);
    }

    // creates a map with the specified size
    public Map(int rows, int cols) {
        // validate map size arguments
        if (rows < MIN_MAP_ROWS || rows > MAX_MAP_ROWS) {
            rows = DEFAULT_MAP_ROWS;
        }
        if (cols < MIN_MAP_COLS || cols > MAX_MAP_COLS) {
            cols = DEFAULT_MAP_COLS;
        }
        // create tiles
        map = new Tile[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                map[i][j] = new Tile(i, j);
            }
        }
    }

    public int getNumRows() {
        return map.length;
    }

    public int getNumCols() {
        return map[0].length;
    }

    public Tile getTile(int row, int col) {
        return map[row][col];
    }

    public Tile getTile(TileLocation location) {
        return map[location.getRow()][location.getCol()];
    }

    public List<Tile> getTiles() {
        LinkedList<Tile> tiles = new LinkedList<>();
        for (int i = 0; i < getNumRows(); i++) {
            for (int j = 0; j < getNumCols(); j++) {
                tiles.add(map[i][j]);
            }
        }
        return tiles;
    }

    public Tile getNeighbourOf(Tile tile, int side) {
        TileLocation location = tile.getLocation().getLocationOn(side);
        if (!isLocationOnMap(location)) {
            // no neighbour exists on the specified side
            return null;
        }
        // return the neighbour
        return getTile(location);
    }

    public boolean isLocationOnMap(TileLocation location) {
        int row = location.getRow();
        int col = location.getCol();
        if (row < 0 || row >= getNumRows()) {
            return false;
        }
        if (col < 0 || col >= getNumCols()) {
            return false;
        }
        return true;
    }

    public TileLocation getScenarioPointLocation(ScenarioPoint scenarioPoint) {
        int row, col;
        int pos = scenarioPoint.getPos();
        switch (scenarioPoint.getSide()) {
            case SIDE_TOP:
                row = -1;
                col = pos;
                break;
            case SIDE_LEFT:
                row = pos;
                col = -1;
                break;
            case SIDE_RIGHT:
                row = pos;
                col = getNumCols();
                break;
            case SIDE_BOTTOM:
                row = getNumRows();
                col = pos;
                break;
            default:
                throw new IllegalStateException("Invalid side in scenario point.");
        }
        return new TileLocation(row, col);
    }
}
