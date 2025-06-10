package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.InventoryUtility;
import eva.ware.utils.player.MouseUtility;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Pose;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

@ModuleRegister(name = "LongJump", category = Category.Movement)
public class LongJump extends Module {
    public ModeSetting mode = new ModeSetting("Мод", "Slap", "Slap", "FlagBoost", "InstantLong");

    public LongJump() {
        addSettings(mode);
    }

    boolean placed;
    int counter;
    public TimerUtility slapTimer = new TimerUtility();
    public TimerUtility flagTimer = new TimerUtility();

    @Subscribe
    public void onWorldChange(EventChangeWorld e) {
        toggle();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

        if (mode.is("Slap") && !mc.player.isInWater()) {
            int slot = InventoryUtility.getSlotInInventoryOrHotbar();
            if (slot == -1) {
                print("У вас нет полублоков в хотбаре!");
                toggle();
                return;
            }
            int old = mc.player.inventory.currentItem;
            if (MouseUtility.rayTraceResult(2, mc.player.rotationYaw, 90, mc.player) instanceof BlockRayTraceResult result) {
                if (MoveUtility.isMoving()) {
                    if (mc.player.fallDistance >= 0.8 && mc.world.getBlockState(mc.player.getPosition()).isAir() && !mc.world.getBlockState(result.getPos()).isAir() && mc.world.getBlockState(result.getPos()).isSolid() && !(mc.world.getBlockState(result.getPos()).getBlock() instanceof SlabBlock) && !(mc.world.getBlockState(result.getPos()).getBlock() instanceof StairsBlock)) {
                        mc.player.inventory.currentItem = slot;
                        placed = true;
                        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, result);
                        mc.player.inventory.currentItem = old;
                        mc.player.fallDistance = 0;
                    }
                    mc.gameSettings.keyBindJump.pressed = false;
                    if ((mc.player.isOnGround() && !mc.gameSettings.keyBindJump.pressed) && placed && mc.world.getBlockState(mc.player.getPosition()).isAir() && !mc.world.getBlockState(result.getPos()).isAir() && mc.world.getBlockState(result.getPos()).isSolid() && !(mc.world.getBlockState(result.getPos()).getBlock() instanceof SlabBlock) && slapTimer.isReached(750)) {
                        mc.player.setPose(Pose.STANDING);
                        slapTimer.reset();
                        placed = false;
                    } else if ((mc.player.isOnGround() && !mc.gameSettings.keyBindJump.pressed)) {
                        mc.player.jump();
                        placed = false;
                    }
                }
            } else {
                if ((mc.player.isOnGround() && !mc.gameSettings.keyBindJump.pressed)) {
                    mc.player.jump();
                    placed = false;
                }
            }
        }

        if (mode.is("FlagBoost")) {
            if (mc.player == null || mc.world == null) return;
            if (mc.player.motion.y != -0.0784000015258789) {
                flagTimer.reset();
            }

            if (!MoveUtility.isMoving()) {
                flagTimer.setTime(flagTimer.getTime() + 50L);
            }

            if (flagTimer.isReached(100) && MoveUtility.isMoving()) {
                flagHop();
                mc.player.motion.y = 1.0;
            }
        }

        if (mode.is("InstantLong") && mc.player.hurtTime == 7) {
            MoveUtility.setCuttingSpeed(6.603774070739746);
            mc.player.motion.y = 0.42;
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

        if (mode.is("Slap")) {
            if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
                placed = false;
                counter = 0;
                mc.player.setPose(Pose.STANDING);
            }
        }

        if (mode.is("FlagBoost")) {
            if (e.isReceive()) {
                if (e.getPacket() instanceof SPlayerPositionLookPacket look) {
                    mc.player.setPosition(look.getX(), look.getY(), look.getZ());
                    mc.player.connection.sendPacket(new CConfirmTeleportPacket(look.getTeleportId()));
                    flagHop();
                    e.cancel();
                }
            }
        }
    }

    public void flagHop() {
        mc.player.motion.y = 0.4229;
        MoveUtility.setSpeed(1.953f);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        counter = 0;
        placed = false;
    }


}
