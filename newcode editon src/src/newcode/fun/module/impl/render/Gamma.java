package newcode.fun.module.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

import java.util.Calendar;

@Annotation(name = "Gamma", type = TypeList.Render, desc = "Динамическое управление освещением в зависимости от времени суток")
public class Gamma extends Module {
    private final Minecraft mc = Minecraft.getInstance();

    ModeSetting lightingMode = new ModeSetting("Режим освещения", "Динамичный", "Динамичный", "Фиксированный", "Ночное зрение")
            .onChange(newValue -> onLightingModeChanged());

    public SliderSetting gammaSlider = new SliderSetting("Яркость (Gamma)", 1.0f, 0.1f, 2.0f, 0.01f).setVisible(() -> lightingMode.is("Фиксированный"));
    public BooleanOption dynamicLighting = new BooleanOption("Динамическое освещение", true);
    public BooleanOption enableNightVision = new BooleanOption("Ночное зрение", true);

    public Gamma() {
        addSettings(lightingMode, gammaSlider);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            applyLightingMode();
            if (lightingMode.is("Ночное зрение") && enableNightVision.get()) {
                mc.player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
            }
        }
        return false;
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    private void applyLightingMode() {
        resetGamma();
        if (lightingMode.is("Динамичный")) {
            applyDynamicLighting();
        } else if (lightingMode.is("Фиксированный")) {
            applyFixedLighting();
        }
    }

    private void applyDynamicLighting() {
        float timeOfDay = mc.world.getDayTime() % 24000;
        float dayNightCycle = Math.abs(timeOfDay - 12000) / 12000.0f;

        if (dynamicLighting.get()) {
            float gamma = 1.0f + dayNightCycle * 1.5f;
            mc.gameSettings.gamma = gamma;
        }
    }

    private void applyFixedLighting() {
        mc.gameSettings.gamma = gammaSlider.getValue().floatValue();
    }

    @Override
    protected void onDisable() {
        resetGamma();
        super.onDisable();
    }

    private void resetGamma() {
        mc.gameSettings.gamma = 1.0f;
        mc.player.removePotionEffect(Effects.NIGHT_VISION);
    }

    private void onLightingModeChanged() {
        resetGamma();
        applyLightingMode();
    }

    private String getTimeString() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
}
