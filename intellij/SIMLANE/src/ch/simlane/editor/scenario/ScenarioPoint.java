package ch.simlane.editor.scenario;

import ch.simlane.editor.Editor;

import java.util.Objects;

public abstract class ScenarioPoint {

    public static final int TYPE_START = 11000;
    public static final int TYPE_END = 11001;

    private int type;
    private ScenarioPointLocation location;

    ScenarioPoint(int type, int side, int pos) {
        if (!Editor.isValidSide(side)) {
            throw new IllegalArgumentException(side + " is not a valid side.");
        }
        this.type = type;
        location = new ScenarioPointLocation(side, pos);
    }

    public int getType() {
        return type;
    }

    public ScenarioPointLocation getLocation() {
        return location;
    }

    public int getSide() {
        return location.getSide();
    }

    public int getPos() {
        return location.getPos();
    }

    public static class ScenarioPointLocation {

        private int side;
        private int pos;

        public ScenarioPointLocation(int side, int pos) {
            this.side = side;
            this.pos = pos;
        }

        public int getSide() {
            return side;
        }

        public int getPos() {
            return pos;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ScenarioPointLocation)) {
                return false;
            }
            ScenarioPointLocation other = (ScenarioPointLocation) o;
            return side == other.side && pos == other.pos;
        }

        public int hashCode() {
            return Objects.hash(side, pos);
        }
    }
}
