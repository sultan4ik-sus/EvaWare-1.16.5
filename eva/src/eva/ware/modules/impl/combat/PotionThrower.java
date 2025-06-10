package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventMotion;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.MoveUtility;
import eva.ware.utils.player.PotionUtility;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.*;
import net.minecraft.util.Hand;

@ModuleRegister(name = "PotionThrower", category = Category.Combat)
public class PotionThrower extends Module {
    private float previousPitch;
    public boolean isActive, isActivePotion;
    private int selectedSlot;
    private final TimerUtility timerUtility = new TimerUtility();
    private final PotionUtility potionUtility = new PotionUtility();

    public PotionThrower() {
        this.addSettings();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (this.isActive() || !MoveUtility.isBlockUnder(0.5f) || mc.player.isOnGround()) {
            for (PotionType potionType : PotionType.values()) {
                this.isActivePotion = potionType.isEnabled();
            }
        } else {
            this.isActivePotion = false;
        }
        if (this.isActive() && this.previousPitch == mc.player.getLastReportedPitch()) {
            int oldItem = mc.player.inventory.currentItem;
            this.selectedSlot = -1;
            for (PotionType potionType : PotionType.values()) {
                if (!potionType.isEnabled()) continue;
                int slot = this.findPotionSlot(potionType);
                if (this.selectedSlot == -1) {
                    this.selectedSlot = slot;
                }
                this.isActive = true;
            }
            if (this.selectedSlot > 8) {
                mc.playerController.pickItem(this.selectedSlot);
            }
            mc.player.connection.sendPacket(new CHeldItemChangePacket(oldItem));
        }
        if (this.timerUtility.isReached(500L)) {
            try {
                this.reset();
                this.selectedSlot = -2;
            } catch (Exception exception) {
            }
        }
        this.potionUtility.changeItemSlot(this.selectedSlot == -2);
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (!this.isActive() || MoveUtility.isBlockUnder(0.5f) || !mc.player.isOnGround()) {
            return;
        }
        float[] angles = new float[]{mc.player.rotationYaw, 90.0f};
        this.previousPitch = 90.0f;
        e.setYaw(angles[0]);
        e.setPitch(this.previousPitch);
        mc.player.rotationPitchHead = this.previousPitch;
        mc.player.rotationYawHead = angles[0];
        mc.player.renderYawOffset = angles[0];
    }

    private void reset() {
        for (PotionType potionType : PotionType.values()) {
            potionType.setEnabled(this.isPotionActive(potionType));
        }
    }

    private int findPotionSlot(PotionType type) {
        int hbSlot = this.getPotionIndexHb(type.getId());
        if (hbSlot != -1) {
            this.potionUtility.setPreviousSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            PotionUtility.useItem(Hand.MAIN_HAND);
            type.setEnabled(false);
            this.timerUtility.reset();
            return hbSlot;
        }
        int invSlot = this.getPotionIndexInv(type.getId());
        if (invSlot != -1) {
            this.potionUtility.setPreviousSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            PotionUtility.useItem(Hand.MAIN_HAND);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            type.setEnabled(false);
            this.timerUtility.reset();
            return invSlot;
        }
        return -1;
    }

    public boolean isActive() {
        for (PotionType potionType : PotionType.values()) {
            if (!potionType.isEnabled()) continue;
            return true;
        }
        return false;
    }

    private boolean isPotionActive(PotionType type) {
        if (mc.player.isPotionActive(type.getPotion())) {
            this.isActive = false;
            return false;
        }
        return this.getPotionIndexInv(type.getId()) != -1 || this.getPotionIndexHb(type.getId()) != -1;
    }

    private int getPotionIndexHb(int id) {
        for (int i = 0; i < 9; ++i) {
            for (EffectInstance potion : PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() != Effect.get(id) || mc.player.inventory.getStackInSlot(i).getItem() != Items.SPLASH_POTION) continue;
                return i;
            }
        }
        return -1;
    }

    private int getPotionIndexInv(int id) {
        for (int i = 9; i < 36; ++i) {
            for (EffectInstance potion : PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() != Effect.get(id) || mc.player.inventory.getStackInSlot(i).getItem() != Items.SPLASH_POTION) continue;
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        this.isActive = false;
        super.onDisable();
    }

    public enum PotionType {
        STRENGTH(Effects.STRENGTH, 5),
        SPEED(Effects.SPEED, 1),
        FIRE_RESIST(Effects.FIRE_RESISTANCE, 12);

        private final Effect potion;
        private final int id;
        private boolean enabled;

        private PotionType(Effect potion, int potionId) {
            this.potion = potion;
            this.id = potionId;
        }

        public Effect getPotion() {
            return this.potion;
        }

        public int getId() {
            return this.id;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean var1) {
            this.enabled = var1;
        }
    }
}
