package newcode.fun.module.impl.movement;

import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventDamage;
import newcode.fun.control.events.impl.player.EventNoSlow;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.StopWatch;
import newcode.fun.utils.misc.DamageUtil;
import newcode.fun.utils.move.MoveUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Annotation(name = "NoSlow", type = TypeList.Movement)
public class NoSlow extends Module {
    public ModeSetting mode = new ModeSetting("Мод", "Vanilla", "Vanilla", "Matrix", "Grim");
    private DamageUtil damageUtil = new DamageUtil();
    public final SliderSetting vanillaSpeed = new SliderSetting("Скорость", 60F, 20F, 100F, 1F).setVisible(() -> mode.is("Vanilla"));
    public final BooleanOption OnlyJump = new BooleanOption("Только на земле", false);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final StopWatch stopWatch = new StopWatch();

    public NoSlow() {
        addSettings(mode, vanillaSpeed, OnlyJump);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (mc.player.isElytraFlying()) return false;
        if (OnlyJump.get() && !mc.player.isOnGround()) return false;

        if (event instanceof EventNoSlow eventNoSlow) {
            handleEventUpdate(eventNoSlow);
        } else if (event instanceof EventDamage damage) {
            damageUtil.processDamage(damage);
        } else if (event instanceof EventPacket eventPacket) {
            if (eventPacket.isReceivePacket())
                damageUtil.onPacketEvent(eventPacket);
        }
        return false;
    }

    /**
     * Обрабатывает событие типа EventUpdate.
     */
    private void handleEventUpdate(EventNoSlow eventNoSlow) {
        if (mc.player.isHandActive()) {
            switch (mode.get()) {
                case "Vanilla" -> handleVanillaMode(eventNoSlow);
                case "Grim" -> handleGrimACMode(eventNoSlow);
                case "Matrix" -> handleMatrixMode(eventNoSlow);
                case "Really World" -> handleGrimNewMode(eventNoSlow);
            }
        }
    }

    public boolean isBlockUnderWith() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset(mc.player.getMotion().x, -1e-1, mc.player.getMotion().z);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }
    /**
     * Обрабатывает мод "Vanilla" с учетом слайдера скорости.
     */
    private void handleVanillaMode(EventNoSlow eventNoSlow) {
        eventNoSlow.setCancel(true);
        float speedMultiplier = vanillaSpeed.getValue().floatValue() / 100.0F;
        mc.player.motion.x *= speedMultiplier;
        mc.player.motion.z *= speedMultiplier;
    }

    private void handleMatrixMode(EventNoSlow eventNoSlow) {
        boolean isFalling = (double) mc.player.fallDistance > 0.725;
        float speedMultiplier;
        eventNoSlow.setCancel(true);

        // Check if the player is on the ground and not jumping
        if (mc.player.isOnGround() && !mc.player.movementInput.jump) {
            if (mc.player.ticksExisted % 2 == 0) {
                boolean isNotStrafing = mc.player.moveStrafing == 0.0F;
                speedMultiplier = isNotStrafing ? 0.5F : 0.4F;

                speedMultiplier *= 2;

                mc.player.motion.x *= speedMultiplier;
                mc.player.motion.z *= speedMultiplier;
            }
        } else if (isFalling) {
            boolean isVeryFastFalling = (double) mc.player.fallDistance > 1.4;
            speedMultiplier = isVeryFastFalling ? 0.95F : 0.97F;

            if (mc.player.motion.y < -0.5) { // If falling fast
                speedMultiplier *= 0.9F; // Reduce speed more
            } else if (mc.player.motion.y < -0.2) { // If falling moderately
                speedMultiplier *= 0.95F; // Slightly reduce speed
            }

            mc.player.motion.x *= speedMultiplier;
            mc.player.motion.z *= speedMultiplier;
        }

        // Additional bypass logic for specific scenarios
        if (mc.player.isInWater()) {
            mc.player.motion.x *= 0.8F; // Reduce speed in water
            mc.player.motion.z *= 0.8F;
        }

        if (mc.player.isSneaking()) {
            mc.player.motion.x *= 0.5F; // Reduce speed while sneaking
            mc.player.motion.z *= 0.5F;
        }

        // Implement a cooldown mechanism to prevent detection
        if (mc.player.ticksExisted % 100 == 0) {
            // Reset motion to normal after a certain period
            mc.player.motion.x *= 1.0F;
            mc.player.motion.z *= 1.0F;
        }
    }

    private void handleGrimNewMode(EventNoSlow e) {
        this.damageUtil.time(1500);

        if ((mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK
                || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT) && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }

        if (mc.player.isOnGround() && !mc.player.movementInput.jump && !damageUtil.isNormalDamage()) {
            return;
        }

        if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            e.setCancel(true);
            return;
        }
        e.setCancel(true);
        sendItemChangePacket();
    }

    /**
     * Обрабатывает мод "GrimAC".
     */
    private void handleGrimACMode(EventNoSlow noSlow) {
        if (mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && mc.player.getActiveHand() == Hand.MAIN_HAND || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }

        if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            noSlow.setCancel(true);
            return;
        }

        noSlow.setCancel(true);
        sendItemChangePacket();
    }

    /**
     * Отправляет пакеты смены активного предмета, если игрок движется.
     */
    private void sendItemChangePacket() {
        if (MoveUtil.isMoving()) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket((mc.player.inventory.currentItem % 8 + 1)));
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }
}

