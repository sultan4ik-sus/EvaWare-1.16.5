package newcode.fun.utils.math;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.utils.IMinecraft;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil implements IMinecraft {

    public static BigDecimal round(float f, int times) {
        BigDecimal bd = new BigDecimal(Float.toString(f));
        bd = bd.setScale(times, RoundingMode.HALF_UP);
        return bd;
    }
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min; // Если значение меньше минимального, возвращаем минимальное
        } else if (value > max) {
            return max; // Если значение больше максимального, возвращаем максимальное
        }
        return value; // Если значение в пределах, возвращаем его
    }
    public static double round(double num, double increment) {
        double v = (double) Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Vector3d interpolatePos(float oldx, float oldy, float oldz, float x, float y, float z) {
        double getx = (double)(oldx + (x - oldx) * mc.getRenderPartialTicks()) - mc.getRenderManager().info.getProjectedView().getX();
        double gety = (double)(oldy + (y - oldy) * mc.getRenderPartialTicks()) - mc.getRenderManager().info.getProjectedView().getY();
        double getz = (double)(oldz + (z - oldz) * mc.getRenderPartialTicks()) - mc.getRenderManager().info.getProjectedView().getZ();
        return new Vector3d(getx, gety, getz);
    }

    public static int calc(final int value) {
        MainWindow mainWindow = mc.getMainWindow();
        return (int) (value * mainWindow.getGuiScaleFactor() / 2);
    }
    public static float calc(final float value) {
        MainWindow mainWindow = mc.getMainWindow();
        return (float) (value * mainWindow.getGuiScaleFactor() / 2);
    }
    public static double calc(final double value) {
        MainWindow mainWindow = mc.getMainWindow();
        return value * mainWindow.getGuiScaleFactor() / 2;
    }

    public static void scaleElements(float xCenter, float yCenter, float scale, Runnable runnable) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(xCenter, yCenter, 0);
        RenderSystem.scalef(scale, scale, 1);
        RenderSystem.translatef(-xCenter, -yCenter, 0);
        runnable.run();
        RenderSystem.popMatrix();


    }

    public static void scaleElements(float xCenter, float yCenter, float scaleX, float scaleY, float scaleZ, Runnable runnable) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(xCenter, yCenter, 0);
        RenderSystem.scalef(scaleX, scaleY, scaleZ);
        RenderSystem.translatef(-xCenter, -yCenter, 0);
        runnable.run();
        RenderSystem.popMatrix();
    }

    public static float calculateXPosition(float x, float width) {
        return x - width / 2f;
    }

    public static float calculateYPosition(float y, float height) {
        return y - height / 2;
    }

    public static float calculateDelta(float a, float b) {
        return a - b;
    }

    public static Vector2f rotationToEntity(Entity target) {
        Vector3d vector3d = target.getPositionVec().subtract(Minecraft.getInstance().player.getPositionVec());
        double magnitude = Math.hypot(vector3d.x, vector3d.z);
        return new Vector2f(
                (float) Math.toDegrees(Math.atan2(vector3d.z, vector3d.x)) - 90.0F,
                (float) (-Math.toDegrees(Math.atan2(vector3d.y, magnitude)))
        );
    }

    public static Vector2f rotationToBlock(BlockPos target) {
        return rotationToVec(Vector3d.copyCentered(target));
    }

    public static Vector2f rotationToVec(Vector3d target) {
        Vector3d vector3d = target.subtract(Minecraft.getInstance().player.getPositionVec());
        double magnitude = Math.hypot(vector3d.getX(), vector3d.getZ());
        return new Vector2f(
                (float) Math.toDegrees(Math.atan2(vector3d.getZ(), vector3d.getX())) - 90.0F,
                (float) (-Math.toDegrees(Math.atan2(vector3d.getY(), magnitude)))
        );
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static double interpolate2(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {

        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static float randomizeFloat(float min, float max) {
        return (float) (Math.random() * (double) (max - min)) + min;
    }

    public static Vector3d interpolate2(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(interpolate(end.getX(), start.getX(), (double)multiple), interpolate(end.getY(), start.getY(), (double)multiple), interpolate(end.getZ(), start.getZ(), (double)multiple));
    }

    public static org.joml.Vector2f interpolate2(org.joml.Vector2f end, org.joml.Vector2f start, float multiple) {
        return new org.joml.Vector2f((float)interpolate((double)end.x, (double)start.x, (double)multiple), (float)interpolate((double)end.y, (double)start.y, (double)multiple));
    }
}
