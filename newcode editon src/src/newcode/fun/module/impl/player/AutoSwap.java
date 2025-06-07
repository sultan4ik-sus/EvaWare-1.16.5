package newcode.fun.module.impl.player;

import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.game.EventKey;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BindSetting;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.world.InventoryUtils;

@Annotation(name = "AutoSwap", type = TypeList.Player)
public class AutoSwap extends Module {

    private final BindSetting swapKey = new BindSetting("Кнопка свапа", 0);
    private final ModeSetting itemType = new ModeSetting("Свапать с", "Щит", "Щит", "Геплы", "Тотем", "Шар");
    private final ModeSetting swapType = new ModeSetting("Свапать на", "Геплы", "Щит", "Геплы", "Тотем", "Шар");
    private final TimerUtil stopWatch = new TimerUtil();
    private static final SliderSetting swapDelay = new SliderSetting("Задержка свапа", 2, 1, 5, 1);
    public AutoSwap() {
        addSettings(swapKey, itemType, swapType, swapDelay);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (event instanceof EventKey) {
            EventKey e = (EventKey) event;
            ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
            boolean isOffhandNotEmpty = !(offhandItemStack.getItem() instanceof AirItem);

            if (e.key == swapKey.getKey() && stopWatch.hasTimeElapsed(swapDelay.getValue().intValue() * 100)) {
                Item selectedItem = getSelectedItem();
                Item swapItem = getSwapItem();

                int selectedItemSlot = InventoryUtils.getItemSlot(selectedItem);
                int swapItemSlot = InventoryUtils.getItemSlot(swapItem);

                boolean isHoldingSelectedItem = offhandItemStack.getItem() == selectedItem;
                boolean isHoldingSwapItem = offhandItemStack.getItem() == swapItem;

                if (selectedItemSlot >= 0 && swapItemSlot >= 0) {
                    if (!isHoldingSelectedItem) {
                        InventoryUtils.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                        stopWatch.reset();
                        return isOffhandNotEmpty;
                    } else if (!isHoldingSwapItem) {
                        InventoryUtils.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                        stopWatch.reset();
                        return isOffhandNotEmpty;
                    }
                }

                if (isHoldingSelectedItem && isHoldingSwapItem) {
                    if (selectedItemSlot >= 0) {
                        InventoryUtils.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                        stopWatch.reset();
                        return isOffhandNotEmpty;
                    } else if (swapItemSlot >= 0) {
                        InventoryUtils.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                        stopWatch.reset();
                        return isOffhandNotEmpty;
                    }
                }

                if (selectedItemSlot >= 0 || swapItemSlot >= 0) {
                    if (selectedItemSlot >= 0 && !isHoldingSelectedItem) {
                        InventoryUtils.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                        stopWatch.reset();
                        return isOffhandNotEmpty;
                    } else if (swapItemSlot >= 0 && !isHoldingSwapItem) {
                        InventoryUtils.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                        stopWatch.reset();
                        return isOffhandNotEmpty;
                    }
                }
            }
        }
        return false;
    }

    private Item getSwapItem() {
        return getItemByType(swapType.get());
    }

    private Item getSelectedItem() {
        return getItemByType(itemType.get());
    }

    private Item getItemByType(String itemType) {
        switch (itemType) {
            case "Щит": {
                return Items.SHIELD;
            }
            case "Тотем": {
                return Items.TOTEM_OF_UNDYING;
            }
            case "Геплы": {
                return Items.GOLDEN_APPLE;
            }
            case "Шар": {
                return Items.PLAYER_HEAD;
            }
            default: {
                return Items.AIR;
            }
        }
    }
}