package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import eva.ware.Evaware;
import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.misc.SelfDestruct;
import eva.ware.modules.settings.impl.*;
import eva.ware.ui.notify.impl.WarningNotify;
import eva.ware.utils.render.color.ColorUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.ResourceLocation;

@ModuleRegister(name = "WorldTweaks", category = Category.Visual)
public class WorldTweaks extends Module {

    public static ModeListSetting options = new ModeListSetting("Опции",
            new CheckBoxSetting("Маленький игрок", false),
            new CheckBoxSetting("Визуальный скин", true),
            new CheckBoxSetting("Кастомный туман", true),
            new CheckBoxSetting("Своя дистанция тумана", true),
            new CheckBoxSetting("Физика предметов", true),
            new CheckBoxSetting("Время", true)
    );

    public static SliderSetting fogDistance = new SliderSetting("Дистация тумана", 0.4f, 0.1f, 1, 0.1f).visibleIf(() -> options.is("Своя дистанция тумана").getValue());

    public static ModeSetting mode = new ModeSetting("Вид", "Клиент", "Клиент", "Свой").visibleIf(() -> options.is("Кастомный туман").getValue());

    public static ColorSetting colorFog = new ColorSetting("Цвет тумана", ColorUtility.rgb(255, 255, 255)).visibleIf(() -> mode.is("Свой"));

    private static ModeSetting selfSkinType = new ModeSetting("Текстура", "Boy3", "Boy", "Boy2", "Boy3", "Boy4", "Girl", "Girl2", "Girl3", "Sonic", "Sonic2", "Sonic3", "Shadow", "Knuckles", "Pink", "Pink2", "Pink3", "Pink4", "Pink5", "Sora", "Cimol", "Cimol2", "Cimol3", "Cimol4", "Cimol5", "Cimol6", "Cimol7").visibleIf(() -> options.is("Визуальный скин").getValue());

    private static ModeSetting time = new ModeSetting("Время", "Ночь", "Ночь", "День").visibleIf(() -> options.is("Время").getValue());

    public static boolean child;


    public WorldTweaks() {
        addSettings(options, fogDistance, selfSkinType, time, mode, colorFog);
    }

    public ResourceLocation updateSkin(ResourceLocation prevResource, Entity entity) {
        if (isEnabled() && entity instanceof PlayerEntity && entity == mc.player) {
            if (options.is("Визуальный скин").getValue() && !SelfDestruct.unhooked) {
                prevResource = new ResourceLocation("eva/images/skins/" + selfSkinType.getValue().toLowerCase() + ".png");
            }
        }
        return prevResource;
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket p) {
            if (isEnabled("Время")) {
                if (time.getValue().equalsIgnoreCase("День"))
                    p.worldTime = 1000L;
                else
                    p.worldTime = 18000L;
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if ((!isEnabled("Маленький игрок") &&
                !isEnabled("Визуальный скин") &&
                !isEnabled("Время")
        )) {
            toggle();
            Evaware.getInst().getNotifyManager().add(0, new WarningNotify("Включите что-нибудь!", 3000));
        }

        if (isEnabled("Маленький игрок")) {
            child = true;
        } else {
            child = false;
        }
    }

    public boolean isEnabled(String name) {
        return options.is(name).getValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        child = false;
    }
}
