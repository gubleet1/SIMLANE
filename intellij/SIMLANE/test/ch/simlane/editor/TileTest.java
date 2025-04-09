package ch.simlane.editor;

import ch.simlane.editor.event.StateChangeEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TileTest {

    @Test
    void laneStateChangedEventTest() {
        // create tile
        Tile tile = new Tile(0, 0);
        // create tile spy
        Tile spy = Mockito.spy(tile);
        // create argument captor for StateChangeEvent
        ArgumentCaptor<StateChangeEvent> event = ArgumentCaptor.forClass(StateChangeEvent.class);
        // edit tile spy
        spy.setLaneState(Editor.SIDE_LEFT, Editor.SIDE_RIGHT, Tile.LANE_STATE_SELECTED);
        // test call to fireStateChange
        Mockito.verify(spy).fireStateChange(Mockito.any(StateChangeEvent.class));
        Mockito.verify(spy).fireStateChange(event.capture());
        assertEquals(Tile.LANE_STATE_CHANGED_EVENT, event.getValue().getType());
    }
}
