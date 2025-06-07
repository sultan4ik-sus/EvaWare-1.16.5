package newcode.fun.control.events.impl.player;

import newcode.fun.control.events.Event;

public class EventStrafe extends Event {

    public float yaw;

    public EventStrafe(float yaw) {
        this.yaw = yaw;
    }

}
