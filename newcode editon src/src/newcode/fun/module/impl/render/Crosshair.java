package newcode.fun.module.impl.render;

import net.minecraft.client.MainWindow;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.animation.AnimationMath;
import org.joml.Vector4i;

@Annotation(name = "Crosshair", type = TypeList.Render)
public class Crosshair extends Module {

    private float currentCircleAnimation = 0.0F;

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender) {
            renderCrosshair();
        }
        return false;
    }

    private void renderCrosshair() {
        if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
            return;
        }

        if (mc.objectMouseOver == null) {
            return;
        }

        int color = new java.awt.Color(197, 49, 70, 255).getRGB();
        MainWindow window = mc.getMainWindow();
        float centerX = (float) window.scaledWidth() / 2.0F;
        float centerY = (float) window.scaledHeight() / 2.0F;

        float attackStrength = mc.player.getCooledAttackStrength(1.0F);
        float targetRadius = MathHelper.clamp(attackStrength * 360, 0, 360);
        currentCircleAnimation = AnimationMath.lerp(currentCircleAnimation, -targetRadius, 4);

        Vector4i outlineColor;
        if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            outlineColor = new Vector4i(ColorUtils.setAlpha(color, 140), ColorUtils.setAlpha(color, 140), ColorUtils.setAlpha(color, 140), ColorUtils.setAlpha(color, 140));
        } else {
            outlineColor = new Vector4i(ColorUtils.setAlpha(-1, 140), ColorUtils.setAlpha(-1, 140), ColorUtils.setAlpha(-1, 140), ColorUtils.setAlpha(-1, 140));
        }

        RenderUtils.Render2D.drawRoundOutline(centerX - 3.5f, centerY - 3.5f, 7, 7, 3, 0f, ColorUtils.rgba(0, 0, 0, 0), outlineColor);
    }
}
