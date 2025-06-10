package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "MoveHelper", category = Category.Movement)
public class MoveHelper extends Module {

    private final List<IPacket<?>> packet = new ArrayList<>();
    public TimerUtility timerUtilityInv = new TimerUtility();
    boolean eActiveNoSlow = false;

    public TimerUtility timerUtil = new TimerUtility();
    public TimerUtility timerUtil1 = new TimerUtility();
    boolean closeMe = false;

    public final CheckBoxSetting noJumpDelay = new CheckBoxSetting("NoJumpDelay", true);
    public final CheckBoxSetting dragonFly = new CheckBoxSetting("DragonFly", false);
    public final CheckBoxSetting invMove = new CheckBoxSetting("InvMove", true);
    public final CheckBoxSetting noSlow = new CheckBoxSetting("NoSlow", true);
    public final ModeSetting noSlowMode = new ModeSetting("NoSlow Mode", "Matrix", "Matrix", "Grim", "GrimNew", "GrimLatest", "Damage").visibleIf(() -> noSlow.getValue());

    public final CheckBoxSetting noWeb = new CheckBoxSetting("NoWeb", false);
    public ModeSetting noWebMode = new ModeSetting("NoWeb Mode", "Motion", "Motion", "Matrix").visibleIf(() -> noWeb.getValue());
    public SliderSetting jumpMotion = new SliderSetting("Сила прыжка", 0, 0, 10, 0.5f).visibleIf(() -> noWebMode.is("Matrix"));
    public SliderSetting speed = new SliderSetting("Скорость", 1, 0.1f, 2, 0.2f).visibleIf(() -> noWeb.getValue());


    public final CheckBoxSetting levitationControl = new CheckBoxSetting("LevitationControl", false);
    public ModeSetting lcMode = new ModeSetting("LC Mode", "Control", "Remove", "Control").visibleIf(() -> levitationControl.getValue());
    public SliderSetting moveUp = new SliderSetting("Скорость вверх", 1, 1, 5, 0.1f).visibleIf(() -> lcMode.is("Control") && levitationControl.getValue());
    public SliderSetting moveDown = new SliderSetting("Скорость вниз", 1, 1, 5, 0.1f).visibleIf(() -> lcMode.is("Control") && levitationControl.getValue());

    public MoveHelper() {
        addSettings(noJumpDelay, invMove, dragonFly, noSlow, noSlowMode, noWeb, noWebMode, jumpMotion, speed, levitationControl, lcMode, moveUp, moveDown);
    }

    @Subscribe
    public void onEating(EventNoSlow e) {
        handleEventUpdate(e);
    }

    @Subscribe
    public void onClose(EventInventoryClose e) {
        if (ClientUtility.isConnectedToServer("funtime") && invMove.getValue()) {
            if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MoveUtility.isMoving()) {
                new Thread(() -> {
                    timerUtilityInv.reset();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket p : packet) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                e.cancel();
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (ClientUtility.isConnectedToServer("funtime") && invMove.getValue()) {
            if (e.getPacket() instanceof CClickWindowPacket p && MoveUtility.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }
            }
        }

        if (noSlow.getValue()) {
            if (noSlowMode.is("Damage")) {
                if (e.getPacket() instanceof SEntityVelocityPacket && ((SEntityVelocityPacket)e.getPacket()).getEntityID() == mc.player.getEntityId()) {
                    closeMe = true;
                    timerUtil.reset();
                }

                if (closeMe && timerUtil.hasTimeElapsed(1600, false)) {
                    closeMe = false;
                    timerUtil.reset();
                }
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (levitationControl.getValue()) {
            if (lcMode.is("Control")) {
                if (mc.player.isPotionActive(Effects.LEVITATION)) {
                    int amplifier = mc.player.getActivePotionEffect(Effects.LEVITATION).getAmplifier();
                    if (mc.gameSettings.keyBindJump.pressed)
                        mc.player.motion.y = (((0.05D * (double) (amplifier + 1) - mc.player.motion.y) * 0.2D) * moveUp.getValue());
                    else if (mc.gameSettings.keyBindSneak.pressed)
                        mc.player.motion.y = (-(((0.05D * (double) (amplifier + 1) - mc.player.motion.y) * 0.2D) * moveDown.getValue()));
                    else mc.player.motion.y = (0);
                }
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (invMove.getValue()) {
            if (mc.player != null) {
                final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
                if (ClientUtility.isConnectedToServer("funtime")) {
                    if (!timerUtilityInv.isReached(400)) {
                        for (KeyBinding keyBinding : pressedKeys) {
                            keyBinding.setPressed(false);
                        }
                        return;
                    }
                }


                if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen || mc.currentScreen instanceof AnvilScreen) {
                    return;
                }

                updateKeyBindingState(pressedKeys);
            }
        }

        if (noJumpDelay.getValue()) {
            mc.player.jumpTicks = 0;
        }

        if (noWeb.getValue()) {
            if (noWebMode.is("Motion")) {
                if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() == Blocks.COBWEB) {
                    mc.player.motion.y = 0.0;
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.motion.y = 1.2000000476837158;
                    }

                    if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.motion.y = -1.2000000476837158;
                    }

                    MoveUtility.setMotion(speed.getValue());
                }
            }

            if (noWebMode.is("Matrix")) {
                if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() == Blocks.COBWEB) {
                    mc.player.motion.y += 2;
                } else if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() == Blocks.COBWEB) {
                    mc.player.motion.y += jumpMotion.getValue();
                    MoveUtility.setSpeed(speed.getValue());
                    mc.gameSettings.keyBindJump.pressed = false;
                }
            }
        }

        if (levitationControl.getValue() && lcMode.is("Remove")) {
            if (mc.player.isPotionActive(Effects.LEVITATION)) {
                mc.player.removeActivePotionEffect(Effects.LEVITATION);
            }
        }

        if (dragonFly.getValue()) {
            if (mc.player.abilities.isFlying) {
                MoveUtility.setMotion(1.0);
                mc.player.motion.y = 0.0;
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motion.y = 0.25;
                    if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                        mc.player.motion.y = 0.5;
                    }
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motion.y = -0.25;
                    if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                        mc.player.motion.y = -0.5;
                    }
                }
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }

    private void handleEventUpdate(EventNoSlow eventNoSlow) {
        if (mc.player.isHandActive() && noSlow.getValue()) {
            switch (noSlowMode.getValue()) {
                case "Grim" -> handleGrimMode(eventNoSlow);
                case "Matrix" -> handleMatrixMode(eventNoSlow);
                case "GrimNew" -> handleGrimNewMode(eventNoSlow);
                case "GrimLatest" -> handleHWMode(eventNoSlow);
                case "Dagame" -> handleDamageMode(eventNoSlow);
            }
        } else {
            eActiveNoSlow = false;
            mc.timer.timerSpeed = 1f;
        }
    }

    private void handleDamageMode(EventNoSlow e) {
        if ((mc.player.isInWater() || closeMe) && mc.player.getActiveHand() == Hand.MAIN_HAND && mc.player.getHeldItemOffhand().getUseAction() == UseAction.NONE) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            e.cancel();
        }

        if (mc.player.getActiveHand() == Hand.OFF_HAND && MoveUtility.isMoving()) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            e.cancel();
        }
    }

    private void handleHWMode(EventNoSlow event) {
        boolean mainHandActive;
        boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean bl = mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;
        if ((mc.player.getItemInUseCount() < 28 && mc.player.getItemInUseCount() > 4 || mc.player.getHeldItemOffhand().getItem() == Items.SHIELD) && mc.player.isHandActive() && !mc.player.isPassenger()) {
            mc.playerController.syncCurrentPlayItem();
            if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                int old = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.player.setSprinting(false);
                event.cancel();
            }
            if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                if (mc.player.getHeldItemOffhand().getUseAction().equals((Object)UseAction.NONE)) {
                    event.cancel();
                }
            }
            mc.playerController.syncCurrentPlayItem();
        }
    }

    private void handleMatrixMode(EventNoSlow eventNoSlow) {
        boolean isFalling = (double) mc.player.fallDistance > 0.725;
        float speedMultiplier;
        eventNoSlow.cancel();
        if (mc.player.isOnGround() && !mc.player.movementInput.jump) {
            if (mc.player.ticksExisted % 2 == 0) {
                boolean isNotStrafing = mc.player.moveStrafing == 0.0F;
                speedMultiplier = isNotStrafing ? 0.5F : 0.4F;
                mc.player.motion.x *= speedMultiplier;
                mc.player.motion.z *= speedMultiplier;
            }
        } else if (isFalling) {
            boolean isVeryFastFalling = (double) mc.player.fallDistance > 1.4;
            speedMultiplier = isVeryFastFalling ? 0.95F : 0.97F;
            mc.player.motion.x *= speedMultiplier;
            mc.player.motion.z *= speedMultiplier;
        }
    }

    private void handleGrimNewMode(EventNoSlow e) {
        if ((mc.player.getHeldItemOffhand().getUseAction() != UseAction.BLOCK || mc.player.getActiveHand() != Hand.MAIN_HAND) && (mc.player.getHeldItemOffhand().getUseAction() != UseAction.EAT || mc.player.getActiveHand() != Hand.MAIN_HAND)) {
            if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
                e.cancel();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            } else {
                e.cancel();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            }
        }
    }

    private void handleGrimMode(EventNoSlow noSlow) {
        boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;
        if (!(mc.player.getItemInUseCount() < 25 && mc.player.getItemInUseCount() > 4) && mc.player.getHeldItemOffhand().getItem() != Items.SHIELD) {
            return;
        }
        if (mc.player.isHandActive() && !mc.player.isPassenger()) {
            mc.playerController.syncCurrentPlayItem();
            if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                int old = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.player.setSprinting(false);
                noSlow.cancel();
            }

            if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));

                if (mc.player.getHeldItemOffhand().getUseAction().equals(UseAction.NONE)) {
                    noSlow.cancel();
                }
            }
            mc.playerController.syncCurrentPlayItem();
        }
    }

    private void sendItemChangePacket() {
        ItemStack stack;
        if (!MoveUtility.isMoving()) {
            return;
        }
        int slot = 0;
        do {
            if (++slot == mc.player.inventory.currentItem) {
                ++slot;
            }
            if (slot <= 8) continue;
            slot = -1;
            break;
        } while ((stack = mc.player.inventory.getStackInSlot(slot)).getItem().getUseAction(stack) == UseAction.NONE);
        if (slot == -1) {
            return;
        }
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
    }

    private boolean canCancel() {
        boolean isHandActive = mc.player.isHandActive();
        boolean isLevitation = mc.player.isPotionActive(Effects.LEVITATION);
        boolean isOnGround = mc.player.isOnGround();
        boolean isJumpPressed = mc.gameSettings.keyBindJump.pressed;
        boolean isElytraFlying = mc.player.isElytraFlying();

        if (isLevitation || isElytraFlying) {
            return false;
        }

        return (isOnGround || isJumpPressed) && isHandActive;
    }
}
