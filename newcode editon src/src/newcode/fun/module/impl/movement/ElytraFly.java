package newcode.fun.module.impl.movement;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.world.InventoryUtils;

@Annotation(
        name = "ElytraFly",
        type = TypeList.Movement, desc = "Полёт использующий элитры через инвентарь"
)
public class ElytraFly extends Module {
    private final TimerUtil timerUtil = new TimerUtil();
    private final TimerUtil timerUtil1 = new TimerUtil();
    private final TimerUtil timerUtil2 = new TimerUtil();
    private final SliderSetting timerStartFireWork = new SliderSetting("Задержка фейер", 400.0F, 50.0F, 1500.0F, 1.0F);
    private final ModeSetting mode = new ModeSetting("Обход", "Новый", new String[]{"Новый", "Безопасный"});
    int oldItem = -1;
    private final TimerUtil swapBackTimer = new TimerUtil();
    private boolean swapBackPending = false;
    private ItemStack oldStack = null;

    public ElytraFly() {
        this.addSettings(new Setting[]{this.mode, this.timerStartFireWork});
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (InventoryUtils.getItemSlot(Items.FIREWORK_ROCKET) == -1) {
                return false;
            }
            if (mode.is("Новый")) {
                int timeSwap = 550;
                for (int i = 0; i < 9; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ELYTRA && !mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isElytraFlying()) {
                        if (this.timerUtil1.hasTimeElapsed((long) timeSwap)) {
                            this.timerUtil2.reset();
                            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
                            mc.player.startFallFlying();
                            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
                            this.oldItem = i;
                            this.timerUtil1.reset();
                        }

                        if (this.timerUtil.hasTimeElapsed((long) this.timerStartFireWork.getValue().intValue()) && mc.player.isElytraFlying()) {
                            if (mc.player.isHandActive() && !mc.player.isBlocking()) return false;
                            this.useFirework();
                            this.timerUtil.reset();
                        }
                    }
                }
            } else {
                if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.jump();
                }
                if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
                    if (mc.player.fallDistance != 0.0F && !mc.player.isElytraFlying()) {
                        mc.player.startFallFlying();
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                    }
                }
                if (timerUtil.hasTimeElapsed(800L)) {
                    manageElytraFlight();
                    timerUtil.reset();
                }
                if (swapBackPending && swapBackTimer.hasTimeElapsed(200L)) {
                    swapBack();
                    swapBackPending = false;
                }
            }
        }
        return false;
    }
    private void manageElytraFlight() {
        ItemStack currentChestItem = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        int elytraSlot = InventoryUtils.getItemSlot(Items.ELYTRA);

        if (reasonToEquipElytra(currentChestItem) && elytraSlot != -1) {
            this.oldStack = currentChestItem.copy();
            InventoryUtils.moveItem(elytraSlot, 6, true);
            mc.player.startFallFlying();
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));

            useFirework();

            swapBackTimer.reset();
            swapBackPending = true;
        }
    }

    private void swapBack() {
        if (this.oldStack != null) {
            int oldStackSlot = InventoryUtils.getItemSlot(this.oldStack.getItem());
            if (oldStackSlot != -1) {
                InventoryUtils.moveItem(oldStackSlot, 6, true);
                this.oldStack = null;
            }
        }
    }

    private boolean reasonToEquipElytra(ItemStack stack) {
        return stack.getItem() != Items.ELYTRA;
    }

    public boolean getItemNoHotbar(Item item) {
        for(int i = 9; i < 36; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    private void useFirework() {
        if (InventoryUtils.getItemSlot(Items.FIREWORK_ROCKET) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствуют фейерверки!");
        } else {
            InventoryUtils.inventorySwapClick(Items.FIREWORK_ROCKET, false);
        }
    }

    public void onDisable() {
        super.onDisable();
        if (mode.is("Новый")) {
            if (this.oldItem != -1) {
                if (((ItemStack) mc.player.inventory.armorInventory.get(2)).getItem() == Items.ELYTRA && mc.player.inventory.getStackInSlot(this.oldItem).getItem() instanceof ArmorItem) {
                    mc.playerController.windowClick(0, 6, this.oldItem, ClickType.SWAP, mc.player);
                }

                this.oldItem = -1;
            }

            mc.gameSettings.keyBindSneak.setPressed(false);
        } else {
            swapBack();
        }
    }
}
