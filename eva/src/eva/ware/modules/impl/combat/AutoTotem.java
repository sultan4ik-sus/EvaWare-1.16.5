package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.player.InventoryUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.potion.Effects;

import java.util.Iterator;

@ModuleRegister(name = "AutoTotem", category = Category.Combat)
public class AutoTotem extends Module {
    private final SliderSetting health = new SliderSetting("Здоровье", 3.5F, 1.0F, 20.0F, 0.1F);
    private final CheckBoxSetting swapBack = new CheckBoxSetting("Возвращать предмет", true);
    private final CheckBoxSetting noBallSwitch = new CheckBoxSetting("Не брать если шар", false);
    private final ModeListSetting options = new ModeListSetting("Учитывать",
            new CheckBoxSetting("Золотые сердца", true),
            new CheckBoxSetting("Кристаллы", true),
            new CheckBoxSetting("Падение", true),
            new CheckBoxSetting("Элитру", true)
    );
    private final SliderSetting healthElytra = (new SliderSetting("Здоровье на элитре", 6.0F, 1.0F, 20.0F, 0.5F)).visibleIf(() -> options.is("Элитру").getValue());
    int oldItem = -1;

    public AutoTotem() {
        addSettings(health, healthElytra, swapBack, noBallSwitch, options);
    }

    @Subscribe
    private void handleEventUpdate(EventUpdate event) {
        int slot = InventoryUtility.getItemSlot(Items.TOTEM_OF_UNDYING);
        boolean handNotNull = !(mc.player.getHeldItemOffhand().getItem() instanceof AirItem);
        boolean totemInHand = mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING || mc.player.getHeldItemMainhand().getItem() == Items.TOTEM_OF_UNDYING;
        if (canSwap()) {
            if (slot >= 0 && !totemInHand) {
                mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                if (handNotNull && oldItem == -1) {
                    oldItem = slot;
                }
            }
        } else if (oldItem != -1 && swapBack.getValue()) {
            mc.playerController.windowClickFixed(0, oldItem, 40, ClickType.SWAP, mc.player, 10);
            mc.playerController.windowClickFixed(0, oldItem, 40, ClickType.SWAP, mc.player, 20);
            mc.playerController.windowClickFixed(0, oldItem, 40, ClickType.SWAP, mc.player, 30);
            mc.playerController.windowClickFixed(0, oldItem, 40, ClickType.SWAP, mc.player, 40);
            mc.playerController.windowClickFixed(0, oldItem, 40, ClickType.SWAP, mc.player, 50);
            oldItem = -1;
        }
    }

    private boolean canSwap() {
        float absorption = options.is("Золотые сердца").getValue() && mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0F;
        if (mc.player.getHealth() + absorption <= this.health.getValue().floatValue()) {
            return true;
        } else {
            if (!this.isBall()) {
                if (this.checkCrystal()) {
                    return true;
                }
            }

            return checkElytra() || checkFall();
        }
    }

    private boolean checkElytra() {
        if (!this.options.is("Элитру").getValue()) {
            return false;
        } else {
            return (mc.player.inventory.armorInventory.get(2)).getItem() == Items.ELYTRA && mc.player.getHealth() <= this.healthElytra.getValue().floatValue();
        }
    }

    private boolean checkFall() {
        if (!options.is("Падение").getValue()) {
            return false;
        } else {
            return mc.player.fallDistance > 6;
        }
    }

    private boolean isBall() {
        if (options.is("Падение").getValue() && mc.player.fallDistance > 5.0F) {
            return false;
        } else {
            return noBallSwitch.getValue() && mc.player.getHeldItemOffhand().getItem() instanceof SkullItem;
        }
    }

    private boolean checkCrystal() {
        if (!options.is("Кристаллы").getValue()) {
            return false;
        } else {
            Iterator iterator = mc.world.getAllEntities().iterator();

            Entity entity;
            do {
                do {
                    if (!iterator.hasNext()) {
                        return false;
                    }

                    entity = (Entity)iterator.next();
                    if (entity instanceof EnderCrystalEntity && mc.player.getDistance(entity) <= 6f) {
                        return true;
                    }
                } while(!(entity instanceof TNTEntity) && !(entity instanceof TNTMinecartEntity));
            } while(!(mc.player.getDistance(entity) <= 6f));

            return true;
        }
    }

    private void reset() {
        oldItem = -1;
    }

    @Override
    public void onEnable() {
        reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }
}