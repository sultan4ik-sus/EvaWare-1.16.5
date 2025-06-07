package newcode.fun.module.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventInput;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.control.events.impl.player.EventObsidianPlace;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.math.MathUtil;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.move.MoveUtil;
import newcode.fun.utils.world.InventoryUtils;

import java.util.List;

@Annotation(name = "AutoExplosion",
        type = TypeList.Combat, desc = "Автоматически взрывает кристалл"
)
public class AutoExplosion extends Module {
    private BlockPos position = null;
    private Entity crystalEntity = null;
    private int oldSlot = -1;
    private final TimerUtil timerUtil = new TimerUtil();
    private final BooleanOption saveSelf = new BooleanOption("Не взрыв себя", false);
    public final BooleanOption correction = new BooleanOption("Корект движения", true);
    public final BooleanOption saveItem = new BooleanOption("Не взрыв предметы", true);
    private final SliderSetting speedplace = (new SliderSetting("Задержка взрыва", 2F, 1F, 5F, 0.5F));

    public AutoExplosion() {
        addSettings(saveSelf, saveItem, correction, speedplace);
    }

    public Vector2f server;

    public boolean check() {
        return server != null && correction.get() && crystalEntity != null && position != null && state;
    }

    @Override
    public boolean onEvent(Event event) {
        if (isPlayerDead()) {
            server = null;
            position = null;
            crystalEntity = null;
            oldSlot = -1;
        }
        if (event instanceof EventInput e) {
            if (check()) {
                MoveUtil.fixMovement(e, server.x);
            }
        }
        if (event instanceof EventObsidianPlace obsidianPlace) {
            handleObsidianPlace(obsidianPlace.getPos());
        } else if (event instanceof EventUpdate updateEvent) {
            handleUpdateEvent(updateEvent);
        } else if (event instanceof EventMotion motionEvent) {
            handleMotionEvent(motionEvent);
        }
        return false;
    }

    private void handleUpdateEvent(EventUpdate updateEvent) {
        ensureEndCrystalsInHotbar();

        if (position != null) {
            List<Entity> crystals = mc.world.getEntitiesWithinAABBExcludingEntity(null,
                            new AxisAlignedBB(position.getX(),
                                    position.getY(),
                                    position.getZ(),
                                    position.getX() + 1.0,
                                    position.getY() + 2.0,
                                    position.getZ() + 1.0))
                    .stream()
                    .filter(entity -> entity instanceof EnderCrystalEntity)
                    .toList();

            crystals.forEach(this::attackEntity);
        }

        if (crystalEntity != null && !crystalEntity.isAlive()) {
            crystalEntity = null;
            position = null;
            server = null;
        }
    }

    private void ensureEndCrystalsInHotbar() {
        if (getSlotWithCrystal() == -1) {
            InventoryUtils.inventorySwapClick(Items.END_CRYSTAL, false);
        }
    }

    private void handleObsidianPlace(BlockPos position) {
        final int crystalSlot = getSlotWithCrystal();

        this.oldSlot = mc.player.inventory.currentItem;

        if (crystalSlot == -1 || position == null) {
            return;
        }

        mc.player.inventory.currentItem = crystalSlot;

        BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(
                new Vector3d(position.getX() + 0.5f, position.getY() + 0.5f, position.getZ() + 0.5f),
                Direction.UP,
                position,
                false
        );

        if (mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, rayTraceResult)
                == ActionResultType.SUCCESS) {
            mc.player.swingArm(Hand.MAIN_HAND);
        }
        if (oldSlot != -1)
            mc.player.inventory.currentItem = this.oldSlot;
        this.oldSlot = -1;
        this.position = position;
    }

    private void handleMotionEvent(EventMotion motionEvent) {
        if (!isValid(crystalEntity)) {
            return;
        }

        server = MathUtil.rotationToEntity(crystalEntity);

        motionEvent.setYaw(server.x);
        motionEvent.setPitch(server.y);
        mc.player.rotationYawHead = server.x;
        mc.player.renderYawOffset = server.x;
        mc.player.rotationPitchHead = server.y;
    }

    private void attackEntity(Entity base) {
        if (!isValid(base)
                || mc.player.getCooledAttackStrength(1) < 1) {
            return;
        }
        if (timerUtil.hasTimeElapsed(speedplace.getValue().longValue() * 100L)) {
            mc.playerController.attackEntity(mc.player, base);
            mc.player.swingArm(Hand.MAIN_HAND);
            timerUtil.reset();
        }
        crystalEntity = base;
    }

    private int getSlotWithCrystal() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.END_CRYSTAL) {
                return i;
            }
        }
        return -1;
    }

    private boolean isCorrectDistance() {
        if (position == null) {
            return false;
        }
        return mc.player.getPositionVec().distanceTo(
                new Vector3d(position.getX(),
                        position.getY(),
                        position.getZ())) <= mc.playerController.getBlockReachDistance();
    }

    private boolean isValid(Entity base) {
        if (base == null) {
            return false;
        }
        if (position == null) {
            return false;
        }
        if (saveSelf.get() && base.getPosition().getY() <= mc.player.getPosition().getY()) {
            return false;
        }

        if (saveItem.get() && isItemsNearby(base.getPosition())) {
            return false;
        }

        return isCorrectDistance();
    }

    private boolean isItemsNearby(BlockPos position) {
        List<Entity> nearbyEntities = mc.world.getEntitiesWithinAABBExcludingEntity(null,
                new AxisAlignedBB(position.getX() - 5, position.getY(), position.getZ() - 5,
                        position.getX() + 5, position.getY() + 1, position.getZ() + 5));

        for (Entity entity : nearbyEntities) {
            if (entity instanceof ItemEntity itemEntity) {
                if (entity.getPosition().getY() == position.getY()) {
                    if (isProtectedItem(itemEntity)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isProtectedItem(ItemEntity itemEntity) {
        Item item = itemEntity.getItem().getItem();
        return item == Items.GOLDEN_APPLE ||
                item == Items.PLAYER_HEAD ||
                item == Items.NETHERITE_HELMET ||
                item == Items.NETHERITE_CHESTPLATE ||
                item == Items.NETHERITE_LEGGINGS ||
                item == Items.NETHERITE_BOOTS ||
                item == Items.NETHERITE_SWORD ||
                item == Items.ELYTRA ||
                item == Items.FIREWORK_ROCKET ||
                item == Items.DIAMOND_HELMET ||
                item == Items.DIAMOND_CHESTPLATE ||
                item == Items.DIAMOND_LEGGINGS ||
                item == Items.DIAMOND_BOOTS ||
                item == Items.DIAMOND_SWORD ||
                item == Items.ENCHANTED_GOLDEN_APPLE ||
                item == Items.NETHERITE_AXE ||
                item == Items.TOTEM_OF_UNDYING ||
                item == Items.NETHERITE_PICKAXE ||
                item == Items.SHULKER_BOX ||
                item == Items.TRIDENT ||
                item == Items.END_CRYSTAL;
    }


    private boolean isPlayerDead() {
        return mc.player == null || mc.player.getHealth() <= 0;
    }

    @Override
    protected void onDisable() {
        server = null;
        position = null;
        crystalEntity = null;
        oldSlot = -1;
        super.onDisable();
    }
}
