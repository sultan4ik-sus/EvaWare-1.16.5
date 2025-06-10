package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventMotion;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.BindSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.modules.settings.impl.StringSetting;
import eva.ware.utils.client.KeyStorage;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.PotionUtility;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@ModuleRegister(name = "AutoFix", category = Category.Misc)
public class AutoFix extends Module {

    public ModeSetting mode = new ModeSetting("Тип починки", "Пузырьки", "Пузырьки", "Команда");
    public StringSetting name = new StringSetting("Команда для починки", "/fix all", "Укажите текст для починки").visibleIf(() -> mode.is("Команда"));
    public BindSetting bind = new BindSetting("Кнопка", -1).visibleIf(() -> mode.is("Пузырьки"));
    public SliderSetting delay = new SliderSetting("Задержка", 50, 0, 500, 1).visibleIf(() -> mode.is("Пузырьки"));

    private final TimerUtility timerUtility = new TimerUtility();
    private final TimerUtility throwDelay = new TimerUtility();
    private float previousPitch;
    private int selectedSlot;
    private final PotionUtility potionUtility = new PotionUtility();

    public AutoFix() {
        addSettings(mode, name, bind, delay);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (!mode.is("Пузырьки")) return;

        if (mc.currentScreen != null) return;
        if (isNotPressed()) return;

        if (this.isNotThrowing()) {
            return;
        }
        if (checkFixInv().equals(ItemStack.EMPTY) || (getPotionIndexInv() == -1 && getPotionIndexHb() == -1)) {
            return;
        }

        this.previousPitch = 90.0F;
        e.setPitch(this.previousPitch);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Команда")) {
            if (timerUtility.isReached(1000) && checkFix(mc.player.getHeldItemMainhand())) {
                print(name.getValue());
                timerUtility.reset();
            }
        }

        if (mode.is("Пузырьки")) {
            if (mc.currentScreen != null) return;
            if (isNotPressed()) return;
            if (!checkFixInv().equals(ItemStack.EMPTY)) {
                if (!this.isNotThrowing() && previousPitch == mc.player.lastReportedPitch) {
                    if (throwDelay.isReached(delay.getValue().intValue())) {
                        int oldItem = mc.player.inventory.currentItem;
                        this.selectedSlot = -1;

                        int slot = this.setSlotAndUseItem();
                        if (this.selectedSlot == -1) {
                            this.selectedSlot = slot;
                        }

                        if (this.selectedSlot > 8) {
                            mc.playerController.pickItem(this.selectedSlot);
                        }

                        mc.player.connection.sendPacket(new CHeldItemChangePacket(oldItem));
                        throwDelay.reset();
                    }
                }

                if (timerUtility.isReached(500L)) {
                    try {
                        this.selectedSlot = -2;
                    } catch (Exception ignored) {
                    }
                }

                this.potionUtility.changeItemSlot(this.selectedSlot == -2);
            }
        }
    }

    private boolean isNotPressed() {
        return !KeyStorage.isKeyDown(bind.getValue());
    }

    private int setSlotAndUseItem() {
        int hbSlot = this.getPotionIndexHb();
        if (hbSlot != -1) {
            this.potionUtility.setPreviousSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            PotionUtility.useItem(Hand.MAIN_HAND);
            timerUtility.reset();
            return hbSlot;
        } else {
            int invSlot = this.getPotionIndexInv();
            if (invSlot != -1) {
                this.potionUtility.setPreviousSlot(mc.player.inventory.currentItem);
                mc.playerController.pickItem(invSlot);
                PotionUtility.useItem(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                timerUtility.reset();
                return invSlot;
            } else {
                return -1;
            }
        }
    }

    public boolean isNotThrowing() {
        return !(mc.player.isOnGround() || mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getBoundingBox().minY - 0.3f, mc.player.getPosZ())).isSolid()) || mc.player.isOnLadder() || mc.player.getRidingEntity() != null || mc.player.abilities.isFlying;
    }


    private int getPotionIndexHb() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) return i;
        }

        return -1;
    }

    private int getPotionIndexInv() {
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) return i;
        }

        return -1;
    }

    private boolean checkFix(ItemStack item) {
        return item.getMaxDamage() != 0 && ((item.getMaxDamage() - item.getDamage()) <= 3);
    }

    private ItemStack checkFixInv() {
        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.isEmpty()) continue;
            boolean mending = false;
            if (stack.isEnchanted()) {
                for (int j = 0; j < stack.getEnchantmentTagList().size(); j++) {
                    String stackItemEnchant = stack.getEnchantmentTagList().getCompound(j).getString("id").replaceAll("minecraft:", "");
                    if (stackItemEnchant.equalsIgnoreCase("mending")) {
                        mending = true;
                        break;
                    }
                }
            }
            if (stack.getMaxDamage() != 0 && stack.getDamage() != 0 && mending) return stack;
        }
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            boolean mending = false;
            if (stack.isEnchanted()) {
                for (int j = 0; j < stack.getEnchantmentTagList().size(); j++) {
                    String stackItemEnchant = stack.getEnchantmentTagList().getCompound(j).getString("id").replaceAll("minecraft:", "");
                    if (stackItemEnchant.equalsIgnoreCase("mending")) {
                        mending = true;
                        break;
                    }
                }
            }
            if (stack.getMaxDamage() != 0 && stack.getDamage() != 0 && mending) return stack;
        }
        return ItemStack.EMPTY;
    }
    
}
