package newcode.fun.module.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventJump;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.animation.AnimationMath;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;
import static org.lwjgl.opengl.GL11.*;

@Annotation(name = "JumpCircle", type = TypeList.Render)
public class JumpCircle extends Module {
    public List<Circle> circles = new ArrayList<>();
    public final ModeSetting element = new ModeSetting("Режим", "Обычный", "Обычный", "Текст", "Тонкий");
    public SliderSetting radius = new SliderSetting("Радиус", 1, 0.5f, 3, 0.1f);
    public SliderSetting speed = new SliderSetting("Скорость", 1, 1, 5, 0.1f);
    public SliderSetting fadeSpeed = new SliderSetting("Скорость исчезновения", 1, 1f, 5, 0.5f);

    public JumpCircle() {
        addSettings(element, radius, speed, fadeSpeed);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventJump) {
            addCircle();
        } else if (event instanceof EventRender render && render.isRender3D()) {
            updateCircles();
            renderCircles();
        }
        return false;
    }

    private void addCircle() {
        circles.add(new Circle((float) mc.player.getPosX(), (float) mc.player.getPosY(), (float) mc.player.getPosZ()));
    }

    private void updateCircles() {
        for (Circle circle : circles) {
            circle.factor = AnimationMath.fast(circle.factor, radius.getValue().floatValue(), speed.getValue().floatValue());
            circle.alpha = AnimationMath.fast(circle.alpha, 0, fadeSpeed.getValue().floatValue()); // Используем fadeSpeed
        }
        if (circles.size() >= 1)
            circles.removeIf(circle -> circle.alpha <= 0.005f);
    }


    private void renderCircles() {
        setupRenderSettings();
        for (Circle circle : circles) {
            drawJumpCircle(circle, circle.factor, circle.alpha, 0);
        }
        restoreRenderSettings();
    }

    private void setupRenderSettings() {
        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 1, 0, 1);
        GlStateManager.translated(-mc.getRenderManager().info.getProjectedView().getX(),
                -mc.getRenderManager().info.getProjectedView().getY(),
                -mc.getRenderManager().info.getProjectedView().getZ());
    }

    private void restoreRenderSettings() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();

    }

    private void drawJumpCircle(Circle circle, float radius, float alpha, float shadowSize) {
        double x = circle.spawnX;
        double y = circle.spawnY + 0.1;
        double z = circle.spawnZ;
        GlStateManager.translated(x,y,z);
        GlStateManager.rotatef(circle.factor * 70,0,-1,0);
        switch (element.getIndex()) {
            case 0 -> {
                mc.getTextureManager().bindTexture(new ResourceLocation("newcode/images/all/jump/circlekrug.png"));
            }
            case 1 -> {
                mc.getTextureManager().bindTexture(new ResourceLocation("newcode/images/all/jump/jump_text.png"));
            }
            case 2 -> {
                mc.getTextureManager().bindTexture(new ResourceLocation("newcode/images/all/jump/jump_wex.png"));
            }
        }

        buffer.begin(GL_QUAD_STRIP, POSITION_COLOR_TEX);
        for (int i = 0; i <= 360F; i+=1) {
            float[] colors = RenderUtils.IntColor.rgb(ColorUtils.getColorStyle(i * 2));
            double sin = MathHelper.sin(Math.toRadians(i + 0.1F)) * radius;
            double cos = MathHelper.cos(Math.toRadians(i + 0.1F)) * radius;
            buffer.pos(0, 0, 0).color(colors[0], colors[1], colors[2], MathHelper.clamp(circle.alpha ,0,1)).tex(0.5f, 0.5f).endVertex();
            buffer.pos(sin, 0, cos).color(colors[0], colors[1], colors[2], MathHelper.clamp(circle.alpha ,0,1)).tex((float) ((sin / (2 * radius)) + 0.5f), (float) ((cos / (2 * radius)) + 0.5f)).endVertex();
        }
        tessellator.draw();
        GlStateManager.rotatef(-circle.factor * 70,0,-1,0);
        GlStateManager.translated(-x,-y,-z);
    }

    class Circle {
        public final float spawnX;
        public final float spawnY;
        public final float spawnZ;
        public float factor = 0;
        public float alpha = 5;
        public float shadow = 40;
        public float ticks = 0;

        public Circle(float spawnX, float spawnY, float spawnZ) {
            this.spawnX = spawnX;
            this.spawnY = spawnY;
            this.spawnZ = spawnZ;
        }
    }
}
