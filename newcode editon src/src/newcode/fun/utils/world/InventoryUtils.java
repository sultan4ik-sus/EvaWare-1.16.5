package newcode.fun.utils.world;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import newcode.fun.control.Manager;
import newcode.fun.module.impl.combat.AttackAura;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.IMinecraft;

import static net.minecraft.client.Minecraft.player;

public class InventoryUtils implements IMinecraft {

    public static int getHotBarSlot(Item input) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == input) {
                return i;
            }
        }
        return -1;
    }

    public static int getTrident() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof TridentItem) {
                return i;
            }
        }
        return -1;
    }

    public static int getItem(Item item, boolean hotbar) {
        for (int i = 0; i < (hotbar ? 9 : 45); ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int getItemSlot(Item input) {
        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() == input) {
                return -2;
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static boolean moveItem(final int from, final int to, final boolean air) {
        if (from == to) {
            return air;
        }
        pickupItem(from, 0);
        pickupItem(to, 0);
        if (air) {
            pickupItem(from, 0);
        }
        return air;
    }

    public static void pickupItem(int slot, int button) {
        mc.playerController.windowClick(0, slot, button, ClickType.PICKUP, mc.player);
    }

    public static void dropItem(int slot) {
        mc.playerController.windowClick(0, slot, 0, ClickType.THROW, mc.player);
    }

    public static int getAxe(boolean hotBar) {
        int startSlot = hotBar ? 0 : 9;
        int endSlot = hotBar ? 9 : 36;

        for (int i = startSlot; i < endSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() instanceof AxeItem) {
                return i;
            }
        }

        return -1;
    }

    public static boolean doesHotbarHaveItem(Item item) {
        for(int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    public static int findFreeSlot() {
        for (int i = 0; i < 36; i++) { 
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static void antipolet(Item item, boolean rotation) {
        if (InventoryHelper.getItemIndex(item) != -1) {
            for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                    int originalSlot = i;
                    mc.playerController.windowClick(0, (i < 9 ? 36 + i : i), 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, (i < 9 ? 36 + i : i), 0, ClickType.PICKUP, mc.player);

                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                                mc.gameSettings.keyBindSneak.setPressed(true);
                            }
                        }
                    }, 20);

                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                mc.gameSettings.keyBindSneak.setPressed(false);
                            }
                        }
                    }, 60);

                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(0, (originalSlot < 9 ? 36 + originalSlot : originalSlot), 0, ClickType.PICKUP, mc.player);
                        }
                    }, 110);
                    mc.playerController.windowClick(0, (originalSlot < 9 ? 36 + originalSlot : originalSlot), 0, ClickType.PICKUP, mc.player);

                    break;
                }
            }
        }
    }

    public static void inventorySwapAxe(boolean rotation) {
        Item[] axes = {
                Items.NETHERITE_AXE,
                Items.GOLDEN_AXE,
                Items.IRON_AXE,
                Items.DIAMOND_AXE,
                Items.STONE_AXE,
                Items.WOODEN_AXE
        };

        for (Item axe : axes) {
            if (InventoryHelper.getItemIndex(axe) != -1) {
                int i;
                if (!doesHotbarHaveItem(axe)) {
                    for (i = 0; i < 36; ++i) {
                        if (mc.player.inventory.getStackInSlot(i).getItem() == axe) {
                            mc.playerController.windowClick(0, i, mc.player.inventory.currentItem % 8, ClickType.SWAP, mc.player);
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8));

                            if (rotation && AttackAura.target != null) {
                                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, false));
                            }

                            int finalI = i;
                            new java.util.Timer().schedule(new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                                    mc.playerController.windowClick(0, finalI, mc.player.inventory.currentItem % 8, ClickType.SWAP, mc.player);
                                }
                            }, 60);
                            break;
                        }
                    }
                }
            }
        }
    }


    public static void inventorySwapClick(Item item, boolean rotation) {
        if (InventoryHelper.getItemIndex(item) != -1) {
            int i;
            if (doesHotbarHaveItem(item)) {
                for (i = 0; i < 9; i++) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                        if (i != mc.player.inventory.currentItem) {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
                        }

                        if (rotation && Manager.FUNCTION_MANAGER.auraFunction.target != null) {
                            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, false));
                        }

                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

                        if (i != mc.player.inventory.currentItem) {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                        }
                        break;
                    }
                }
            }


            if (!doesHotbarHaveItem(item)) {
                for (i = 0; i < 36; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                        mc.playerController.windowClick(0, i, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));

                        if (rotation && AttackAura.target != null) {
                            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, false));
                        }

                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        if (ClientUtils.isConnectedToServer("funtime") || ClientUtils.isConnectedToServer("metahvh")) {
                            int finalI = i;
                            new java.util.Timer().schedule(new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                                    mc.playerController.windowClick(0, finalI, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                                }
                            }, 370);
                        } else {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                            mc.playerController.windowClick(0, i, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        }
                        break;
                    }
                }
            }
        }
    }
}
