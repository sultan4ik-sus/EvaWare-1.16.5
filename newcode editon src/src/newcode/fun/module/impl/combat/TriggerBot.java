package newcode.fun.module.impl.combat;

import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;

@Annotation(name = "TriggerBot", type = TypeList.Combat)
public class TriggerBot extends Module {


    private final BooleanOption onlyCritical = new BooleanOption("Только криты", true);
    private final BooleanOption onlySpaceCritical = new BooleanOption("Только с пробелом", false)
            .setVisible(onlyCritical::get);

    public TriggerBot() {
        addSettings(onlyCritical, onlySpaceCritical);
    }

    private long cpsLimit = 0;

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate e) {
            if (cpsLimit > System.currentTimeMillis()) {
                cpsLimit--;
            }

            if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
                if (whenFalling() && (cpsLimit <= System.currentTimeMillis())) {
                    cpsLimit = System.currentTimeMillis() + 550;
                    if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
                        mc.playerController.attackEntity(mc.player, ((EntityRayTraceResult) mc.objectMouseOver).getEntity());
                        mc.player.swingArm(Hand.MAIN_HAND);
                    }
                }
            }
        }
        return false;
    }

    // Проверка, падает ли игрок, и условия для отмены критических ударов
    public boolean whenFalling() {
        boolean critWater = mc.player.areEyesInFluid(FluidTags.WATER);

        final boolean reasonForCancelCritical = mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isOnLadder()
                || mc.player.isInWater() && critWater
                || mc.player.isRidingHorse()
                || mc.player.abilities.isFlying
                || mc.player.isElytraFlying();

        final boolean onSpace = onlySpaceCritical.get()
                && mc.player.isOnGround()
                && !mc.gameSettings.keyBindJump.isKeyDown();

        if (mc.player.getCooledAttackStrength(1.5F) < 0.92F)
            return false;
        if (!reasonForCancelCritical && onlyCritical.get()) {
            return onSpace || !mc.player.isOnGround() && mc.player.fallDistance > 0.0F;
        }

        return true;
    }

}
