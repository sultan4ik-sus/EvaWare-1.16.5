package newcode.fun.module.impl.player;

import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(
        name = "TapeMouse",
        type = TypeList.Player, desc = "Авто кликер на левый или правый клик"
)

public class TapeMouse extends Module {
    public SliderSetting speed = new SliderSetting("Задержка", 1, 0, 50, 1);
    public ModeSetting mode = new ModeSetting("Выбор режимов", "Левый клик", new String[]{"Левый клик", "Правый клик"});
    private final TimerUtil timerUtil = new TimerUtil();
    public TapeMouse() {
        addSettings(mode, speed);
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate e) {
            if (mode.is("Левый клик")) {
                if (timerUtil.hasTimeElapsed(speed.getValue().longValue() * 100L)) {
                    mc.clickMouse();
                    timerUtil.reset();
                }
            }
            if (mode.is("Правый клик")) {
                if (timerUtil.hasTimeElapsed(speed.getValue().longValue() * 100L)) {
                    mc.rightClickMouse();
                    timerUtil.reset();
                }
            }
        }

        return false;
    }
}
