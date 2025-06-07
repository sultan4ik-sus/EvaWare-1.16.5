package newcode.fun.module.impl.movement;

import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;

@Annotation(name = "NoFall", type = TypeList.Movement)
public class NoFall extends Module {


    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventMotion e) {
            if (mc.player.ticksExisted % 3 == 0 && mc.player.fallDistance > 3) {
                e.setY(e.getY() + 0.2f);
            }
        }
        return false;
    }
}
