package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.math.MathHelper;

@ModuleRegister(name = "Timer", category = Category.Movement)
public class Timer extends Module {

    public final SliderSetting speed = new SliderSetting("Скорость", 2f, 0.1f, 10f, 0.1f);
    public final CheckBoxSetting smart = new CheckBoxSetting("Умный", true);
    public SliderSetting ticks = new SliderSetting("Тики", 1.0f, 0.15f, 3.0f, 0.1f);
    public CheckBoxSetting moveUp = new CheckBoxSetting("Восставливать", false).visibleIf(() -> smart.getValue());
    public SliderSetting moveUpValue = new SliderSetting("Значение", 0.05f, 0.01f, 0.1f, 0.01f).visibleIf(() -> moveUp.getValue() && smart.getValue());
    public double value;

    public float maxViolation = 100.0F;
    public float violation = 0.0F;

    public Timer() {
        addSettings(speed, ticks, smart, moveUp, moveUpValue);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SEntityVelocityPacket p) {
            if (p.getEntityID() == mc.player.getEntityId()) {
                resetSpeed();
            }
        }

        if (e.getPacket() instanceof CConfirmTransactionPacket p) {
            e.cancel();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!mc.player.isOnGround()) {
            this.violation += 0.1f;
            this.violation = MathHelper.clamp(this.violation, 0.0F, this.maxViolation / (this.speed.getValue()));
        }

        mc.timer.timerSpeed = this.speed.getValue();
        if (!this.smart.getValue() || mc.timer.timerSpeed <= 1.0F) {
            return;
        }
        if (this.violation < (this.maxViolation) / (this.speed.getValue())) {
            this.violation += this.ticks.getValue();
            this.violation = MathHelper.clamp(this.violation, 0.0F, this.maxViolation / (this.speed.getValue()));
        } else {
            this.resetSpeed();
        }
    }

    private void reset() {
        mc.timer.timerSpeed = 1;
    }

    public void resetSpeed() {
        this.setEnabled(false, false);
        mc.timer.timerSpeed = 1.0F;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}
