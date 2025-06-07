package newcode.fun.module.impl.movement;

import newcode.fun.control.events.Event;
import newcode.fun.control.Manager;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "AntiTarget", type = TypeList.Movement, desc = "Не даёт вас за таргетить на элитрах")
public class AntiTarget extends Module {
    public static SliderSetting gradus = new SliderSetting("Наклон", 35f, 30f, 50f, 1f);
    public static SliderSetting speed2 = new SliderSetting("Скорость", 1.9f, 1.9f, 2.5f, 0.01f);

    private float currentPitch;

    public AntiTarget() {
        addSettings(gradus, speed2);
        this.currentPitch = 0f;
    }

    @Override
    public boolean onEvent(Event event) {
        if (Manager.FUNCTION_MANAGER.auraFunction.target != null) {
            return false;
        }

        if (mc.player.isElytraFlying()) {
            float targetPitch = -gradus.getValue().floatValue();
            currentPitch += (targetPitch - currentPitch) * 0.1f;

            mc.player.rotationPitch = currentPitch;
        }
        return false;
    }
}