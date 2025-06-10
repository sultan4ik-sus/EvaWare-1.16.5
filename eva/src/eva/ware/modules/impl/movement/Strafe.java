package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.combat.HitAura;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.player.DamagePlayerUtility;
import eva.ware.utils.player.InventoryUtility;
import eva.ware.utils.player.MoveUtility;
import eva.ware.utils.player.StrafeMovement;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;

import java.util.Random;


@ModuleRegister(name = "Strafe", category = Category.Movement)
public class Strafe extends Module {
    private final ModeSetting mode = new ModeSetting("Обход", "Matrix Hard", "Matrix", "Matrix Hard");
    private final CheckBoxSetting elytra = new CheckBoxSetting("Буст с элитрой", false);
    private final SliderSetting setSpeed = new SliderSetting("Скорость", 1.5F, 0.5F, 2.5F, 0.1F).visibleIf(() -> elytra.getValue());
    private final CheckBoxSetting damageBoost = new CheckBoxSetting("Буст с дамагом", false);
    private final SliderSetting boostSpeed = new SliderSetting("Значение буста", 0.7f, 0.1F, 5.0f, 0.1F).visibleIf(() -> damageBoost.getValue());

    private final CheckBoxSetting onlyGround = new CheckBoxSetting("Только на земле", false).visibleIf(() -> mode.is("Matrix Hard"));
    private final CheckBoxSetting autoJump = new CheckBoxSetting("Прыгать", false);
    private final CheckBoxSetting moveDir = new CheckBoxSetting("Направление", true);

    private final DamagePlayerUtility damageUtil = new DamagePlayerUtility();
    private final StrafeMovement strafeMovement = new StrafeMovement();
    private final TargetStrafe targetStrafe;
    private final HitAura hitAura;
    public static int waterTicks;

    public boolean check() {
        return Evaware.getInst().getModuleManager().getHitAura().getTarget() != null && Evaware.getInst().getModuleManager().getHitAura().isEnabled();
    }

    public Strafe(TargetStrafe targetStrafe, HitAura hitAura) {
        this.targetStrafe = targetStrafe;
        this.hitAura = hitAura;
        addSettings(mode, elytra, setSpeed, damageBoost, boostSpeed, onlyGround, autoJump, moveDir);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;
        handleEventAction(e);
    }

    @Subscribe
    private void onMoving(EventMoving e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;
        handleEventMove(e);
    }

    @Subscribe
    private void onPostMove(EventPostMove e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;
        handleEventPostMove(e);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;
        handleEventPacket(e);
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;
        handleDamageEvent(e);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

        if (moveDir.getValue() && !check()) {
            mc.player.rotationYawHead = MoveUtility.moveYaw(mc.player.rotationYaw);
            mc.player.renderYawOffset = mc.player.rotationYawHead;
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

        if (autoJump.getValue()) {
            if (mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
                mc.player.jump();
            }
        }

        if (!elytra.getValue()) return;
        int elytra = InventoryUtility.getInstance().getHotbarSlotOfItem();

        if (mc.player.isInWater() || mc.player.isInLava() || waterTicks > 0 || elytra == -1)
            return;
        if (mc.player.fallDistance != 0 && mc.player.fallDistance < 0.1 && mc.player.motion.y < -0.1) {
            if (elytra != -2) {
                mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
            }
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));

            if (elytra != -2) {
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
            }
        }
    }

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.getValue()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(ActionEvent action) {
        if  (mode.is("Matrix Hard")) {
            if (strafes()) {
                handleStrafesEventAction(action);
            }
            if (strafeMovement.isNeedSwap()) {
                handleNeedSwapEventAction(action);
            }
        }
    }

    private void handleEventMove(EventMoving eventMove) {
        int elytraSlot = InventoryUtility.getInstance().getHotbarSlotOfItem();

        if (elytra.getValue() && elytraSlot != -1) {
            if (MoveUtility.isMoving() && !mc.player.onGround && mc.player.fallDistance >= 0.15 && eventMove.isToGround()) {
                MoveUtility.setMotion(setSpeed.getValue());
                strafeMovement.setOldSpeed(setSpeed.getValue() / 1.06);
            }
        }

        if (mc.player.isInWater() || mc.player.isInLava()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
        if (mode.is("Matrix Hard")) {
            if (onlyGround.getValue())
                if (!mc.player.isOnGround()) return;

            if (strafes()) {
                handleStrafesEventMove(eventMove);
            } else {
                strafeMovement.setOldSpeed(0);
            }
        }

        if (mode.is("Matrix")) {
            if (waterTicks > 0) return;
            if (MoveUtility.isMoving() && MoveUtility.getMotion() <= 0.289385188) {
                if (!eventMove.isToGround()) {
                    MoveUtility.setStrafe(MoveUtility.reason(false) || mc.player.isHandActive() ? MoveUtility.getMotion() - 0.00001f : 0.245f - (new Random().nextFloat() * 0.000001f));
                }
            }
        }
    }

    private void handleEventPostMove(EventPostMove eventPostMove) {
        strafeMovement.postMove(eventPostMove.getHorizontalMove());
    }

    private void handleEventPacket(EventPacket packet) {

        if (packet.getType() == EventPacket.Type.RECEIVE) {
            if (damageBoost.getValue()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    private void handleStrafesEventAction(ActionEvent action) {
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    private void handleStrafesEventMove(EventMoving eventMove) {
        if (targetStrafe.isEnabled() && (hitAura.isEnabled() && hitAura.getTarget() != null)) {
            return;
        }

        if (damageBoost.getValue())
            this.damageUtil.time(700L);

        final float damageSpeed = boostSpeed.getValue().floatValue() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.getValue(), damageUtil.isNormalDamage(), false, damageSpeed);

        MoveUtility.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(ActionEvent action) {
        action.setSprintState(!mc.player.serverSprintState);
        strafeMovement.setNeedSwap(false);
    }

    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }

    }

    public boolean strafes() {
        if (isInvalidPlayerState()) {
            return false;
        }
        
		if (mc.player.isInWater() || waterTicks > 0) {
			return false;
		}

        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();

        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }

        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }

        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        super.onEnable();
        betterComp(Evaware.getInst().getModuleManager().getSpeed());
    }
}
