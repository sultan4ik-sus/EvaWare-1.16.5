package newcode.fun.module.impl.movement;

import net.minecraft.util.math.MathHelper;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.misc.TimerUtil;

import static newcode.fun.utils.render.ColorUtils.rgba;

@Annotation(name = "Timer", type = TypeList.Movement)
public class Timer extends Module {


    public SliderSetting timerAmount = new SliderSetting("Скорость", 2, 1, 10, 0.01f);

    public BooleanOption smart = new BooleanOption("Умный", "Smart", true);
    public SliderSetting upValue = new SliderSetting("Значение", 0.02f, 0.01f, 0.5f, 0.01f);

    public SliderSetting ticks = new SliderSetting("Скорость убывания", 1.0f, 0.15f, 3.0f, 0.1f);

    public float maxViolation = 100.0F;
    private float violation = 0.0F;
    private double prevPosX, prevPosY, prevPosZ;
    private float yaw;
    private float pitch;
    public float animWidth;

    private boolean isBoost;

    private TimerUtil timerUtil = new TimerUtil();

    public Timer() {
        addSettings(timerAmount, smart, upValue, ticks);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (event instanceof EventUpdate) {
            handleEventUpdate();
        }
        return false;
    }


    private void handleEventUpdate() {
        if (timerUtil.hasTimeElapsed(25000)) {
            reset();
            timerUtil.reset();
        }
        if (!mc.player.isOnGround() && !isBoost) {
            this.violation += 0.1f;
            this.violation = MathHelper.clamp(this.violation, 0.0F, this.maxViolation / (this.timerAmount.getValue().floatValue()));
        }

        mc.timer.timerSpeed = this.timerAmount.getValue().floatValue();

        // Проверяем условия для умного режима или если скорость таймера <= 1.0F
        if (!this.smart.get() || mc.timer.timerSpeed <= 1.0F) {
            return;
        }
        // Прибавляем значение ticks к переменной speed
        if (this.violation < (this.maxViolation) / (this.timerAmount.getValue().floatValue())) {
            this.violation += this.ticks.getValue().floatValue();
            this.violation = MathHelper.clamp(this.violation, 0.0F, this.maxViolation / (this.timerAmount.getValue().floatValue()));
        } else {
            // Сбрасываем speed, если превышено максимальное значение
            this.resetSpeed();
        }
    }

    /**
     * Обновляет таймер и устанавливает новые значения положения и поворота игрока.
     *
     * @param yaw   значение поворота по горизонтали
     * @param pitch значение поворота по вертикали
     * @param posX  координата X положения игрока
     * @param posY  координата Y положения игрока
     * @param posZ  координата Z положения игрока
     */
    public void updateTimer(float yaw, float pitch, double posX, double posY, double posZ) {
        // Проверяем, находится ли игрок в том же местоположении
        if (this.notMoving()) {
            // Уменьшаем speed на основе ticks и добавляем 0.4

            this.violation = (float) ((double) this.violation - ((double) this.ticks.getValue().floatValue() + 0.4));

            this.violation -= this.upValue.getValue().floatValue();
        }

        // Ограничиваем speed в заданных пределах
        this.violation = (float) MathHelper.clamp(this.violation, 0.0, Math.floor(this.maxViolation));

        // Устанавливаем новые значения положения и поворота
        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Проверяет, находится ли игрок в движении.
     */
    private boolean notMoving() {
        // Проверяем, совпадают ли текущие координаты и поворот с игроком
        return this.prevPosX == mc.player.getPosX()
                && this.prevPosY == mc.player.getPosY()
                && this.prevPosZ == mc.player.getPosZ()
                && this.yaw == mc.player.rotationYaw
                && this.pitch == mc.player.rotationPitch;
    }


    /**
     * Возвращает текущее значение violation.
     */
    public float getViolation() {
        return this.violation;
    }

    /**
     * Сбрасывает скорость и состояние.
     */
    public void resetSpeed() {
        // Сбрасываем скорость и состояние
        this.setState(false);
        mc.timer.timerSpeed = 1.0F;
    }

    public void reset() {
    }

    @Override
    public void onDisable() {
        reset();
        // Сбрасываем скорость таймера при отключении
        mc.timer.timerSpeed = 1;
        timerUtil.reset();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        reset();
        // Устанавливаем скорость таймера в 1.0F при включении
        mc.timer.timerSpeed = 1.0F;
        super.onEnable();
    }
}