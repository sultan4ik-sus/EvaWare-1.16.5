package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eva.ware.Evaware;
import eva.ware.events.EventChangeWorld;
import eva.ware.events.EventPreRender3D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.BlockUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RectUtility;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.joml.Math;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "FireFly", category = Category.Visual)
public class FireFly extends Module {
    private final List<FireFlyEntity> particles = new ArrayList<>();
    private final ModeSetting fallModeSetting = new ModeSetting("Режим", "Простой", "Простой", "Взлет");
    private final SliderSetting count = new SliderSetting("Количество", 100, 10, 1000, 10);
    public final CheckBoxSetting randomColor = new CheckBoxSetting("Рандомный цвет", false);
    private final ResourceLocation texture = new ResourceLocation("eva/images/firefly.png");

    public FireFly() {
        addSettings(fallModeSetting, count, randomColor);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        particles.clear();
        spawnParticle(mc.player);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        particles.clear();
    }

    private void spawnParticle(LivingEntity entity) {
        double distance = MathUtility.random(5, 50), yawRad = Math.toRadians(MathUtility.random(0, 360)), xOffset = -Math.sin(yawRad) * distance, zOffset = Math.cos(yawRad) * distance;

        particles.add(new FireFlyEntity(new Vector3d(entity.getPosX() + xOffset, entity.getPosY() + (fallModeSetting.is("Взлет") ? MathUtility.random(-5, 0) : MathUtility.random(3, 15)), entity.getPosZ() + zOffset),
                new Vector3d(), particles.size(), ColorUtility.random().hashCode()));
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        particles.clear();
    }

    @Subscribe
    public void onPreRender(EventPreRender3D event) {
        ClientPlayerEntity player = mc.player;

        // Remove expired or distant particles
        particles.removeIf(particle ->
                particle.time.isReached(5000) ||
                        particle.position.distance(player.getPosX(), player.getPosY(), player.getPosZ()) >= 60
        );

        // Spawn new particles if needed
        if (particles.size() <= count.getValue().intValue()) {
            spawnParticle(player);
        }

        MatrixStack matrix = event.getMatrix();
        boolean lightEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);

        RenderSystem.pushMatrix();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        if (lightEnabled) {
            RenderSystem.disableLighting();
        }

        GL11.glShadeModel(GL11.GL_SMOOTH);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        RectUtility.bindTexture(texture);

        if (!particles.isEmpty()) {
            particles.forEach(fireFlyEntity -> fireFlyEntity.update(true));

            float pos = 0.2F;
            for (FireFlyEntity particle : particles) {
                updateParticleAlpha(particle);

                int alpha = (int) Math.clamp(0, (int) (particle.getAlpha().getValue()), particle.getAngle());
                int colorGlow = randomColor.getValue() ?
                        ColorUtility.reAlphaInt(particle.getColor(), alpha) :
                        ColorUtility.reAlphaInt(ColorUtility.getColor(particle.index * 99), alpha);

                renderParticle(matrix, particle, pos, alpha, colorGlow);
            }
        }

        cleanupRenderState(lightEnabled, matrix);
    }

    private void updateParticleAlpha(FireFlyEntity particle) {
        if ((int) particle.getAlpha().getValue() != 255 && !particle.time.isReached(particle.alpha.getDuration())) {
            particle.getAlpha().run(255);
        }
        if ((int) particle.getAlpha().getValue() != 0 && particle.time.isReached(5000 - particle.alpha.getDuration())) {
            particle.getAlpha().run(0);
        }
    }

    private void renderParticle(MatrixStack matrix, FireFlyEntity particle, float pos, int alpha, int colorGlow) {
        final Vector3d vec = particle.getPosition();
        matrix.push();
        RectUtility.setupOrientationMatrix(matrix, (float) vec.x, (float) vec.y, (float) vec.z);
        matrix.rotate(mc.getRenderManager().getCameraOrientation());
        matrix.translate(0, pos / 2F, 0);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RectUtility.drawRect(matrix, -pos, -pos, pos, pos, colorGlow, colorGlow, colorGlow, colorGlow, true, true);

        float size = pos / 2F;
        int color = ColorUtility.reAlphaInt(-1, alpha);
        RectUtility.drawRect(matrix, -size, -size, size, size, color, color, color, color, true, true);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        matrix.pop();
    }

    private void cleanupRenderState(boolean lightEnabled, MatrixStack matrix) {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        GlStateManager.clearCurrentColor();
        GL11.glShadeModel(GL11.GL_FLAT);

        if (lightEnabled) {
            RenderSystem.enableLighting();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
        matrix.pop();
        RenderSystem.popMatrix();
    }


    @Getter
    private static class FireFlyEntity {
        private final int index;
        private final TimerUtility time = new TimerUtility();
        private final CompactAnimation alpha = new CompactAnimation(Easing.LINEAR, 150);
        private final int color;

        public final Vector3d position;
        private final Vector3d delta;

        public FireFlyEntity(final Vector3d position, final Vector3d velocity, final int index, int color) {
            this.position = position;
            this.delta = new Vector3d(velocity.x, velocity.y, velocity.z);
            this.index = index;
            this.color = color;
            this.time.reset();
        }

        public void update(boolean physics) {
            if (physics) {
                final Block block1 = BlockUtility.getBlock(this.position.x, this.position.y, this.position.z + this.delta.z);
                if (isValidBlock(block1))
                    this.delta.z *= -0.8;

                final Block block2 = BlockUtility.getBlock(this.position.x, this.position.y + this.delta.y, this.position.z);
                if (isValidBlock(block2)) {
                    this.delta.x *= 0.999F;
                    this.delta.z *= 0.999F;
                    this.delta.y *= -0.7;
                }

                final Block block3 = BlockUtility.getBlock(this.position.x + this.delta.x, this.position.y, this.position.z);
                if (isValidBlock(block3))
                    this.delta.x *= -0.8;
            }

            this.updateMotion();
        }

        private boolean isValidBlock(Block block) {
            return !(block instanceof AirBlock)
                    && !(block instanceof BushBlock)
                    && !(block instanceof AbstractButtonBlock)
                    && !(block instanceof TorchBlock)
                    && !(block instanceof LeverBlock)
                    && !(block instanceof AbstractPressurePlateBlock)
                    && !(block instanceof CarpetBlock)
                    && !(block instanceof FlowingFluidBlock);
        }

        public int getAngle() {
            return (int) Math.clamp(0, 255, ((Math.sin(time.getTime() / 250D) + 1F) / 2F) * 255);
        }

        public void updateMotion() {
            double motion = 0.005;
            this.delta.x += (Math.random() - 0.5) * motion;
            this.delta.y += (Math.random() - 0.5) * motion;

            if (!Evaware.getInst().getModuleManager().getFireFly().fallModeSetting.is("Простой")) {
                this.delta.y = 0.2;
            }

            this.delta.z += (Math.random() - 0.5) * motion;

            double maxSpeed = 0.5;
            this.delta.x = MathHelper.clamp(this.delta.x, -maxSpeed, maxSpeed);
            this.delta.y = MathHelper.clamp(this.delta.y, -maxSpeed, maxSpeed);
            this.delta.z = MathHelper.clamp(this.delta.z, -maxSpeed, maxSpeed);

            this.position.x += this.delta.x;
            this.position.y += this.delta.y;
            this.position.z += this.delta.z;

        }
    }
}
