package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eva.ware.events.AttackEvent;
import eva.ware.events.EventChangeWorld;
import eva.ware.events.EventPreRender3D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RectUtility;
import eva.ware.utils.player.rotation.RotationUtility;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ModuleRegister(name = "HitBubbles", category = Category.Visual)
public class HitBubbles extends Module {
    private final List<Particle> particles = new ArrayList<>();
    private final ResourceLocation texture = new ResourceLocation("eva/images/modules/bubble.png");
    long maxTime = 0;
    @Override
    public void onEnable() {
        super.onEnable();
        particles.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        particles.clear();
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        particles.clear();
    }

    @Subscribe
    public void onAttack(AttackEvent event) {
        if (event.entity == mc.player) return;
        if (event.entity instanceof LivingEntity target) {
            ClientPlayerEntity player = mc.player;
            double distance = player.getDistance(target) - 1F + player.getWidth() / 2.F, yawRad = Math.toRadians(RotationUtility.calculate(target).x), xOffset = -Math.sin(yawRad) * distance, zOffset = Math.cos(yawRad) * distance;
            Vector3d particlePosition = new Vector3d(player.getPosX() + xOffset, target.getPosY() + target.getEyeHeight(), player.getPosZ() + zOffset);
            for (Particle p : particles) {
                maxTime = p.time;
            }
            particles.add(new Particle(particles.size(), particlePosition, maxTime));
        }
    }

    @Subscribe
    public void onRender(EventPreRender3D event) {
        if (particles.isEmpty()) return;
        boolean bloom = true;
        MatrixStack matrixStack = event.getMatrix();

        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();

            if (System.currentTimeMillis() - p.time > 1500) {
                iterator.remove();
                continue;
            }

            if (System.currentTimeMillis() - p.time > 1000 && !p.isBack) {
                p.animation.animate(0, 0.5, Easings.EXPO_IN);
                p.isBack = true;
            }

            p.animation.update();
            float maxScale = (float) (1 * p.animation.getValue());
            float alphaPC = (float) (1 * p.animation.getValue());

            if (p.renderRule()) {
                setRenderingTexture3D(() -> drawParticle(p, matrixStack, maxScale, alphaPC, bloom), matrixStack, texture);
            }
        }
    }

    private void drawParticle(Particle particle, MatrixStack matrix, float maxScale, float alphaPC, boolean bloom) {
        int[] colors = getColorsParticles(particle, alphaPC);
        float extendXY = maxScale;
        setParticleOrientation(particle, () -> {
            for (int i = 0; i < 2; ++i)
                RectUtility.drawRect(matrix, -extendXY, -extendXY, extendXY, extendXY, colors[0], colors[1], colors[2], colors[3], bloom, true);
        }, matrix);
    }

    private void setRenderingTexture3D(Runnable render, MatrixStack matrix, ResourceLocation resourceLocation) {
        boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);
        RenderSystem.pushMatrix();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        if (light)
            RenderSystem.disableLighting();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        RectUtility.bindTexture(resourceLocation);
        matrix.push();
        render.run();

        matrix.pop();
        GlStateManager.clearCurrentColor();
        GL11.glShadeModel(GL11.GL_FLAT);
        if (light)
            RenderSystem.enableLighting();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
        matrix.pop();
        RenderSystem.popMatrix();
    }

    private int[] getColorsParticles(Particle particle, float alphaPC) {
        int[] colors = new int[4];
        int indexAppend = particle.index * 45;
        colors[0] = ColorUtility.multAlpha(ColorUtility.getColor(indexAppend), alphaPC);
        colors[1] = ColorUtility.multAlpha(ColorUtility.getColor(90 + indexAppend), alphaPC);
        colors[2] = ColorUtility.multAlpha(ColorUtility.getColor(180 + indexAppend), alphaPC);
        colors[3] = ColorUtility.multAlpha(ColorUtility.getColor(270 + indexAppend), alphaPC);
        return colors;
    }

    private void setParticleOrientation(Particle particle, Runnable render, MatrixStack matrix) {
        matrix.push();
        RectUtility.setupOrientationMatrix(matrix, particle.pos.getX(), particle.pos.getY(), particle.pos.getZ());
        matrix.rotate(particle.quaternion);
        RectUtility.bindTexture(texture);
        matrix.push();
        final float rotatePC01 = (float) (1 * particle.animation.getValue());
        matrix.rotate(Vector3f.ZP.rotationDegrees(rotatePC01 * -360.F));
        render.run();
        matrix.pop();
        matrix.pop();
    }

    private static class Particle {
        private final Animation animation = new Animation();
        private final long time;
        private final int index;
        private final Vector3d pos;
        private final Quaternion quaternion = mc.getRenderManager().getCameraOrientation().copy();
        private boolean isBack;

        public Particle(int index, Vector3d pos, long maxTime) {
            this.index = index;
            this.pos = pos;
            time = System.currentTimeMillis();
            animation.animate(1, 1, Easings.EXPO_OUT);
        }

        public boolean isToRemove() {
            return mc.player.getPositionVec().distanceTo(pos) > 100;
        }

        public boolean renderRule() {
            return !isToRemove();
        }
    }
}
