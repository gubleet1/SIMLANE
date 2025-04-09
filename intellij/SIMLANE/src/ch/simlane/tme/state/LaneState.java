package ch.simlane.tme.state;

import ch.simlane.tme.components.Lane;
import ch.simlane.tme.components.TrafficLight;

import static ch.simlane.tme.components.Connector.ConnectorLocation;

public class LaneState {

    private ConnectorLocation start;
    private ConnectorLocation end;
    private TrafficLight state;

    public LaneState(Lane lane) {
        start = lane.getStart().getLocation();
        end = lane.getEnd().getLocation();
        state = lane.getState();
    }

    public ConnectorLocation getStart() {
        return start;
    }

    public ConnectorLocation getEnd() {
        return end;
    }

    public TrafficLight getState() {
        return state;
    }
}
