package newcode.fun.module.impl.player;

import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventCalculateCooldown;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.ClientUtils;

import java.util.*;
import java.util.function.Supplier;

@Annotation(name = "ItemsCooldown", type = TypeList.Player)
public class ItemsCooldown extends Module {

    public static final MultiBoxSetting items = new MultiBoxSetting("Предметы",
            new BooleanOption("Геплы", true),
            new BooleanOption("Перки", false),
            new BooleanOption("Хорусы", false),
            new BooleanOption("Чарки", false));

    public static final SliderSetting gappleTime = new SliderSetting("Кулдаун гепла", 4.5F, 1.0F, 10.0F, 0.05F)
            .setVisible(() -> items.get(0));
    private static final SliderSetting pearlTime = new SliderSetting("Кулдаун перок", 14.05F, 1.0F, 15.0F, 0.05F)
            .setVisible(() -> items.get(1));
    private static final SliderSetting horusTime = new SliderSetting("Кулдаун хорусов", 2.3F, 1.0F, 10.0F, 0.05F)
            .setVisible(() -> items.get(2));
    private static final SliderSetting enchantmentGappleTime = new SliderSetting("Кулдаун чарок", 4.5F, 1.0F, 10.0F, 0.05F)
            .setVisible(() -> items.get(3));
    public BooleanOption visuals = new BooleanOption("Визуализировать", true);

    public HashMap<Item, Long> lastUseItemTime = new HashMap<>();

    public ItemsCooldown() {
        addSettings(items, gappleTime, enchantmentGappleTime, pearlTime, horusTime, visuals);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventCalculateCooldown calculateCooldown) {
            applyGoldenAppleCooldown(calculateCooldown);
        }
        return false;
    }

    private void applyGoldenAppleCooldown(EventCalculateCooldown calcCooldown) {
        List<Item> itemsToRemove = new ArrayList<>();

        for (Map.Entry<Item, Long> entry : lastUseItemTime.entrySet()) {
            ItemEnum itemEnum = ItemEnum.getItemEnum(entry.getKey());

            if (itemEnum == null || calcCooldown.itemStack != itemEnum.getItem() || !itemEnum.getActive().get() || isNotPvP()) {
                continue;
            }

            long time = System.currentTimeMillis() - entry.getValue();
            float timeSetting = itemEnum.getTime().get() * 1000.0F;

            if (time < timeSetting && itemEnum.getActive().get()) {
                calcCooldown.setCooldown(time / timeSetting);
            } else {
                itemsToRemove.add(itemEnum.getItem());
            }
        }

        itemsToRemove.forEach(lastUseItemTime::remove);
    }

    public boolean isNotPvP() {
        return !ClientUtils.isPvP();
    }

    public boolean isCurrentItem(ItemEnum item) {
        if (!item.getActive().get()) {
            return false;
        }

        return item.getActive().get() && Arrays.stream(ItemEnum.values()).anyMatch(e -> e == item);
    }

    @Getter
    public enum ItemEnum {
        CHORUS(Items.CHORUS_FRUIT,
                () -> items.get(2),
                () -> horusTime.getValue().floatValue()),
        GOLDEN_APPLE(Items.GOLDEN_APPLE,
                () -> items.get(0),
                () -> gappleTime.getValue().floatValue()),
        ENCHANTED_GOLDEN_APPLE(Items.ENCHANTED_GOLDEN_APPLE,
                () -> items.get(3),
                () -> enchantmentGappleTime.getValue().floatValue()),
        ENDER_PEARL(Items.ENDER_PEARL,
                () -> items.get(1),
                () -> pearlTime.getValue().floatValue());

        private final Item item;
        private final Supplier<Boolean> active;
        private final Supplier<Float> time;


        ItemEnum(Item item, Supplier<Boolean> active, Supplier<Float> time) {
            this.item = item;
            this.active = active;
            this.time = time;
        }

        public static ItemEnum getItemEnum(Item item) {
            return Arrays.stream(ItemEnum.values())
                    .filter(e -> e.getItem() == item)
                    .findFirst()
                    .orElse(null);
        }
    }

}