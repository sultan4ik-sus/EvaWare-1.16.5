package newcode.fun.control.events.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import newcode.fun.control.events.Event;
@Getter
@Setter
@AllArgsConstructor
public class EventInput extends Event {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;



}
