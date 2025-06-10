package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventCooldown;
import eva.ware.events.EventKey;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.BindSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.InventoryUtility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.*;
import net.minecraft.potion.Effects;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "AutoSwap", category = Category.Combat)
public class AutoSwap extends Module {
    final ModeSetting swapMode = new ModeSetting("Тип", "Умный", "Умный", "По бинду");
    final ModeSetting itemType = new ModeSetting("Предмет", "Щит", "Щит", "Геплы", "Тотем", "Шар");
    final ModeSetting swapType = new ModeSetting("Свапать на", "Геплы", "Щит", "Геплы", "Тотем", "Шар");

    final BindSetting keyToSwap = new BindSetting("Кнопка", -1).visibleIf(() -> swapMode.is("По бинду"));
    final SliderSetting health = new SliderSetting("Здоровье", 11.0F, 5.0F, 19.0F, 0.5F).visibleIf(() -> swapMode.is("Умный"));
    final TimerUtility timerUtility = new TimerUtility();
    boolean shieldIsCooldown;
    int oldItem = -1;

    final TimerUtility delay = new TimerUtility();
    final AutoTotem autoTotem;

    public AutoSwap(AutoTotem autoTotem) {
        this.autoTotem = autoTotem;
        addSettings(swapMode, itemType, swapType, keyToSwap, health);
    }

    @Subscribe
    public void onEventKey(EventKey e) {
        if (!swapMode.is("По бинду")) {
            return;
        }

        ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
        boolean isOffhandNotEmpty = !(offhandItemStack.getItem() instanceof AirItem);

        if (e.isKeyDown(keyToSwap.getValue()) && timerUtility.isReached(200)) {
            Item currentItem = offhandItemStack.getItem();
            boolean isHoldingSwapItem = currentItem == getSwapItem();
            boolean isHoldingSelectedItem = currentItem == getSelectedItem();
            int selectedItemSlot = getSlot(getSelectedItem());
            int swapItemSlot = getSlot(getSwapItem());

            if (selectedItemSlot >= 0) {
                if (!isHoldingSelectedItem) {
                    InventoryUtility.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                    timerUtility.reset();
                    return;
                }
            }
            if (swapItemSlot >= 0) {
                if (!isHoldingSwapItem) {
                    InventoryUtility.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                    timerUtility.reset();
                }
            }
        }
    }

    @Subscribe
    private void onCooldown(EventCooldown e) {
        shieldIsCooldown = isCooldown(e);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!swapMode.is("Умный")) {
            return;
        }

        Item currentItem = mc.player.getHeldItemOffhand().getItem();

        if (timerUtility.isReached(400L)) {
            swapIfShieldIsBroken(currentItem);
            swapIfHealthToLow(currentItem);
            timerUtility.reset();
        }
        boolean isRightClickWithGoldenAppleActive = false;

        if (currentItem == Items.GOLDEN_APPLE && !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE)) {
            isRightClickWithGoldenAppleActive = mc.gameSettings.keyBindUseItem.isKeyDown();
        }


        if (isRightClickWithGoldenAppleActive) {
            timerUtility.reset();
        }
    }

    @Override
    public void onDisable() {
        shieldIsCooldown = false;
        oldItem = -1;
        super.onDisable();
    }

    private void swapIfHealthToLow(Item currentItem) {
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);

        int goldenAppleSlot = getSlot(getSwapItem());

        if (shieldIsCooldown || !gappleIsNotCooldown) {
            return;
        }

        if (isLowHealth() && !isHoldingGoldenApple && isHoldingSelectedItem) {
            InventoryUtility.moveItem(goldenAppleSlot, 45, isOffhandNotEmpty);
            if (isOffhandNotEmpty && oldItem == -1) {
                oldItem = goldenAppleSlot;
            }
        } else if (!isLowHealth() && isHoldingGoldenApple && oldItem >= 0) {
            InventoryUtility.moveItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private void swapIfShieldIsBroken(Item currentItem) {
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);
        int goldenAppleSlot = getSlot(getSwapItem());

        if (shieldIsCooldown && !isHoldingGoldenApple && isHoldingSelectedItem && gappleIsNotCooldown) {
            InventoryUtility.moveItem(goldenAppleSlot, 45, isOffhandNotEmpty);
            if (isOffhandNotEmpty && oldItem == -1) {
                oldItem = goldenAppleSlot;
            }
            print(shieldIsCooldown + "");
        } else if (!shieldIsCooldown && isHoldingGoldenApple && oldItem >= 0) {
            InventoryUtility.moveItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private boolean isLowHealth() {
        float currentHealth = mc.player.getHealth() + (mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f);
        return currentHealth <= health.getValue();
    }

    private boolean isCooldown(EventCooldown cooldown) {
        Item item = cooldown.getItem();


        if (!itemType.is("Shield")) {
            return false;
        } else {
            return cooldown.isAdded() && item instanceof ShieldItem;
        }
    }

    private Item getSwapItem() {
        return getItemByType(swapType.getValue());
    }

    private Item getSelectedItem() {
        return getItemByType(itemType.getValue());
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Шар" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }

    private int getSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                if (mc.player.inventory.getStackInSlot(i).isEnchanted()) {
                    finalSlot = i;
                    break;
                } else {
                    finalSlot = i;
                }
            }
        }
        if (finalSlot < 9 && finalSlot != -1) {
            finalSlot = finalSlot + 36;
        }
        return finalSlot;
    }
}