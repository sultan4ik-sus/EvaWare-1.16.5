package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventCancelOverlay;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.potion.Effects;

/* ДОДЕЛАТЬ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

@ModuleRegister(name = "NoRender", category = Category.Visual)
public class NoRender extends Module {

    public ModeListSetting element = new ModeListSetting("Удалять",
            new CheckBoxSetting("Огонь на экране", true),
            new CheckBoxSetting("Линия босса", false),
            new CheckBoxSetting("Анимация тотема", true),
            new CheckBoxSetting("Тайтлы", false),
            new CheckBoxSetting("Таблица", false),
            new CheckBoxSetting("Туман", true),
            new CheckBoxSetting("Тряску камеры", true),
            new CheckBoxSetting("Плохие эффекты", true),
            new CheckBoxSetting("Дождь", true),
            new CheckBoxSetting("Камера клип", true),
            new CheckBoxSetting("Броня", false),
            new CheckBoxSetting("Плащ", false),
            new CheckBoxSetting("Эффект свечения", true),
            new CheckBoxSetting("Эффект воды", true),
            new CheckBoxSetting("Трава", true),
            new CheckBoxSetting("Партиклы", false)
            );

    public NoRender() {
        addSettings(element);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        handleEventUpdate(e);
    }

    @Subscribe
    private void onEventCancelOverlay(EventCancelOverlay e) {
        handleEventOverlaysRender(e);
    }

    private void handleEventOverlaysRender(EventCancelOverlay event) {
        boolean cancelOverlay = switch (event.overlayType) {
            case FIRE_OVERLAY -> element.is("Огонь на экране").getValue();
            case BOSS_LINE -> element.is("Линия босса").getValue();
            case SCOREBOARD -> element.is("Таблица").getValue();
            case TITLES -> element.is("Тайтлы").getValue();
            case TOTEM -> element.is("Анимация тотема").getValue();
            case FOG -> element.is("Туман").getValue();
            case HURT -> element.is("Тряску камеры").getValue();
            case UNDER_WATER -> element.is("Эффект воды").getValue();
            case CAMERA_CLIP -> element.is("Камера клип").getValue();
            case ARMOR -> element.is("Броня").getValue();
        };

        if (cancelOverlay) {
            event.cancel();
        }
    }

    private void handleEventUpdate(EventUpdate event) {
        boolean isRaining = mc.world.isRaining() && element.is("Дождь").getValue();

        boolean hasEffects = (mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.NAUSEA)) && element.is("Плохие эффекты").getValue();

        if (isRaining) {
            mc.world.setRainStrength(0);
            mc.world.setThunderStrength(0);
        }

        if (hasEffects) {
            mc.player.removePotionEffect(Effects.NAUSEA);
            mc.player.removePotionEffect(Effects.BLINDNESS);
        }
    }
}
