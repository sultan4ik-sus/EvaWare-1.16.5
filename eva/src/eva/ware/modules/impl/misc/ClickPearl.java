package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.combat.ItemCooldown;
import eva.ware.modules.settings.impl.BindSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.InventoryUtility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "ClickPearl", category = Category.Misc)
public class ClickPearl extends Module {
    final ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Легитный");
    final BindSetting pearlKey = new BindSetting("Кнопка", -98);
    final InventoryUtility.Hand handUtil = new InventoryUtility.Hand();
    final ItemCooldown itemCooldown;
    long delay;
    final TimerUtility waitMe = new TimerUtility();
    final TimerUtility timerUtility = new TimerUtility();
    final TimerUtility timerUtility2 = new TimerUtility();
    public ActionType actionType = ActionType.START;
    Runnable runnableAction;
    int oldSlot = -1;

    public ClickPearl(ItemCooldown itemCooldown) {
        this.itemCooldown = itemCooldown;
        addSettings(mode, pearlKey);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == pearlKey.getValue()) {
            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
                final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
                if (ClientUtility.isConnectedToServer("funtime")) {
                    if (!waitMe.isReached(400)) {
                        for (KeyBinding keyBinding : pressedKeys) {
                            keyBinding.setPressed(false);
                        }
                        return;
                    }
                }

                sendRotatePacket();

                oldSlot = mc.player.inventory.currentItem;

                if (mode.is("Обычный")) {
                    InventoryUtility.inventorySwapClick(Items.ENDER_PEARL, true);
                } else {
                    if (runnableAction == null) {
                        actionType = ActionType.START;
                        runnableAction = () -> vebatSoli();
                        timerUtility.reset();
                        timerUtility2.reset();
                    }
                }
            } else {
                ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

                if (itemCooldown.isEnabled() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                    itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
                }
            }
        }
    }

    @Subscribe
    public void onTick(EventTick e) {
        if (runnableAction != null) {
            runnableAction.run();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void vebatSoli() {
        int slot = InventoryUtility.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        Hand hand = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (slot != -1) {
            interact(slot, hand);
        } else {
            runnableAction = null;
        }
    }

    private void swingAndSendPacket(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.player.swingArm(hand);
    }

    private void interact(Integer slot, Hand hand) {
        if (actionType == ActionType.START) { // начало
            switchSlot(slot, hand);
            actionType = ActionType.WAIT;
        } else if (actionType == ActionType.WAIT && timerUtility.isReached(50L)) { // какая та хуйня
            actionType = ActionType.USE_ITEM;
        } else if (actionType == ActionType.USE_ITEM) {
            sendRotatePacket();
            swingAndSendPacket(hand);
            switchSlot(mc.player.inventory.currentItem, hand);
            actionType = ActionType.SWAP_BACK;
        } else if (actionType == ActionType.SWAP_BACK && timerUtility2.isReached(300L)) { // задержка на свап обратно
            mc.player.inventory.currentItem = oldSlot;
            runnableAction = null;
        }
    }

    private void switchSlot(int slot, Hand hand) {
        if (slot != mc.player.inventory.currentItem && hand != Hand.OFF_HAND) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
            mc.player.inventory.currentItem = slot;
        }
    }

    private void sendRotatePacket() {
        if (Evaware.getInst().getModuleManager().getHitAura().getTarget() != null) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
        }
    }

    public enum ActionType {
        START, WAIT, USE_ITEM, SWAP_BACK
    }
}
