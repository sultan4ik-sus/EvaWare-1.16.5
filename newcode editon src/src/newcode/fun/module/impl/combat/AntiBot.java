package newcode.fun.module.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.ColorSetting;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.utils.misc.TimerUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Annotation(
        name = "AntiBot",
        type = TypeList.Combat
)
public class AntiBot extends Module {
    private final ModeSetting mode = new ModeSetting("Выбор режима", "Новый", new String[]{"Старый", "Новый"});
    private final TimerUtil timerUtil = new TimerUtil();
    public static List<Entity> isBot = new ArrayList();
    public AntiBot() {
        this.addSettings(new Setting[]{this.mode});
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (this.mode.is("Новый")) {
                this.newMatrix();
            }

            if (this.mode.is("Старый")) {
                this.oldMatrix();
            }
        }

        return false;
    }

    public void oldMatrix() {
        Iterator var1 = mc.world.getPlayers().iterator();

        while(var1.hasNext()) {
            PlayerEntity entity = (PlayerEntity)var1.next();
            if (!entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(entity.getName().getString())) && !isBot.contains(entity)) {
                isBot.add(entity);
            }
        }

    }

    public void newMatrix() {
        Iterator var1 = mc.world.getPlayers().iterator();

        while(var1.hasNext()) {
            PlayerEntity entity = (PlayerEntity)var1.next();
            if (mc.player != entity && ((ItemStack)entity.inventory.armorInventory.get(0)).getItem() != Items.AIR && ((ItemStack)entity.inventory.armorInventory.get(1)).getItem() != Items.AIR && ((ItemStack)entity.inventory.armorInventory.get(2)).getItem() != Items.AIR && ((ItemStack)entity.inventory.armorInventory.get(3)).getItem() != Items.AIR && ((ItemStack)entity.inventory.armorInventory.get(0)).isEnchantable() && ((ItemStack)entity.inventory.armorInventory.get(1)).isEnchantable() && ((ItemStack)entity.inventory.armorInventory.get(2)).isEnchantable() && ((ItemStack)entity.inventory.armorInventory.get(3)).isEnchantable() && entity.getHeldItemOffhand().getItem() == Items.AIR && (((ItemStack)entity.inventory.armorInventory.get(0)).getItem() == Items.LEATHER_BOOTS || ((ItemStack)entity.inventory.armorInventory.get(1)).getItem() == Items.LEATHER_LEGGINGS || ((ItemStack)entity.inventory.armorInventory.get(2)).getItem() == Items.LEATHER_CHESTPLATE || ((ItemStack)entity.inventory.armorInventory.get(3)).getItem() == Items.LEATHER_HELMET || ((ItemStack)entity.inventory.armorInventory.get(0)).getItem() == Items.IRON_BOOTS || ((ItemStack)entity.inventory.armorInventory.get(1)).getItem() == Items.IRON_LEGGINGS || ((ItemStack)entity.inventory.armorInventory.get(2)).getItem() == Items.IRON_CHESTPLATE || ((ItemStack)entity.inventory.armorInventory.get(3)).getItem() == Items.IRON_HELMET) && entity.getHeldItemMainhand().getItem() != Items.AIR && !((ItemStack)entity.inventory.armorInventory.get(0)).isDamaged() && !((ItemStack)entity.inventory.armorInventory.get(1)).isDamaged() && !((ItemStack)entity.inventory.armorInventory.get(2)).isDamaged() && !((ItemStack)entity.inventory.armorInventory.get(3)).isDamaged() && entity.getFoodStats().getFoodLevel() == 20) {
                if (!isBot.contains(entity)) {
                    isBot.add(entity);
                }

                return;
            }

            if (isBot.contains(entity)) {
                isBot.remove(entity);
            }
        }

    }

    public static boolean checkBot(LivingEntity entity) {
        return entity instanceof PlayerEntity ? isBot.contains(entity) : false;
    }

    public void onDisable() {
        super.onDisable();
        isBot.clear();
    }
}

