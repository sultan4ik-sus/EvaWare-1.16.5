package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventCalculateCooldown;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.client.ClientUtility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.*;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "ItemCooldown", category = Category.Combat)
public class ItemCooldown extends Module {

    public static final ModeListSetting items = new ModeListSetting("Предметы",
            new CheckBoxSetting("Геплы", true),
            new CheckBoxSetting("Перки", true),
            new CheckBoxSetting("Хорусы", true),
            new CheckBoxSetting("Чарки", false));

    static final SliderSetting gappleTime = new SliderSetting("Кулдаун гепла", 4.5F, 1.0F, 10.0F, 0.05F)
            .visibleIf(() -> items.is("Геплы").getValue());
    static final SliderSetting pearlTime = new SliderSetting("Кулдаун перок", 14.05F, 1.0F, 15.0F, 0.05F)
            .visibleIf(() -> items.is("Перки").getValue());
    static final SliderSetting horusTime = new SliderSetting("Кулдаун хорусов", 2.3F, 1.0F, 10.0F, 0.05F)
            .visibleIf(() -> items.is("Хорусы").getValue());
    static final SliderSetting enchantmentGappleTime = new SliderSetting("Кулдаун чарок", 4.5F, 1.0F, 10.0F, 0.05F)
            .visibleIf(() -> items.is("Чарки").getValue());

    private final CheckBoxSetting onlyPvP = new CheckBoxSetting("Только в PVP", true);

    public ItemCooldown() {
        addSettings(items, gappleTime, pearlTime, horusTime, enchantmentGappleTime, onlyPvP);
    }

    public HashMap<Item, Long> lastUseItemTime = new HashMap<>();
    public boolean isCooldown;

    @Subscribe
    public void onCalculateCooldown(EventCalculateCooldown e) {
        applyGoldenAppleCooldown(e);
    }

    private void applyGoldenAppleCooldown(EventCalculateCooldown calcCooldown) {
        List<Item> itemsToRemove = new ArrayList<>();

        for (Map.Entry<Item, Long> entry : lastUseItemTime.entrySet()) {
            ItemEnum itemEnum = ItemEnum.getItemEnum(entry.getKey());

            if (itemEnum == null || calcCooldown.getItemStack() != itemEnum.getItem() || !itemEnum.getActive().get() || isNotPvP()) {
                continue;
            }

            long time = System.currentTimeMillis() - entry.getValue();
            float timeSetting = itemEnum.getTime().get() * 1000.0F;

            if (time < timeSetting && itemEnum.getActive().get()) {
                calcCooldown.setCooldown(time / timeSetting);
                isCooldown = true;
            } else {
                isCooldown = false;
                itemsToRemove.add(itemEnum.getItem());
            }
        }

        itemsToRemove.forEach(lastUseItemTime::remove);
    }

    public boolean isNotPvP() {
        return onlyPvP.getValue() && !ClientUtility.isPvP();
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
                () -> items.is("Хорусы").getValue(),
                horusTime::getValue),
        GOLDEN_APPLE(Items.GOLDEN_APPLE,
                () -> items.is("Геплы").getValue(),
                gappleTime::getValue),
        ENCHANTED_GOLDEN_APPLE(Items.ENCHANTED_GOLDEN_APPLE,
                () -> items.is("Чарки").getValue(),
                enchantmentGappleTime::getValue),
        ENDER_PEARL(Items.ENDER_PEARL,
                () -> items.is("Перки").getValue(),
                pearlTime::getValue);

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
