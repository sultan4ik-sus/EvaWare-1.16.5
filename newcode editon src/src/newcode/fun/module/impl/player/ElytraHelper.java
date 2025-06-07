package newcode.fun.module.impl.player;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.game.EventKey;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BindSetting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.language.Translated;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.world.InventoryUtils;

@Annotation(name = "ElytraHelper", type = TypeList.Player, desc = "Переключает элитру на нагрудник")
public class ElytraHelper extends Module {
    private final BindSetting swapKey = new BindSetting("Свап элитры", 0);
    private final BindSetting fireworkKey = new BindSetting("Фейерверк", 0);
    private static final SliderSetting swapDelay = new SliderSetting("Задержка свапа", 2, 0, 2, 1);
    private final BooleanOption autoFly = new BooleanOption("Авто взлёт", true);
    private final BooleanOption autoJump = new BooleanOption("Авто прыжок", false);
    private static final BooleanOption autoFireWork = new BooleanOption("Авто фейер", false);
    private final TimerUtil timerUtil = new TimerUtil();
    private ItemStack oldStack = null;
    public static SliderSetting autoFireWorkSpeed = new SliderSetting("Задержка фейер", 650f, 300f, 2000f, 50f).setVisible(() -> autoFireWork.get());
    private final TimerUtil stopWatch = new TimerUtil();

    public ElytraHelper() {
        addSettings(swapKey, fireworkKey, swapDelay, autoFireWorkSpeed, autoFly, autoJump, autoFireWork);
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player.isElytraFlying() && autoFireWork.get() && this.timerUtil.hasTimeElapsed(autoFireWorkSpeed.getValue().longValue())) {
                if (mc.player.isHandActive() && !mc.player.isBlocking()) return false;
                this.useFirework();
                this.timerUtil.reset();
            }
            if (this.autoJump.get() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
                if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.jump();
                    return false;
                }
            }
            if (this.autoFly.get() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
                if (mc.player.fallDistance != 0.0F && !mc.player.isElytraFlying()) {
                    mc.player.startFallFlying();
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                }
            }
        }
        if (event instanceof EventKey e) {
            ItemStack itemStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (e.key == this.swapKey.getKey() && stopWatch.hasTimeElapsed(swapDelay.getValue().intValue() * 225)) {
                int elytraSlot = InventoryUtils.getItemSlot(Items.ELYTRA);
                if (elytraSlot == -1) {
                    ClientUtils.sendMessage(Translated.isRussian() ? "You have no " + TextFormatting.RED + "elytra" : "У вас отсутствуют " + TextFormatting.RED + "элитры");
                    return false;
                }
                if (this.reasonToEquipElytra(itemStack)) {
                    ItemStack n = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
                    this.oldStack = n.copy();
                    if (!InventoryUtils.moveItem(elytraSlot, 6, true)) {
                        int freeSlot = InventoryUtils.findFreeSlot();
                        if (freeSlot != -1) {
                            InventoryUtils.moveItem(elytraSlot, freeSlot, true);
                        } else {
                            ClientUtils.sendMessage(Translated.isRussian() ? "Нет свободных слотов для элитр" : "No free slots for elytra");
                        }
                    }
                    stopWatch.reset();
                } else if (this.oldStack != null) {
                    int oldStackSlot = InventoryUtils.getItemSlot(this.oldStack.getItem());
                    if (!InventoryUtils.moveItem(oldStackSlot, 6, true)) {
                        int freeSlot = InventoryUtils.findFreeSlot();
                        if (freeSlot != -1) {
                            InventoryUtils.moveItem(oldStackSlot, freeSlot, true);
                        } else {
                            ClientUtils.sendMessage(Translated.isRussian() ? "Нет свободных слотов для старого предмета" : "No free slots for old item");
                        }
                    }
                    stopWatch.reset();
                }
            }

            if (e.key == this.fireworkKey.getKey() && itemStack.getItem() == Items.ELYTRA) {
                this.useFirework();
            }
        }
        return false;
    }

    private void useFirework() {
        if (InventoryUtils.getItemSlot(Items.FIREWORK_ROCKET) == -1) {
            ClientUtils.sendMessage(Translated.isRussian() ? "You have no " + TextFormatting.RED + "fireworks" : "У вас отсутствуют " + TextFormatting.RED + "фейерверки");
        } else {
            InventoryUtils.inventorySwapClick(Items.FIREWORK_ROCKET, false);
        }
    }

    private boolean reasonToEquipElytra(ItemStack stack) {
        return stack.getItem() != Items.ELYTRA;
    }
}
