package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.EventMotion;
import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.MouseUtility;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "Spider", category = Category.Movement)
public class Spider extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Grim", "Grim", "Grim 2", "Matrix", "Elytra");
    private final SliderSetting spiderSpeed = new SliderSetting("Скорость", 2.0f, 1.0f, 10.0f, 0.05f).visibleIf(() -> mode.is("Matrix"));
    private final CheckBoxSetting bypass = new CheckBoxSetting("Второй обход", true).visibleIf(() -> mode.is("Elytra"));
    TimerUtility timerUtility = new TimerUtility();
    int oldItem = -1;
    int oldItem1 = -1;
    int i;
    long speed;

    public Spider() {
        addSettings(mode, bypass, spiderSpeed);
    }

    @Subscribe
    private void onMotion(EventMotion motion) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

        switch (mode.getValue()) {
            case "Matrix" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                long speed = MathHelper.clamp(500 - (spiderSpeed.getValue().longValue() / 2 * 100), 0, 500);
                if (timerUtility.isReached(speed)) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.collidedVertically = true;
                    mc.player.collidedHorizontally = true;
                    mc.player.isAirBorne = true;
                    mc.player.jump();
                    timerUtility.reset();
                }
            }
            case "Grim" -> {
                int slotInHotBar = getSlotInInventoryOrHotbar(true);

                if (slotInHotBar == -1) {
                    print("Блоки не найдены!");
                    toggle();
                    return;
                }
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                if (mc.player.isOnGround()) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.jump();
                }
                if (mc.player.fallDistance > 0 && mc.player.fallDistance < 2) {
                    placeBlocks(motion, slotInHotBar);
                }
            }

            case "Grim 2" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                if (!mc.player.isOnGround()) {
                    speed = (long) MathHelper.clamp(500L - spiderSpeed.getValue() / 2L * 100L, 0L, 500L);
                    if (this.timerUtility.isReached(speed)) {
                        mc.gameSettings.keyBindSneak.setPressed(true);
                        motion.setOnGround(true);
                        mc.player.setOnGround(true);
                        mc.player.collidedVertically = true;
                        mc.player.collidedHorizontally = true;
                        mc.player.isAirBorne = true;
                        if (mc.player.fallDistance != 0.0F) {
                            mc.gameSettings.keyBindForward.setPressed(true);
                            mc.gameSettings.keyBindForward.setPressed(false);
                        }

                        if (!mc.player.movementInput.jump) mc.player.jump();
                        mc.gameSettings.keyBindSneak.setPressed(false);
                        this.timerUtility.reset();
                    }
                }
            }

            case "Elytra" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                motion.setPitch(0.0F);
                mc.player.rotationPitchHead = 0.0F;
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

        switch (mode.getValue()) {
            case "Elytra" -> {
                if (!bypass.getValue()) {
                    if (!mc.player.collidedHorizontally) {
                        return;
                    }
                    for (i = 0; i < 9; ++i) {
                        if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ELYTRA && !mc.player.isOnGround() && mc.player.collidedHorizontally && mc.player.fallDistance == 0.0F) {
                            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
                            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                            MoveUtility.setMotion(0.06);
                            mc.player.motion.y = 0.366;
                            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
                            this.oldItem = i;
                        }
                    }
                } else {
                    if ((mc.player.inventory.armorInventory.get(2)).getItem() != Items.ELYTRA && mc.player.collidedHorizontally) {
                        for(i = 0; i < 9; ++i) {
                            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ELYTRA) {
                                mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
                                this.oldItem1 = i;
                                this.timerUtility.reset();
                            }
                        }
                    }

                    if (mc.player.collidedHorizontally) {
                        mc.gameSettings.keyBindJump.setPressed(false);
                        if (this.timerUtility.isReached(180L)) {
                            mc.gameSettings.keyBindJump.setPressed(true);
                        }
                    }

                    if ((mc.player.inventory.armorInventory.get(2)).getItem() == Items.ELYTRA && !mc.player.collidedHorizontally && this.oldItem1 != -1) {
                        mc.playerController.windowClick(0, 6, this.oldItem1, ClickType.SWAP, mc.player);
                        this.oldItem1 = -1;
                    }

                    if ((mc.player.inventory.armorInventory.get(2)).getItem() == Items.ELYTRA && !mc.player.isOnGround() && mc.player.collidedHorizontally) {
                        if (mc.player.fallDistance != 0.0F) {
                            return;
                        }

                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                        MoveUtility.setMotion(0.02);
                        mc.player.motion.y = 0.36;
                    }
                }
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

        switch (mode.getValue()) {
            case "Elytra" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                if (!bypass.getValue()) {
                    IPacket iPacket = e.getPacket();
                    if (iPacket instanceof SPlayerPositionLookPacket) {
                        SPlayerPositionLookPacket p = (SPlayerPositionLookPacket) iPacket;
                        mc.player.func_242277_a(new Vector3d(p.getX(), p.getY(), p.getZ()));
                        mc.player.setRawPosition(p.getX(), p.getY(), p.getZ());
                        return;
                    }

                    if (e.getPacket() instanceof SEntityMetadataPacket && ((SEntityMetadataPacket) e.getPacket()).getEntityId() == mc.player.getEntityId()) {
                        e.cancel();
                    }
                } else {
                    if (e.getPacket() instanceof SEntityMetadataPacket && ((SEntityMetadataPacket) e.getPacket()).getEntityId() == mc.player.getEntityId()) {
                        e.cancel();
                    }
                }
            }
        }
    }

    private void placeBlocks(EventMotion motion, int block) {
        int last = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = block;
        motion.setPitch(80);
        motion.setYaw(mc.player.getHorizontalFacing().getHorizontalAngle());
        BlockRayTraceResult r = (BlockRayTraceResult) MouseUtility.rayTrace(4, motion.getYaw(), motion.getPitch(), mc.player);
        mc.player.swingArm(Hand.MAIN_HAND);
        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, r);
        mc.player.inventory.currentItem = last;
        mc.player.fallDistance = 0;
    }

    public int getSlotInInventoryOrHotbar(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TORCH) {
                continue;
            }

            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }
}
