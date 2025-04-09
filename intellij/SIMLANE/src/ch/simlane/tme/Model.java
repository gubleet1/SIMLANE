package ch.simlane.tme;

import ch.simlane.tme.algorithms.ShortestPath;
import ch.simlane.tme.components.Car;
import ch.simlane.tme.components.Connector;
import ch.simlane.tme.components.Intersection;
import ch.simlane.tme.components.Lane;

import java.util.LinkedList;
import java.util.List;

public class Model {

    private List<Car> cars;
    private List<Lane> lanes;
    private List<Intersection> intersections;

    public Model() {
        cars = new LinkedList<>();
        lanes = new LinkedList<>();
        intersections = new LinkedList<>();
    }

    public List<Car> getCars() {
        return cars;
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    public List<Lane> getLanes() {
        return lanes;
    }

    public void addLane(Lane lane) {
        lanes.add(lane);
    }

    public List<Intersection> getIntersections() {
        return intersections;
    }

    public void addIntersection(Intersection intersection) {
        intersections.add(intersection);
    }

    public boolean validate() {
        for (Car car : cars) {
            Connector start = car.getStartConnector();
            Connector end = car.getEndConnector();
            List<Connector> path = ShortestPath.shortestPath(start, end);
            if (path == null) {
                return false;
            }
            car.setPath(path);
        }
        return true;
    }

    public void reset() {
        cars.forEach(Car::reset);
        lanes.forEach(Lane::reset);
        intersections.forEach(Intersection::reset);
    }
}
