package ch.simlane.utils;

import ch.simlane.editor.Editor;
import ch.simlane.editor.Map;
import ch.simlane.editor.Tile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONMap {

    private static final String JSON_KEY_SCENARIO = "scenario";
    private static final String JSON_KEY_ROWS = "rows";
    private static final String JSON_KEY_COLS = "cols";
    private static final String JSON_KEY_TILES = "tiles";
    private static final String JSON_KEY_LANES = "lanes";
    private static final String JSON_KEY_SIDE_FROM = "from";
    private static final String JSON_KEY_SIDE_TO = "to";

    private Map map;
    private String scenario;

    private JSONObject json;

    public JSONMap(Map map, String scenario) throws JSONException {
        this.map = map;
        this.scenario = scenario;
        createJSON();
    }

    public JSONMap(String jsonMap) throws JSONException {
        json = new JSONObject(jsonMap);
        createMap();
        scenario = json.getString(JSON_KEY_SCENARIO);
    }

    public Map getMap() {
        return map;
    }

    public String getScenario() {
        return scenario;
    }

    public String getJSON() {
        return json.toString(2);
    }

    private void createJSON() throws JSONException {
        json = new JSONObject();
        json.put(JSON_KEY_SCENARIO, scenario);
        json.put(JSON_KEY_ROWS, map.getNumRows());
        json.put(JSON_KEY_COLS, map.getNumCols());
        createTilesJSON();
    }

    private void createTilesJSON() throws JSONException {
        json.put(JSON_KEY_TILES, new JSONArray());
        for (int i = 0; i < map.getNumRows(); i++) {
            JSONArray row = new JSONArray();
            json.append(JSON_KEY_TILES, row);
            for (int j = 0; j < map.getNumCols(); j++) {
                JSONObject jsonTile = new JSONObject();
                Tile tile = map.getTile(i, j);
                createTileJSON(jsonTile, tile);
                row.put(jsonTile);
            }
        }
    }

    private void createTileJSON(JSONObject jsonTile, Tile tile) throws JSONException {
        jsonTile.put(JSON_KEY_LANES, new JSONArray());
        int[][] lanes = tile.getLaneState();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (lanes[i][j] == Tile.LANE_STATE_SELECTED) {
                    createLaneJSON(jsonTile, i, j);
                }
            }
        }
    }

    private void createLaneJSON(JSONObject jsonTile, int fromIndex, int toIndex) throws JSONException {
        int sideFrom = Editor.getSideFromArrayIndex(fromIndex);
        int sideTo = Editor.getSideFromArrayIndex(toIndex);
        JSONObject lane = new JSONObject();
        lane.put(JSON_KEY_SIDE_FROM, sideFrom);
        lane.put(JSON_KEY_SIDE_TO, sideTo);
        jsonTile.append(JSON_KEY_LANES, lane);
    }

    private void createMap() throws JSONException {
        int rows = json.getInt(JSON_KEY_ROWS);
        int cols = json.getInt(JSON_KEY_COLS);
        map = new Map(rows, cols);
        createTiles(json.getJSONArray(JSON_KEY_TILES));
    }

    private void createTiles(JSONArray tiles) throws JSONException {
        for (int i = 0; i < map.getNumRows(); i++) {
            for (int j = 0; j < map.getNumCols(); j++) {
                JSONObject jsonTile = tiles.getJSONArray(i).getJSONObject(j);
                Tile tile = map.getTile(i, j);
                createTile(tile, jsonTile);
            }
        }
    }

    private void createTile(Tile tile, JSONObject jsonTile) throws JSONException {
        JSONArray lanes = jsonTile.getJSONArray(JSON_KEY_LANES);
        for (int i = 0; i < lanes.length(); i++) {
            JSONObject lane = lanes.getJSONObject(i);
            int sideFrom = lane.getInt(JSON_KEY_SIDE_FROM);
            int sideTo = lane.getInt(JSON_KEY_SIDE_TO);
            tile.setLaneState(sideFrom, sideTo, Tile.LANE_STATE_SELECTED);
        }
    }
}
