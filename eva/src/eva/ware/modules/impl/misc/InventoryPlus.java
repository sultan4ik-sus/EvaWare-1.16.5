package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.ui.notify.impl.WarningNotify;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CCloseWindowPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@ModuleRegister(name = "InventoryPlus", category = Category.Misc)
public class InventoryPlus extends Module {

    public CheckBoxSetting xcarry = new CheckBoxSetting("XCarry", false);
    public CheckBoxSetting itemScroller = new CheckBoxSetting("ItemScroller", true);
    public CheckBoxSetting autoArmor = new CheckBoxSetting("AutoArmor", true);
    final SliderSetting delay = new SliderSetting("Задержка", 100.0f, 0.0f, 1000.0f, 1.0f).visibleIf(() -> autoArmor.getValue());
    final CheckBoxSetting onlyInv = new CheckBoxSetting("Только в инве", false).visibleIf(() -> autoArmor.getValue());
    final CheckBoxSetting workInMove = new CheckBoxSetting("Работать в движении", true).visibleIf(() -> autoArmor.getValue());
    final TimerUtility timerUtilityAutoArmor = new TimerUtility();

    public InventoryPlus() {
        addSettings(xcarry, itemScroller, autoArmor, delay, onlyInv, workInMove);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!xcarry.getValue() && !itemScroller.getValue() && !autoArmor.getValue()) {
            toggle();
            Evaware.getInst().getNotifyManager().add(0, new WarningNotify("Включите что-нибудь!", 3000));
        }

        if (autoArmor.getValue()) {
            if (!workInMove.getValue()) {
                if (MoveUtility.isMoving()) {
                    return;
                }
            }

            if (onlyInv.getValue()) {
                if (!(mc.currentScreen instanceof InventoryScreen)) {
                    return;
                }
            }
            PlayerInventory inventoryPlayer = AutoActions.mc.player.inventory;
            int[] bestIndexes = new int[4];
            int[] bestValues = new int[4];

            for (int i = 0; i < 4; ++i) {
                bestIndexes[i] = -1;
                ItemStack stack = inventoryPlayer.armorItemInSlot(i);

                if (!isItemValid(stack) || !(stack.getItem() instanceof ArmorItem armorItem)) {
                    continue;
                }

                bestValues[i] = calculateArmorValue(armorItem, stack);
            }

            for (int i = 0; i < 36; ++i) {
                Item item;
                ItemStack stack = inventoryPlayer.getStackInSlot(i);

                if (!isItemValid(stack) || !((item = stack.getItem()) instanceof ArmorItem)) continue;

                ArmorItem armorItem = (ArmorItem) item;
                int armorTypeIndex = armorItem.getSlot().getIndex();
                int value = calculateArmorValue(armorItem, stack);

                if (value <= bestValues[armorTypeIndex]) continue;

                bestIndexes[armorTypeIndex] = i;
                bestValues[armorTypeIndex] = value;
            }

            ArrayList<Integer> randomIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
            Collections.shuffle(randomIndexes);

            for (int index : randomIndexes) {
                int bestIndex = bestIndexes[index];

                if (bestIndex == -1 || (isItemValid(inventoryPlayer.armorItemInSlot(index)) && inventoryPlayer.getFirstEmptyStack() == -1))
                    continue;

                if (bestIndex < 9) {
                    bestIndex += 36;
                }

                if (!this.timerUtilityAutoArmor.isReached(this.delay.getValue().longValue())) break;

                ItemStack armorItemStack = inventoryPlayer.armorItemInSlot(index);

                if (isItemValid(armorItemStack)) {
                    AutoActions.mc.playerController.windowClick(0, 8 - index, 0, ClickType.QUICK_MOVE, AutoActions.mc.player);
                }

                AutoActions.mc.playerController.windowClick(0, bestIndex, 0, ClickType.QUICK_MOVE, AutoActions.mc.player);
                this.timerUtilityAutoArmor.reset();
                break;
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof CCloseWindowPacket && xcarry.getValue()) {
            e.cancel();
        }
    }

    private boolean isItemValid(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    public static int calculateArmorValue(final ArmorItem armor, final ItemStack stack) {
        final int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        final IArmorMaterial armorMaterial = armor.getArmorMaterial();
        final int damageReductionAmount = armorMaterial.getDamageReductionAmount(armor.getEquipmentSlot());
        return ((armor.getDamageReduceAmount() * 20 + protectionLevel * 12 + (int) (armor.getToughness() * 2) + damageReductionAmount * 5) >> 3);
    }
}
