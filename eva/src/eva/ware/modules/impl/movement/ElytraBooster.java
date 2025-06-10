package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.math.TimerUtility;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

@ModuleRegister(name = "ElytraBooster", category = Category.Movement)
public class ElytraBooster extends Module {
    public final ModeSetting mode = new ModeSetting("Мод", "Standart", "Standart", "BravoHVH");
    public final SliderSetting boostPower = new SliderSetting("Скорость буста", 1.63f, 1.55f, 1.8f, 0.01f).visibleIf(() -> mode.is("Standart"));
    public double boosterSpeed;
    public boolean restart;
    public TimerUtility timerUtility = new TimerUtility();

    public ElytraBooster() {
        addSettings(mode, boostPower);
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof SPlayerPositionLookPacket packet && mode.is("BravoHVH")) {
            restart = false;
            boosterSpeed = 0.7;
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mode.is("BravoHVH")) {
            if (!restart) {
                boosterSpeed = 1.5;
                if (timerUtility.isReached(1000)) {
                    restart = true;
                    timerUtility.reset();
                }
            }

            if (restart) boosterSpeed = Math.min(boosterSpeed + 0.05, 1.66800064); // 1.66800064
        } else {
            boosterSpeed = boostPower.getValue();
        }
    }
}
