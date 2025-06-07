package newcode.fun.module.impl.movement;

import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMove;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.move.MoveUtil;

@Annotation(name = "DragonFly", type = TypeList.Movement)
public class DragonFly extends Module {
    private final SliderSetting dragonFlySpeed = new SliderSetting("Скорость флая", 1.05f, 1.0f, 5.0F, 0.01f);
    private final SliderSetting dragonFlyMotionY = new SliderSetting("Скорость флая по Y", 0.55f, 0.1f, 2.5f, 0.01f);

    public DragonFly() {
        addSettings(dragonFlySpeed,dragonFlyMotionY);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventMove move) {
            handleDragonFly(move);
        }
        return false;
    }

    /**
     * Обработка движения при /fly
     *
     * @param move Обработчик EventMove
     */
    private void handleDragonFly(EventMove move) {
        if (mc.player.abilities.isFlying) {

            if (!mc.player.isSneaking() && mc.gameSettings.keyBindJump.isKeyDown()) {
                move.motion().y = dragonFlyMotionY.getValue().floatValue();
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                move.motion().y = -dragonFlyMotionY.getValue().floatValue();
            }

            MoveUtil.MoveEvent.setMoveMotion(move, dragonFlySpeed.getValue().floatValue());
        }
    }
}
