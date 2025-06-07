package newcode.fun.module.impl.movement;

import net.minecraft.potion.Effects;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.control.Manager;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.impl.combat.AttackAura;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.move.MoveUtil;

@Annotation(
        name = "Sprint",
        type = TypeList.Movement,
        desc = "Автоматически включает бег при движении вперед"
)
public class Sprint extends Module {
    private final TimerUtil timerUtil = new TimerUtil();
    public final ModeSetting sprintmode = new ModeSetting("Режимм", "Vanilla", "Vanilla", "ReallyWorld");
    public BooleanOption keepSprint = new BooleanOption("Удержи при ударе", true);

    public Sprint() {
        this.addSettings(sprintmode, keepSprint);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @Override
    public boolean onEvent(Event event) {
        if (!Manager.FUNCTION_MANAGER.sprint.keepSprint.get() && Manager.FUNCTION_MANAGER.auraFunction.getTarget() != null)
            return true;
        if (event instanceof EventUpdate) {
            switch (sprintmode.getIndex()) {
                case 0 -> {
                    if (!mc.player.isSneaking() && !mc.player.collidedHorizontally)
                        mc.player.setSprinting(MoveUtil.isMoving());
                }
                case 1 -> {
                    if (mc.player.isInWater()) {
                        if (MoveUtil.isMoving() && mc.player.moveForward > 0) {
                            mc.player.setSprinting(true);
                        } else {
                            mc.player.setSprinting(false);
                        }
                    } else {
                        if (Manager.FUNCTION_MANAGER.noSlow.state) {
                            if (mc.player.isSneaking() || mc.player.isForcedDown() || mc.player.isElytraFlying() || mc.player.isCrouching() || mc.player.getFoodStats().getFoodLevel() <= 6 || mc.player.isOnLadder() || mc.player.isPotionActive(Effects.SLOWNESS) || mc.player.isInLava()) {
                                mc.player.setSprinting(false);
                                return false;
                            }
                            if (MoveUtil.isMoving() && mc.player.moveForward > 0) {
                                mc.player.setSprinting(true);
                            } else {
                                mc.player.setSprinting(false);
                            }
                        } else {
                            if (mc.player.isSneaking() || mc.player.isForcedDown() || mc.player.isElytraFlying() || mc.player.isHandActive() || mc.player.isCrouching() || mc.player.getFoodStats().getFoodLevel() <= 6 || mc.player.isOnLadder() || mc.player.isPotionActive(Effects.SLOWNESS) || mc.player.isInLava()) {
                                mc.player.setSprinting(false);
                                return false;
                            }
                            if (MoveUtil.isMoving() && mc.player.moveForward > 0) {
                                mc.player.setSprinting(true);
                            } else {
                                mc.player.setSprinting(false);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
