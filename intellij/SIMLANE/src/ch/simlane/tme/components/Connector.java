package ch.simlane.tme.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Connector {

    public static final int CONNECTOR_TYPE_DEFAULT = 10000;
    public static final int CONNECTOR_TYPE_START = 10001;
    public static final int CONNECTOR_TYPE_END = 10002;

    private ConnectorLocation location;

    private List<Lane> in;
    private LaneGroup out;

    private int type;

    public Connector(Object refIn, Object refOut) {
        this(refIn, refOut, CONNECTOR_TYPE_DEFAULT);
    }

    public Connector(Object refIn, Object refOut, int type) {
        location = new ConnectorLocation(refIn, refOut);
        in = new LinkedList<>();
        this.type = type;
    }

    public List<Lane> getIn() {
        return in;
    }

    // for connecting connectors (also changes the end connectors of the lanes)
    private void setIn(List<Lane> in) {
        in.forEach(lane -> lane.setEnd(this));
        this.in = in;
    }

    // for instantiating new lanes
    void addIn(Lane lane) {
        if (in.contains(lane)) {
            return;
        }
        in.add(lane);
    }

    public LaneGroup getOut() {
        return out;
    }

    // for instantiating new lane groups
    void setOut(LaneGroup out) {
        setOut(out, false);
    }

    private void setOut(LaneGroup out, boolean connecting) {
        if (connecting) {
            // also change the start connector of the lane group when connecting connectors
            out.setStart(this);
        }
        this.out = out;
    }

    private int getType() {
        return type;
    }

    public boolean isConnectable() {
        if (isConnected()) {
            return false;
        }
        return isInSet() || isOutSet();
    }

    public Connector connectWith(Connector connector) {
        if (!isConnectableWith(connector)) {
            throw new IllegalStateException("The connectors can not be connected.");
        }
        if (isInSet()) {
            setOut(connector.getOut(), true);
        } else {
            setIn(connector.getIn());
        }
        connector.clear();
        return this;
    }

    private boolean isConnectableWith(Connector connector) {
        if (connector.getType() != CONNECTOR_TYPE_DEFAULT) {
            return false;
        }
        if (isConnected() || connector.isConnected()) {
            return false;
        }
        if (!(isInSet() || connector.isInSet())) {
            return false;
        }
        if (!(isOutSet() || connector.isOutSet())) {
            return false;
        }
        return true;
    }

    private boolean isConnected() {
        return isInSet() && isOutSet();
    }

    private boolean isInSet() {
        return !in.isEmpty() || type == CONNECTOR_TYPE_START;
    }

    private boolean isOutSet() {
        return out != null || type == CONNECTOR_TYPE_END;
    }

    private void clear() {
        in = new LinkedList<>();
        out = null;
    }

    public Lane getLaneTo(Connector end) {
        for (Lane lane : out.getLanes()) {
            if (lane.getEnd() == end) {
                return lane;
            }
        }
        throw new IllegalStateException("Connector does not have a lane that leads to specified end.");
    }

    public ConnectorLocation getLocation() {
        return location;
    }

    public static class ConnectorLocation {

        /*
         * References to two arbitrary objects that represent the incoming respectively outgoing
         * lanes of the corresponding connector outside the tme.
         * In the case of SIMLANE these are tile location objects for two adjacent tiles. A connector
         * is therefore located between two tiles.
         */
        private Object refIn;
        private Object refOut;

        public ConnectorLocation(Object refIn, Object refOut) {
            this.refIn = refIn;
            this.refOut = refOut;
        }

        public Object getRefIn() {
            return refIn;
        }

        public Object getRefOut() {
            return refOut;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ConnectorLocation)) {
                return false;
            }
            ConnectorLocation other = (ConnectorLocation) o;
            return refIn.equals(other.refIn) && refOut.equals(other.refOut);
        }

        public int hashCode() {
            return Objects.hash(refIn, refOut);
        }
    }
}
