package newcode.fun.control.events.impl.player;

import newcode.fun.control.events.Event;

public class EventRotation extends Event {

    public float yaw,pitch;

    public EventRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

}
