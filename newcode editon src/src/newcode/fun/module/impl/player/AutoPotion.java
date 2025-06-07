package newcode.fun.module.impl.player;

import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.*;
import net.minecraft.util.Hand;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.world.PotionUtils;
import java.util.function.Supplier;

import static net.minecraft.client.Minecraft.player;

@Annotation(name = "AutoPotion", type = TypeList.Player)
public class AutoPotion extends Module {
    private static MultiBoxSetting potions = new MultiBoxSetting("Бафать",
            new BooleanOption("Силу", true),
            new BooleanOption("Скорость", true),
            new BooleanOption("Огнестойкость", true));
    private BooleanOption autoDisable = new BooleanOption("Авто выключение", false);
    private BooleanOption onlyPvP = new BooleanOption("Только в PVP",false);
    public boolean isActive;
    private int selectedSlot;
    private float previousPitch;
    private TimerUtil time = new TimerUtil();
    private PotionUtils potionUtil = new PotionUtils();

    public AutoPotion() {
        addSettings(potions, onlyPvP, autoDisable);
    }

    public boolean isActivePotion;

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (player.isElytraFlying() || (player.isHandActive() && !player.isBlocking())) {
                return false;
            }
            if (this.isActive() && this.shouldUsePotion()) {
                for (PotionType potionType : PotionType.values()) {
                    isActivePotion = potionType.isEnabled();
                }
            } else {
                isActivePotion = false;
            }

            if (this.isActive() && this.shouldUsePotion() && previousPitch == player.getLastReportedPitch()) {
                int oldItem = player.inventory.currentItem;
                this.selectedSlot = -1;

                for (PotionType potionType : PotionType.values()) {
                    if (potionType.isEnabled()) {
                        int slot = this.findPotionSlot(potionType);
                        if (this.selectedSlot == -1) {
                            this.selectedSlot = slot;
                        }

                        this.isActive = true;
                    }
                }

                if (this.selectedSlot > 8) {
                    mc.playerController.pickItem(this.selectedSlot);
                }

                player.connection.sendPacket(new CHeldItemChangePacket(oldItem));
            }

            if (time.hasTimeElapsed(500L)) {
                try {
                    this.reset();
                    this.selectedSlot = -2;
                } catch (Exception ignored) {
                }
            }

            this.potionUtil.changeItemSlot(this.selectedSlot == -2);
            if (this.autoDisable.get() && this.isActive && this.selectedSlot == -2) {
                this.setState(false);
                this.isActive = false;
            }
        }
        if (event instanceof EventMotion e) {
            if (!this.isActive() || !this.shouldUsePotion()) {
                return false;
            }

            float[] angles = new float[]{player.rotationYaw, 90.0F};
            this.previousPitch = 90.0F;
            e.setYaw(angles[0]);
            e.setPitch(this.previousPitch);
            player.rotationPitchHead = this.previousPitch;
            player.rotationYawHead = angles[0];
            player.renderYawOffset = angles[0];
        }
        return false;
    }

    private boolean shouldUsePotion() {
        return !(onlyPvP.get() && !ClientUtils.isPvP());
    }

    private void reset() {
        for (PotionType potionType : PotionType.values()) {
            if (potionType.isPotionSettingEnabled().get()) {
                potionType.setEnabled(this.isPotionActive(potionType));
            }
        }
    }

    private int findPotionSlot(PotionType type) {
        int hbSlot = this.getPotionIndexHb(type.getPotionId());
        if (hbSlot != -1) {
            this.potionUtil.setPreviousSlot(player.inventory.currentItem);
            player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            PotionUtils.useItem(Hand.MAIN_HAND);
            type.setEnabled(false);
            time.reset();
            return hbSlot;
        } else {
            int invSlot = this.getPotionIndexInv(type.getPotionId());
            if (invSlot != -1) {
                this.potionUtil.setPreviousSlot(player.inventory.currentItem);
                mc.playerController.pickItem(invSlot);
                PotionUtils.useItem(Hand.MAIN_HAND);
                player.connection.sendPacket(new CHeldItemChangePacket(player.inventory.currentItem));
                type.setEnabled(false);
                time.reset();
                return invSlot;
            } else {
                return -1;
            }
        }
    }

    public boolean isActive() {
        for (PotionType potionType : PotionType.values()) {
            if (potionType.isPotionSettingEnabled().get() && potionType.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPotionActive(PotionType type) {
        if (player.isPotionActive(type.getPotion())) {
            this.isActive = false;
            return false;
        } else {
            return this.getPotionIndexInv(type.getPotionId()) != -1 || this.getPotionIndexHb(type.getPotionId()) != -1;
        }
    }

    private int getPotionIndexHb(int id) {
        for (int i = 0; i < 9; ++i) {
            for (EffectInstance potion : net.minecraft.potion.PotionUtils.getEffectsFromStack(player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() == Effect.get(id) && player.inventory.getStackInSlot(i).getItem() == Items.SPLASH_POTION) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getPotionIndexInv(int id) {
        for (int i = 9; i < 36; ++i) {
            for (EffectInstance potion : net.minecraft.potion.PotionUtils.getEffectsFromStack(player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() == Effect.get(id) && player.inventory.getStackInSlot(i).getItem() == Items.SPLASH_POTION) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    protected void onDisable() {
        isActive = false;
        super.onDisable();
    }

    enum PotionType {
        STRENGHT(Effects.STRENGTH, 5, () -> potions.get(0)),
        SPEED(Effects.SPEED, 1, () -> potions.get(1)),
        FIRE_RESIST(Effects.STRENGTH, 12, () -> potions.get(2));

        private final Effect potion;
        private final int potionId;
        private final Supplier<Boolean> potionSetting;
        private boolean enabled;

        PotionType(Effect potion, int potionId, Supplier<Boolean> potionSetting) {
            this.potion = potion;
            this.potionId = potionId;
            this.potionSetting = potionSetting;
        }

        public Effect getPotion() {
            return this.potion;
        }

        public int getPotionId() {
            return this.potionId;
        }

        public Supplier<Boolean> isPotionSettingEnabled() {
            return this.potionSetting;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean var1) {
            this.enabled = var1;
        }

    }
}