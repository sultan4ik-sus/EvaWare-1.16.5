package eva.ware.events;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventInventoryClose extends CancelEvent {

    public int windowId;

}
