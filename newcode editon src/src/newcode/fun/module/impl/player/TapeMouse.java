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
        type = TypeList.Player, desc = "���� ������ �� ����� ��� ������ ����"
)

public class TapeMouse extends Module {
    public SliderSetting speed = new SliderSetting("��������", 1, 0, 50, 1);
    public ModeSetting mode = new ModeSetting("����� �������", "����� ����", new String[]{"����� ����", "������ ����"});
    private final TimerUtil timerUtil = new TimerUtil();
    public TapeMouse() {
        addSettings(mode, speed);
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate e) {
            if (mode.is("����� ����")) {
                if (timerUtil.hasTimeElapsed(speed.getValue().longValue() * 100L)) {
                    mc.clickMouse();
                    timerUtil.reset();
                }
            }
            if (mode.is("������ ����")) {
                if (timerUtil.hasTimeElapsed(speed.getValue().longValue() * 100L)) {
                    mc.rightClickMouse();
                    timerUtil.reset();
                }
            }
        }

        return false;
    }
}
