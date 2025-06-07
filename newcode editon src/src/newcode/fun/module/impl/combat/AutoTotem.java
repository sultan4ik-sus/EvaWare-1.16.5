package newcode.fun.module.impl.combat;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventCrystalEntity;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.world.InventoryUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Annotation(name = "AutoTotem",
        type = TypeList.Combat, desc = "Берёт тотем при низком хп"
)
public class AutoTotem extends Module {
    private final SliderSetting health;
    private final SliderSetting healthelytra;
    private final SliderSetting healthbronya;
    private final MultiBoxSetting mode;
    private final TimerUtil stopWatch;
    private Item backItem;
    private ItemStack backItemStack;
    private int nonEnchantedTotems;
    private boolean totemIsUsed;
    private int itemInMouse;
    private int totemCount;
    private int oldItem;

    public AutoTotem() {
        this.health = new SliderSetting("Здоровье", 4.0f, 1.0f, 20.0f, 0.5f);
        this.healthelytra = new SliderSetting("Здоровье на элитрах", 7.5f, 1.0f, 20.0f, 0.5f);
        this.healthbronya = new SliderSetting("Без полной брони", 7.5f, 1.0f, 20.0F, 0.5F);
        this.mode = new MultiBoxSetting("Учитывать", new BooleanOption[] { new BooleanOption("Поглощение", true), new BooleanOption("Кристаллы", true), new BooleanOption("Якорь", false), new BooleanOption("Падение", true), new BooleanOption("Не брать драконида", true), new BooleanOption("Не брать если шар", true), new BooleanOption("Бэкать предметы", true), new BooleanOption("Сохранять зачар", false)});
        this.stopWatch = new TimerUtil();
        this.backItem = Items.AIR;
        this.itemInMouse = -1;
        this.totemCount = 0;
        this.oldItem = -1;
        this.addSettings(mode, this.health, healthelytra, healthbronya);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (event instanceof final EventCrystalEntity eventCrystalEntity) {
            final Entity entity2 = eventCrystalEntity.getEntity();
            if (entity2 instanceof final EnderCrystalEntity entity) {
                if (this.mode.get(1) && entity.getDistance(AutoTotem.mc.player) <= 6.0f) {
                    this.swapToTotem();
                }
            }
        }
        if (event instanceof EventUpdate) {
            this.totemCount = this.countTotems(true);

            this.nonEnchantedTotems = (int) IntStream.range(0, 36).mapToObj(i -> AutoTotem.mc.player.inventory.getStackInSlot(i)).filter(s -> s.getItem() == Items.TOTEM_OF_UNDYING && !s.isEnchanted()).count();
            final int slot = this.getSlotInInventory();
            final boolean handNotNull = !(AutoTotem.mc.player.getHeldItemOffhand().getItem() instanceof AirItem);
            if (this.shouldToSwapTotem()) {
                if (slot != -1 && !this.isTotemInHands()) {
                    InventoryUtils.moveItem(slot, 45, handNotNull);
                    if (handNotNull && this.oldItem == -1) {
                        this.oldItem = slot;
                    }
                }
            }
            else if (this.oldItem != -1 && this.mode.get(6)) {
                InventoryUtils.moveItem(this.oldItem, 45, handNotNull);
                this.oldItem = -1;
            }
        }
        if (event instanceof final EventPacket eventPacket) {
            if (eventPacket.isReceivePacket()) {
                final IPacket packet = eventPacket.getPacket();
                if (packet instanceof final SEntityStatusPacket statusPacket) {
                    if (statusPacket.getOpCode() == 35 && statusPacket.getEntity(AutoTotem.mc.world) == AutoTotem.mc.player) {
                        this.totemIsUsed = true;
                    }
                }
            }
        }
        return false;
    }

    private void swapToTotem() {
        final int totemSlot = this.getSlotInInventory();
        this.stopWatch.reset();
        final Item mainHandItem = AutoTotem.mc.player.getHeldItemOffhand().getItem();
        if (mainHandItem == Items.TOTEM_OF_UNDYING) {
            return;
        }
        if (totemSlot == -1 && !isCurrentItem(Items.TOTEM_OF_UNDYING)) {
            return;
        }
        if (this.itemInMouse == -1) {
            this.itemInMouse = totemSlot;
            this.backItem = mainHandItem;
            this.backItemStack = AutoTotem.mc.player.getHeldItemOffhand().copy();
        }
        AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.container.windowId, totemSlot, 1, ClickType.PICKUP, AutoTotem.mc.player);
        AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.container.windowId, 45, 1, ClickType.PICKUP, AutoTotem.mc.player);
        if (this.totemCount > 1 && this.totemIsUsed) {
            this.backItemInMouse();
            this.totemIsUsed = false;
        }
        this.backItemInMouse();
    }

    private int countTotems(final boolean includeEnchanted) {
        long totemCount = 0L;
        for (int inventorySize = AutoTotem.mc.player.inventory.getSizeInventory(), slotIndex = 0; slotIndex < inventorySize; ++slotIndex) {
            final ItemStack slotStack = AutoTotem.mc.player.inventory.getStackInSlot(slotIndex);
            if (slotStack.getItem() == Items.TOTEM_OF_UNDYING && (includeEnchanted || !slotStack.isEnchanted())) {
                ++totemCount;
            }
        }
        return (int)totemCount;
    }

    private void backItemInMouse() {
        if (this.itemInMouse != -1) {
            AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.container.windowId, this.itemInMouse, 0, ClickType.PICKUP, AutoTotem.mc.player);
        }
    }

    public static boolean isCurrentItem(final Item item) {
        return AutoTotem.mc.player.inventory.getItemStack().getItem() == item;
    }

    private boolean isTotemInHands() {
        final Hand[] values;
        final Hand[] hands = values = Hand.values();
        for (final Hand hand : values) {
            final ItemStack heldItem = AutoTotem.mc.player.getHeldItem(hand);
            if (heldItem.getItem() == Items.TOTEM_OF_UNDYING && !this.isSaveEnchanted(heldItem)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSaveEnchanted(final ItemStack itemStack) {
        return this.mode.get(7) && itemStack.isEnchanted() && this.nonEnchantedTotems > 0;
    }

    private boolean shouldToSwapTotem() {
        final float absorptionAmount = AutoTotem.mc.player.isPotionActive(Effects.ABSORPTION) ? AutoTotem.mc.player.getAbsorptionAmount() : 0.0f;
        float currentHealth = AutoTotem.mc.player.getHealth();
        if (this.mode.get(0)) {
            currentHealth += absorptionAmount;
        }

        boolean hasFullArmor = true;
        for (ItemStack armor : AutoTotem.mc.player.inventory.armorInventory) {
            if (armor.isEmpty()) {
                hasFullArmor = false;
                break;
            }
        }

        float healthThreshold = hasFullArmor ? this.health.getValue().floatValue() : this.healthbronya.getValue().floatValue();

        return (!this.isOffhandItemBall() && this.isInDangerousSituation()) || currentHealth <= (mc.player.isElytraFlying() ? healthelytra.getValue().floatValue() : healthThreshold) || checkFall();
    }

    private float calculateFallDamage(float fallDistance) {
        if (fallDistance <= 3.0f) return 0;

        float fallDamage = (fallDistance - 3.0f) / 2;

        float armorReduction = 0;
        for (ItemStack armor : mc.player.inventory.armorInventory) {
            if (armor.getItem() instanceof ArmorItem) {
                armorReduction += ((ArmorItem) armor.getItem()).getDamageReductionAmount();
            }
        }

        ItemStack boots = mc.player.inventory.armorInventory.get(0);
        if (boots.getItem() instanceof ArmorItem) {
            int featherFallingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FEATHER_FALLING, boots);
            if (featherFallingLevel > 0) {
                float reductionFactor = 1.0f - (Math.min(featherFallingLevel, 4) * 0.171f);
                fallDamage *= reductionFactor;
            }
        }

        if (hasProtectionAura()) {
            fallDamage *= 0.2f;
        }

        float absorption = mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f;
        fallDamage = Math.max(0, fallDamage - absorption);

        return Math.min(fallDamage, mc.player.getMaxHealth());
    }

    private boolean hasProtectionAura() {
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.hasDisplayName() && "Аура Защиты От Падения".equals(stack.getDisplayName().getString())) {
                return true;
            }
        }
        return false;
    }

    private boolean isInDangerousSituation() {
        return this.checkCrystal() || this.checkAnchor() || this.checkFall();
    }

    private boolean checkFall() {
        if (!this.mode.get(3)) {
            return false;
        }
        if (AutoTotem.mc.player.isInWater() || AutoTotem.mc.player.isElytraFlying()) {
            return false;
        }
        float fallDistance = AutoTotem.mc.player.fallDistance;
        float fallDamage = calculateFallDamage(fallDistance);

        float healthThreshold = this.health.getValue().floatValue();
        float currentHealth = AutoTotem.mc.player.getHealth();

        return fallDamage >= currentHealth / 2.005f;
    }

    private boolean checkAnchor() {
        return this.mode.get(2) && this.getBlock(6.0f, Blocks.RESPAWN_ANCHOR) != null;
    }

    private boolean checkCrystal() {
        if (!this.mode.get(1)) {
            return false;
        }
        for (final Entity entity : AutoTotem.mc.world.getAllEntities()) {
            if (this.isDangerousEntityNearPlayer(entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOffhandItemBall() {
        final boolean isFallingConditionMet = this.mode.get(3) && AutoTotem.mc.player.fallDistance > 5.0f;
        return !isFallingConditionMet && this.mode.get(5) && AutoTotem.mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD;
    }

    private boolean isDangerousEntityNearPlayer(final Entity entity) {
        return (entity instanceof TNTEntity || entity instanceof TNTMinecartEntity) && AutoTotem.mc.player.getDistance(entity) <= 6.0f;
    }

    private BlockPos getBlock(final float distance, final Block block) {
        return this.getSphere(this.getPlayerPosLocal(), distance, 6, false, true, 0).stream().filter(position -> AutoTotem.mc.world.getBlockState(position).getBlock() == block).min(Comparator.comparing(blockPos -> this.getDistanceOfEntityToBlock(AutoTotem.mc.player, blockPos))).orElse(null);
    }

    private List<BlockPos> getSphere(final BlockPos center, final float radius, final int height, final boolean hollow, final boolean fromBottom, final int yOffset) {
        final List<BlockPos> positions = new ArrayList<BlockPos>();
        final int centerX = center.getX();
        final int centerY = center.getY();
        final int centerZ = center.getZ();
        for (int x = centerX - (int)radius; x <= centerX + radius; ++x) {
            for (int z = centerZ - (int)radius; z <= centerZ + radius; ++z) {
                final int yStart = fromBottom ? (centerY - (int)radius) : centerY;
                for (int yEnd = fromBottom ? (centerY + (int)radius) : (centerY + height), y = yStart; y < yEnd; ++y) {
                    if (isPositionWithinSphere(centerX, centerY, centerZ, x, y, z, radius, hollow)) {
                        positions.add(new BlockPos(x, y + yOffset, z));
                    }
                }
            }
        }
        return positions;
    }

    private BlockPos getPlayerPosLocal() {
        if (AutoTotem.mc.player == null) {
            return BlockPos.ZERO;
        }
        return new BlockPos(Math.floor(AutoTotem.mc.player.getPosX()), Math.floor(AutoTotem.mc.player.getPosY()), Math.floor(AutoTotem.mc.player.getPosZ()));
    }

    private double getDistanceOfEntityToBlock(final Entity entity, final BlockPos blockPos) {
        return this.getDistance(entity.getPosX(), entity.getPosY(), entity.getPosZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private double getDistance(final double n, final double n2, final double n3, final double n4, final double n5, final double n6) {
        final double n7 = n - n4;
        final double n8 = n2 - n5;
        final double n9 = n3 - n6;
        return MathHelper.sqrt(n7 * n7 + n8 * n8 + n9 * n9);
    }

    private static boolean isPositionWithinSphere(final int centerX, final int centerY, final int centerZ, final int x, final int y, final int z, final float radius, final boolean hollow) {
        final double distanceSq = Math.pow(centerX - x, 2.0) + Math.pow(centerZ - z, 2.0) + Math.pow(centerY - y, 2.0);
        return distanceSq < Math.pow(radius, 2.0) && (!hollow || distanceSq >= Math.pow(radius - 1.0f, 2.0));
    }

    private int getSlotInInventory() {
        for (int i = 0; i < 36; ++i) {
            final ItemStack itemStack = AutoTotem.mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
                if (mode.get(4) && itemStack.hasDisplayName() && "Тотем Драконида".equals(itemStack.getDisplayName().getString())) {
                    continue;
                }
                if (!this.isSaveEnchanted(itemStack)) {
                    return this.adjustSlotNumber(i);
                }
            }
        }
        return -1;
    }


    private int adjustSlotNumber(final int slot) {
        return (slot < 9) ? (slot + 36) : slot;
    }

    public void onDisable() {
        this.oldItem = -1;
        super.onDisable();
    }
}

