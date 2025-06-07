package newcode.fun.control.events.impl.player;


import newcode.fun.control.events.Event;

public interface Listener<T extends Event> {
    void onEvent(T var1);
}

