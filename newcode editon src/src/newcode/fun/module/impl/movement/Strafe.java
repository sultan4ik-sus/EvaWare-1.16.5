package newcode.fun.module.impl.movement;

import net.minecraft.block.AirBlock;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventAction;
import newcode.fun.control.events.impl.player.EventDamage;
import newcode.fun.control.events.impl.player.EventMove;
import newcode.fun.control.events.impl.player.EventPostMove;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.misc.DamageUtil;
import newcode.fun.utils.move.MoveUtil;

import static newcode.fun.utils.move.MoveUtil.StrafeMovement.*;


@Annotation(name = "Strafe", type = TypeList.Movement)
public class Strafe extends Module {

    private BooleanOption damageBoost = new BooleanOption("Буст с дамагом", false);

    private SliderSetting boostSpeed = new SliderSetting("Скорость буста", 0.7f, 0.1F, 5.0f, 0.1F).setVisible(damageBoost::get);

    private DamageUtil damageUtil = new DamageUtil();

    public Strafe() {
        addSettings();
    }

    @Override
    public boolean onEvent(final Event event) {

            if (event instanceof EventAction action) {
                handleEventAction(action);
            } else if (event instanceof EventMove eventMove) {
                handleEventMove(eventMove);
            } else if (event instanceof EventPostMove eventPostMove) {
                handleEventPostMove(eventPostMove);
            } else if (event instanceof EventPacket packet) {

                handleEventPacket(packet);
            } else if (event instanceof EventDamage damage) {
                handleDamageEvent(damage);

        }
        return false;
    }

    /**
     * Обрабатывает событие типа EventDamage.
     *
     * @param damage Евент дамага
     */
    private void handleDamageEvent(EventDamage damage) {
        if (damageBoost.get()) {
            damageUtil.processDamage(damage);
        }
    }

    /**
     * Обрабатывает событие типа EventAction.
     */
    private void handleEventAction(EventAction action) {
        if (strafes()) {
            handleStrafesEventAction(action);
        }
        if (needSwap) {
            handleNeedSwapEventAction(action);
        }
    }

    /**
     * Обрабатывает событие типа EventMove.
     */
    private void handleEventMove(EventMove eventMove) {
        if (strafes()) {
            handleStrafesEventMove(eventMove);
        } else {
            oldSpeed = 0;
        }
    }

    /**
     * Обрабатывает событие типа EventPostMove.
     */
    private void handleEventPostMove(EventPostMove eventPostMove) {
        postMove(eventPostMove.getHorizontalMove());
    }

    /**
     * Обрабатывает событие типа EventPacket.
     */
    private void handleEventPacket(EventPacket packet) {

        if (packet.isReceivePacket()) {
            if (damageBoost.get()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    /**
     * Обрабатывает событие типа EventAction.
     */
    private void handleStrafesEventAction(EventAction action) {
        if (CEntityActionPacket.lastUpdatedSprint != needSprintState) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    /**
     * Обрабатывает событие типа EventMove для сета скорости.
     */
    private void handleStrafesEventMove(EventMove eventMove) {
        if (damageBoost.get()) this.damageUtil.time(700L);

        final float damageSpeed = boostSpeed.getValue().floatValue() / 10.0F;
        final double speed = calculateSpeed(eventMove, damageBoost.get(), damageUtil.isNormalDamage(), damageSpeed);

        MoveUtil.MoveEvent.setMoveMotion(eventMove, speed);
    }

    /**
     * Обрабатывает событие типа EventAction для needSwap.
     */
    private void handleNeedSwapEventAction(EventAction action) {
        action.setSprintState(!mc.player.serverSprintState);
        needSwap = false;
    }

    /**
     * Обрабатывает событие типа EventPacket для обнуления скорости.
     */
    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            oldSpeed = 0;
        }

    }

    /**
     * Проверяет условия.
     */
    public static boolean strafes() {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        if (mc.player.isSneaking() || mc.player.isElytraFlying()) {
            return false;
        }
        if (mc.player.isInWater() || mc.player.isInLava()) {
            if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isSneaking()
                    && !(mc.world.getBlockState(mc.player.getPosition().add(0, 1, 0)).getBlock() instanceof AirBlock)) {
                return false;
            }
        }
        if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getMaterial() == Material.WEB
                || mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.01, mc.player.getPosZ())).getBlock() instanceof SoulSandBlock) {
            return false;
        }
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    protected void onEnable() {
        oldSpeed = 0;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        mc.player.motion.x *= 0.7f;
        mc.player.motion.z *= 0.7f;
        super.onDisable();
    }
}
