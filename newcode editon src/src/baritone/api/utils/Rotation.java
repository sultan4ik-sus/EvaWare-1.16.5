package baritone.api.utils;

import net.minecraft.util.math.MathHelper;
import newcode.fun.utils.math.GCDUtil;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Brady
 * @since 9/25/2018
 */
public class Rotation {

    private float yaw;
    private float pitch;

    private float bodyYaw;  // для тела

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.bodyYaw = yaw;  // Изначально тело поворачивается с той же скоростью что и голова
        if (Float.isInfinite(yaw) || Float.isNaN(yaw) || Float.isInfinite(pitch) || Float.isNaN(pitch)) {
            throw new IllegalStateException(yaw + " " + pitch);
        }
    }

    public float getYaw() {
        return GCDUtil.getSensitivity(this.yaw);
    }

    public float getPitch() {
        return GCDUtil.getSensitivity(MathHelper.clamp(this.pitch, -90, 90));
    }

    public float getBodyYaw() {
        return GCDUtil.getSensitivity(this.bodyYaw);
    }

    // Устанавливаем плавное изменение между углами для головы и тела
    public Rotation smoothTurn(Rotation targetRotation, float headSpeed, float bodySpeed) {
        // Плавно вращаем голову
        float newYaw = smoothTransition(this.yaw, targetRotation.yaw, headSpeed);
        float newPitch = smoothTransition(this.pitch, targetRotation.pitch, headSpeed);

        // Плавно вращаем тело
        float newBodyYaw = smoothTransition(this.bodyYaw, targetRotation.yaw, bodySpeed);

        return new Rotation(newYaw, newPitch);
    }

    // Плавная функция перехода
    private float smoothTransition(float current, float target, float speed) {
        float difference = MathHelper.wrapDegrees(target - current);
        return current + MathHelper.clamp(difference, -speed, speed);
    }

    public Rotation add(Rotation other) {
        return new Rotation(this.yaw + other.yaw, this.pitch + other.pitch);
    }

    public Rotation subtract(Rotation other) {
        return new Rotation(this.yaw - other.yaw, this.pitch - other.pitch);
    }

    public Rotation clamp() {
        return new Rotation(this.yaw, clampPitch(this.pitch));
    }

    public Rotation normalize() {
        return new Rotation(normalizeYaw(this.yaw), this.pitch);
    }

    public Rotation normalizeAndClamp() {
        return new Rotation(normalizeYaw(this.yaw), clampPitch(this.pitch));
    }

    public boolean isReallyCloseTo(Rotation other) {
        return yawIsReallyClose(other) && Math.abs(this.pitch - other.pitch) < 0.01;
    }

    public boolean yawIsReallyClose(Rotation other) {
        float yawDiff = Math.abs(normalizeYaw(this.yaw) - normalizeYaw(other.yaw));
        return (yawDiff < 0.01 || yawDiff > 359.99);
    }

    public static float clampPitch(float pitch) {
        return Math.max(-90, Math.min(90, pitch));
    }

    public static float normalizeYaw(float yaw) {
        float newYaw = yaw % 360F;
        if (newYaw < -180F) {
            newYaw += 360F;
        }
        if (newYaw > 180F) {
            newYaw -= 360F;
        }
        return newYaw;
    }

    @Override
    public String toString() {
        return "Yaw: " + yaw + ", Pitch: " + pitch + ", Body Yaw: " + bodyYaw;
    }
}
