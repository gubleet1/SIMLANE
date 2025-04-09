package ch.simlane.editor.scenario;

import ch.simlane.editor.CarType;

import java.util.HashMap;
import java.util.Map;

public class StartPoint extends ScenarioPoint {

    private Map<CarType, Integer> inboundTraffic;

    public StartPoint(int side, int pos) {
        super(TYPE_START, side, pos);
        inboundTraffic = new HashMap<>(4);
    }

    public void addInboundTraffic(CarType carType, int count) {
        inboundTraffic.put(carType, count);
    }

    public Map<CarType, Integer> getInboundTraffic() {
        return new HashMap<>(inboundTraffic);
    }
}
