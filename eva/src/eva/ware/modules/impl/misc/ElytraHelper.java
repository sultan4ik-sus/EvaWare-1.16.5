package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventKey;
import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.BindSetting;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.InventoryUtility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TextFormatting;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "ElytraHelper", category = Category.Misc)
public class ElytraHelper extends Module {

    final BindSetting swapChestKey = new BindSetting("Кнопка свапа", -1);
    final BindSetting fireWorkKey = new BindSetting("Кнопка феерверков", -1);

    final CheckBoxSetting autoFireWork = new CheckBoxSetting("Авто феерверк", true);
    final SliderSetting timerFireWork = new SliderSetting("Таймер феера", 400, 100, 2000, 10).visibleIf(() -> autoFireWork.getValue());
    final CheckBoxSetting autoFly = new CheckBoxSetting("Авто взлёт", true);
    final InventoryUtility.Hand handUtil = new InventoryUtility.Hand();

    public ElytraHelper() {
        addSettings(swapChestKey, fireWorkKey, autoFly, autoFireWork, timerFireWork);
    }

    ItemStack currentStack = ItemStack.EMPTY;
    public static TimerUtility timerUtility = new TimerUtility();
    public static TimerUtility fireWorkTimerUtility = new TimerUtility();
    long delay;
    boolean fireworkUsed;

    public TimerUtility wait = new TimerUtility();

    @Subscribe
    private void onEventKey(EventKey e) {
        if (e.getKey() == swapChestKey.getValue() && timerUtility.isReached(100L)) {
            changeChestPlate(currentStack);
            timerUtility.reset();
        }

        if (e.getKey() == fireWorkKey.getValue() && timerUtility.isReached(200L)) {
            if (mc.player.isElytraFlying())
                InventoryUtility.inventorySwapClick(Items.FIREWORK_ROCKET, false);
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (mc.player != null) {
            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint};
            if (ClientUtility.isConnectedToServer("funtime")) {
                if (!wait.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }
        }

        if (autoFly.getValue() && currentStack.getItem() == Items.ELYTRA) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else if (ElytraItem.isUsable(currentStack) && !mc.player.isElytraFlying() && !mc.player.abilities.isFlying) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }

        if (mc.player.isElytraFlying() && autoFireWork.getValue() && !(mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT)) {
            if (fireWorkTimerUtility.isReached(timerFireWork.getValue().longValue())) {
                InventoryUtility.inventorySwapClick(Items.FIREWORK_ROCKET, false);
                fireWorkTimerUtility.reset();
            }
        }

        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handUtil.onEventPacket(e);
    }

    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null) {
            return;
        }
        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                InventoryUtility.moveItem(elytraSlot, 6);
                print(TextFormatting.RED + "Свапнул на элитру!");
                return;
            } else {
                print("Элитра не найдена!");
            }
        }
        int armorSlot = getChestPlateSlot();
        if (armorSlot >= 0) {
            InventoryUtility.moveItem(armorSlot, 6);
            print(TextFormatting.RED + "Свапнул на нагрудник!");
        } else {
            print("Нагрудник не найден!");
        }
    }


    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        timerUtility.reset();
        super.onDisable();
    }

    private int getItemSlot(Item input) {
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
}
