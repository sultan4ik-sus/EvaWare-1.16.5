package newcode.fun.control.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import newcode.fun.control.events.Event;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class EventMouseTick extends Event {

    private int button;
}
