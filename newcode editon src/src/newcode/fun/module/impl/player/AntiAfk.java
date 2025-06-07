package newcode.fun.module.impl.player;

import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.move.MoveUtil;

@Annotation(name = "AntiAfk", type = TypeList.Player)
public class AntiAfk extends Module {

    private final TimerUtil timerUtil = new TimerUtil();

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (!MoveUtil.isMoving()) {
                if (timerUtil.hasTimeElapsed(16000)) {
                    mc.rightClickMouse();
                    timerUtil.reset();
                }
            } else {
                timerUtil.reset();
            }
        }
        return false;
    }
}
