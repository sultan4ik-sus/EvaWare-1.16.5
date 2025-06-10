package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventRender2D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.manager.Theme;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import lombok.Getter;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.RayTraceResult.Type;

import java.awt.*;

@ModuleRegister(name = "Crosshair", category = Category.Visual)
public class Crosshair extends Module {

    public final ModeSetting mode = new ModeSetting("Вид", "Орбиз", "Орбиз", "Класический", "Круг");

    public final CheckBoxSetting staticCrosshair = new CheckBoxSetting("Статический", false).visibleIf(() -> mode.is("Орбиз"));
    private float lastYaw;
    private float lastPitch;

    @Getter
    public float animatedYaw, x;
    @Getter
    public float animatedPitch, y;

    private float animation;
    private float animationSize;

    private final int outlineColor = Color.BLACK.getRGB();
    private final int entityColor = Color.RED.getRGB();

    public Crosshair() {
        addSettings(mode, staticCrosshair);
    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.player == null || mc.world == null || e.getType() != EventRender2D.Type.POST) {
            return;
        }

        x = mc.getMainWindow().getScaledWidth() / 2f;
        y = mc.getMainWindow().getScaledHeight() / 2f;

        switch (mode.getIndex()) {
            case 0 -> {
                float size = 5;

                animatedYaw = MathUtility.fast(animatedYaw, ((lastYaw - mc.player.rotationYaw) + mc.player.moveStrafing) * size, 5);
                animatedPitch = MathUtility.fast(animatedPitch, ((lastPitch - mc.player.rotationPitch) + mc.player.moveForward) * size, 5);
                animation = MathUtility.fast(animation, mc.objectMouseOver.getType() == Type.ENTITY ? 1 : 0, 5);

                int color = ColorUtility.interpolate(Theme.rectColor, Theme.mainRectColor, 1 - animation);

                if (!staticCrosshair.getValue()) {
                    x += animatedYaw;
                    y += animatedPitch;
                }

                animationSize = MathUtility.fast(animationSize, (1 - mc.player.getCooledAttackStrength(1)) * 3, 10);

                float radius = 3 + (staticCrosshair.getValue() ? 0 : animationSize);
                if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    RenderUtility.drawShadowCircle(x, y, radius * 2, ColorUtility.setAlpha(color, 64));
                    RenderUtility.drawCircle(x, y, radius, color);
                }
                lastYaw = mc.player.rotationYaw;
                lastPitch = mc.player.rotationPitch;
            }

            case 1 -> {
                if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) return;

                float cooldown = 1 - mc.player.getCooledAttackStrength(0);
                animationSize = MathUtility.fast(animationSize, (1 - mc.player.getCooledAttackStrength(0)), 10);
                float thickness = 1;
                float length = 3;
                float gap = 2 + 8 * animationSize;

                int color = mc.pointedEntity != null ? entityColor : -1;

                drawOutlined(x - thickness / 2, y - gap - length, thickness, length, color);
                drawOutlined(x - thickness / 2, y + gap, thickness, length, color);

                drawOutlined(x - gap - length, y - thickness / 2, length, thickness, color);
                drawOutlined(x + gap, y - thickness / 2, length, thickness, color);
            }

            case 2 -> {
                animationSize = MathUtility.fast(animationSize, (1 - mc.player.getCooledAttackStrength(1)) * 260, 10);
                if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    RenderUtility.drawCircleWithFill(x, y, 0, 360, 3.8f, 3, false, ColorUtility.rgb(23,21,21));
                    RenderUtility.drawCircleWithFill(x, y, animationSize, 360, 3.8f, 3, false, Hud.getColor(Theme.rectColor, Theme.mainRectColor, 16, 0));
                }
            }
        }
    }

    private void drawOutlined(
            final float x,
            final float y,
            final float w,
            final float h,
            final int hex
    ) {
        RenderUtility.drawRectW(x - 0.5, y - 0.5, w + 1, h + 1, outlineColor); // бля че за хуйня поч его хуярит салат что наделал
        RenderUtility.drawRectW(x, y, w, h, hex);
    }
}
