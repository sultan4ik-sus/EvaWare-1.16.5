package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import eva.ware.manager.friend.FriendManager;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.AttackUtility;
import eva.ware.utils.player.InventoryUtility;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

@ModuleRegister(name = "TriggerBot", category = Category.Combat)
public class TriggerBot extends Module {

    private final CheckBoxSetting onlyCrit = new CheckBoxSetting("Только криты", true);
    private final CheckBoxSetting smartCrit = new CheckBoxSetting("Умные криты", true).visibleIf(() -> onlyCrit.getValue());
    private final CheckBoxSetting players = new CheckBoxSetting("Игроки", true);
    private final CheckBoxSetting mobs = new CheckBoxSetting("Мобы", false);
    private final CheckBoxSetting animals = new CheckBoxSetting("Животные", false);
    private final CheckBoxSetting shieldBreak = new CheckBoxSetting("Ломать щит", true);
    private final CheckBoxSetting tpsSync = new CheckBoxSetting("TPSSync", false);

    public TriggerBot() {
        addSettings(onlyCrit, smartCrit, players, mobs, animals, shieldBreak, tpsSync);
    }

    private final TimerUtility timerUtility = new TimerUtility();
    private final TimerUtility timerForTarget = new TimerUtility();
    @Getter
    LivingEntity target = null;


    @Subscribe
    public void onUpdate(EventUpdate e) {
        Entity entity = getValidEntity();

        target = (LivingEntity) entity;

        if (entity == null || mc.player == null) {
            return;
        }

        if (shouldAttack()) {
            timerUtility.setLastMS(500);
            attackEntity(entity);
        }
    }

    private boolean shouldAttack() {
        return AttackUtility.isPlayerFalling(onlyCrit.getValue(), smartCrit.getValue(), tpsSync.getValue(), true) && (timerUtility.hasTimeElapsed());
    }

    private void attackEntity(Entity entity) {
        boolean shouldStopSprinting = false;
        if (onlyCrit.getValue() && CEntityActionPacket.lastUpdatedSprint) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            shouldStopSprinting = true;
        }

        mc.playerController.attackEntity(mc.player, entity);
        mc.player.swingArm(Hand.MAIN_HAND);
        if (shieldBreak.getValue() && entity instanceof PlayerEntity)
            breakShieldPlayer(entity);

        if (shouldStopSprinting) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
        }
    }

    private Entity getValidEntity() {
        if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
            if (checkEntity((LivingEntity) entity)) {
                return entity;
            }
        }
        return null;
    }

    public static void breakShieldPlayer(Entity entity) {
        if (((LivingEntity) entity).isBlocking()) {
            int invSlot = InventoryUtility.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtility.getInstance().getAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtility.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);

                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }

            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }

    private boolean checkEntity(LivingEntity entity) {
        if (FriendManager.isFriend(entity.getName().getString())) {
            return false;
        }

        AttackUtility entitySelector = new AttackUtility();

        if (players.getValue()) {
            entitySelector.apply(AttackUtility.EntityType.PLAYERS);
        }
        if (mobs.getValue()) {
            entitySelector.apply(AttackUtility.EntityType.MOBS);
        }
        if (animals.getValue()) {
            entitySelector.apply(AttackUtility.EntityType.ANIMALS);
        }
        return entitySelector.ofType(entity, entitySelector.build()) != null && entity.isAlive();
    }

    @Override
    public void onDisable() {
        timerUtility.reset();
        timerForTarget.reset();
        target = null;
        super.onDisable();
    }
}
