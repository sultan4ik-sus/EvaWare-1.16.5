package newcode.fun.module.impl.render;

import net.minecraft.potion.Effects;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventOverlaysRender;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;

@Annotation(name = "NoRender", type = TypeList.Render)
public class NoRender extends Module {

    public MultiBoxSetting element = new MultiBoxSetting("��������",
            new BooleanOption("������� ����", true),
            new BooleanOption("������ �������", true),
            new BooleanOption("���� ���", false),
            new BooleanOption("��������", false),
            new BooleanOption("���������", false),
            new BooleanOption("�����", false),
            new BooleanOption("�����", true),
            new BooleanOption("������ ����", true));

    public NoRender() {
        addSettings(element);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventOverlaysRender) {
            handleEventOverlaysRender((EventOverlaysRender) event);
        } else if (event instanceof EventUpdate) {
            handleEventUpdate((EventUpdate) event);
        }
        return false;
    }

    private void handleEventOverlaysRender(EventOverlaysRender event) {
        EventOverlaysRender.OverlayType overlayType = event.getOverlayType();

        boolean cancelOverlay = switch (overlayType) {
            case FIRE_OVERLAY -> element.get(0);
            case BOSS_LINE -> element.get(2);
            case SCOREBOARD -> element.get(3);
            case TITLES -> element.get(4);
            case TOTEM -> element.get(5);
        };

        if (cancelOverlay) {
            event.setCancel(true);
        }
    }

    private void handleEventUpdate(EventUpdate event) {

        boolean isRaining = element.get(6) && mc.world.isRaining();

        boolean hasEffects = element.get(1) &&
                (mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.NAUSEA));

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
