package ch.simlane.utils;

import ch.simlane.editor.CarType;
import ch.simlane.editor.Editor;
import ch.simlane.editor.scenario.EndPoint;
import ch.simlane.editor.scenario.Scenario;
import ch.simlane.editor.scenario.ScenarioPoint;
import ch.simlane.editor.scenario.StartPoint;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static ch.simlane.editor.scenario.ScenarioPoint.TYPE_END;
import static ch.simlane.editor.scenario.ScenarioPoint.TYPE_START;

public class JSONScenarios {

    private static final String JSON_KEY_SCENARIOS_ARRAY = "scenarios";
    private static final String JSON_KEY_SCENARIO_NAME = "name";
    private static final String JSON_KEY_SCENARIO_DISPLAY_NAME = "display-name";
    private static final String JSON_KEY_SCENARIO_MAP_ROWS = "map-rows";
    private static final String JSON_KEY_SCENARIO_MAP_COLS = "map-cols";
    private static final String JSON_KEY_START_POINTS_ARRAY = "start-points";
    private static final String JSON_KEY_END_POINTS_ARRAY = "end-points";
    private static final String JSON_KEY_SCENARIO_POINT_SIDE = "side";
    private static final String JSON_KEY_SCENARIO_POINT_POS = "pos";
    private static final String JSON_KEY_CARS_ARRAY = "cars";
    private static final String JSON_KEY_CAR_TYPE = "type";
    private static final String JSON_KEY_CAR_COUNT = "count";

    private String scenarios;

    public JSONScenarios(String path) {
        try {
            scenarios = IOUtils.toString(getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getScenarioDisplayName(String name) {
        JSONObject scenarioJSONObject = getScenarioJSONObject(name);
        String displayName;
        try {
            displayName = scenarioJSONObject.getString(JSON_KEY_SCENARIO_DISPLAY_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException("Reading scenarios definition file failed.");
        }
        return displayName;
    }

    public String[] getAvailableScenarios() {
        String[] availableScenarios;
        try {
            JSONArray scenariosJSONArray = new JSONObject(scenarios).getJSONArray(JSON_KEY_SCENARIOS_ARRAY);
            int numScenarios = scenariosJSONArray.length();
            availableScenarios = new String[numScenarios];
            for (int i = 0; i < numScenarios; i++) {
                JSONObject scenarioJSONObject = scenariosJSONArray.getJSONObject(i);
                availableScenarios[i] = scenarioJSONObject.getString(JSON_KEY_SCENARIO_NAME);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException("Reading scenarios definition file failed.");
        }
        return availableScenarios;
    }

    public Scenario getScenario(String name) {
        return parseScenario(getScenarioJSONObject(name));
    }

    private JSONObject getScenarioJSONObject(String name) {
        JSONObject scenarioJSONObject;
        try {
            JSONArray scenariosJSONArray = new JSONObject(scenarios).getJSONArray(JSON_KEY_SCENARIOS_ARRAY);
            scenarioJSONObject = findScenario(scenariosJSONArray, name);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException("Reading scenarios definition file failed.");
        }
        return scenarioJSONObject;
    }

    private JSONObject findScenario(JSONArray scenariosJSONArray, String name) throws JSONException {
        JSONObject scenarioJSONObject;
        for (int i = 0; i < scenariosJSONArray.length(); i++) {
            scenarioJSONObject = scenariosJSONArray.getJSONObject(i);
            if (scenarioJSONObject.getString(JSON_KEY_SCENARIO_NAME).equals(name)) {
                return scenarioJSONObject;
            }
        }
        throw new UnsupportedOperationException("Scenario \"" + name + "\" was not found.");
    }

    private Scenario parseScenario(JSONObject scenarioJSONObject) throws JSONException {
        Scenario scenario;
        String name = scenarioJSONObject.getString(JSON_KEY_SCENARIO_NAME);
        int mapRows = scenarioJSONObject.getInt(JSON_KEY_SCENARIO_MAP_ROWS);
        int mapCols = scenarioJSONObject.getInt(JSON_KEY_SCENARIO_MAP_COLS);
        JSONArray startPointsJSONArray = scenarioJSONObject.getJSONArray(JSON_KEY_START_POINTS_ARRAY);
        JSONArray endPointsJSONArray = scenarioJSONObject.getJSONArray(JSON_KEY_END_POINTS_ARRAY);
        scenario = new Scenario(name, mapRows, mapCols);
        parseScenarioPointsArray(startPointsJSONArray, TYPE_START).forEach(scenario::addScenarioPoint);
        parseScenarioPointsArray(endPointsJSONArray, TYPE_END).forEach(scenario::addScenarioPoint);
        return scenario;
    }

    private List<ScenarioPoint> parseScenarioPointsArray(JSONArray scenarioPointsJSONArray, int type) throws JSONException {
        List<ScenarioPoint> scenarioPoints = new LinkedList<>();
        for (int i = 0; i < scenarioPointsJSONArray.length(); i++) {
            JSONObject scenarioPointJSONObject = scenarioPointsJSONArray.getJSONObject(i);
            scenarioPoints.add(parseScenarioPoint(scenarioPointJSONObject, type));
        }
        return scenarioPoints;
    }

    private ScenarioPoint parseScenarioPoint(JSONObject scenarioPointJSONObject, int type) throws JSONException {
        int side = getSide(scenarioPointJSONObject);
        int pos = scenarioPointJSONObject.getInt(JSON_KEY_SCENARIO_POINT_POS);
        if (type == TYPE_START) {
            StartPoint startPoint = new StartPoint(side, pos);
            JSONArray carsJSONArray = scenarioPointJSONObject.getJSONArray(JSON_KEY_CARS_ARRAY);
            parseCarsArray(carsJSONArray, startPoint);
            return startPoint;
        } else {
            EndPoint endPoint = new EndPoint(side, pos);
            CarType carType = getCarType(scenarioPointJSONObject);
            endPoint.setCarType(carType);
            return endPoint;
        }
    }

    private void parseCarsArray(JSONArray carsJSONArray, StartPoint startPoint) throws JSONException {
        for (int i = 0; i < carsJSONArray.length(); i++) {
            JSONObject carJSONObject = carsJSONArray.getJSONObject(i);
            CarType carType = getCarType(carJSONObject);
            int count = carJSONObject.getInt(JSON_KEY_CAR_COUNT);
            startPoint.addInboundTraffic(carType, count);
        }
    }

    private int getSide(JSONObject jsonObject) throws JSONException {
        String side = jsonObject.getString(JSON_KEY_SCENARIO_POINT_SIDE);
        return Editor.convertToSide(side);
    }

    private CarType getCarType(JSONObject jsonObject) throws JSONException {
        String carType = jsonObject.getString(JSON_KEY_CAR_TYPE);
        return CarType.get(carType);
    }
}
