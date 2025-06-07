package newcode.fun.module.impl.player;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "FastBreak", type = TypeList.Player)
public class FastBreak extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Пакеты", "Пакеты", "Зелье");
    public SliderSetting speed = new SliderSetting("Лвл", 1, 1, 10, 1).setVisible(() -> mode.is("Зелье"));

    public FastBreak() {
        addSettings(mode, speed);
    }

    @Override
    public boolean onEvent(Event event) {
        if (mode.is("Пакеты")) {
            if (event instanceof EventUpdate) {
                mc.playerController.resetBlockRemoving();
                mc.playerController.blockHitDelay = 0;
                if (mc.player.isOnGround()) {
                    mc.playerController.curBlockDamageMP = 1.0F;
                } else {
                }
                mc.player.removePotionEffect(Effects.HASTE);
            }
        } else {
            mc.player.addPotionEffect(new EffectInstance(Effects.HASTE, Integer.MAX_VALUE, speed.getValue().intValue() - 1, false, false));
        }

        return false;
    }
    @Override
    protected void onDisable() {
        mc.player.removePotionEffect(Effects.HASTE);
        super.onDisable();
    }
}
