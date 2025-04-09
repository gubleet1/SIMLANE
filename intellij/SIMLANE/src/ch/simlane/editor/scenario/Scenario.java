package ch.simlane.editor.scenario;

import ch.simlane.editor.CarType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static ch.simlane.editor.scenario.ScenarioPoint.ScenarioPointLocation;

public class Scenario {

    private String name;
    private int mapRows;
    private int mapCols;

    private HashMap<ScenarioPointLocation, StartPoint> startPoints;
    private HashMap<ScenarioPointLocation, EndPoint> endPoints;

    public Scenario(String name, int mapRows, int mapCols) {
        this.name = name;
        this.mapRows = mapRows;
        this.mapCols = mapCols;
        startPoints = new HashMap<>();
        endPoints = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public int getMapRows() {
        return mapRows;
    }

    public int getMapCols() {
        return mapCols;
    }

    public StartPoint getStartPoint(ScenarioPointLocation location) {
        return startPoints.get(location);
    }

    public EndPoint getEndPoint(ScenarioPointLocation location) {
        return endPoints.get(location);
    }

    public List<StartPoint> getStartPoints() {
        return new LinkedList<>(startPoints.values());
    }

    public List<EndPoint> getEndPoints() {
        return new LinkedList<>(endPoints.values());
    }

    public List<ScenarioPoint> getScenarioPoints() {
        List<ScenarioPoint> scenarioPoints = new LinkedList<>(startPoints.values());
        scenarioPoints.addAll(endPoints.values());
        return scenarioPoints;
    }

    public EndPoint getEndPoint(CarType carType) {
        for (EndPoint endPoint : endPoints.values()) {
            if (endPoint.getCarType() == carType) {
                return endPoint;
            }
        }
        throw new IllegalStateException("No EndPoint with the given CarType was found.");
    }

    public void addScenarioPoint(ScenarioPoint scenarioPoint) {
        ScenarioPointLocation location = scenarioPoint.getLocation();
        if (scenarioPoint.getType() == ScenarioPoint.TYPE_START) {
            startPoints.put(location, (StartPoint) scenarioPoint);
        } else {
            endPoints.put(location, (EndPoint) scenarioPoint);
        }
    }
}
