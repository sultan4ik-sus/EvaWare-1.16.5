package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eva.ware.events.EventChangeWorld;
import eva.ware.events.EventPreRender3D;
import eva.ware.events.EventMoving;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
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
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.ResourceLocation;
import org.joml.Math;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "Trails", category = Category.Visual)
public class Trails extends Module {
    private final SliderSetting delay = new SliderSetting("Длина", 1500, 500, 3000, 100);
    private final CheckBoxSetting renderInFirstPerson = new CheckBoxSetting("Отображать всегда", false);

    private final List<Particle3D> particles = new ArrayList<>();
    private final ResourceLocation bloom2 = new ResourceLocation("eva/images/glow.png");
    private final ResourceLocation bloom = new ResourceLocation("eva/images/firefly.png");

    public Trails() {
        addSettings(delay, renderInFirstPerson);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        particles.clear();
    }

    @Subscribe
    public void onUpdate(EventMoving e) {
        ClientPlayerEntity player = mc.player;

        particles.removeIf(particle -> particle.getTimerUtil().isReached(delay.getValue().intValue()));
        particles.removeIf(particle -> particle.position.distance(player.getPosX(), player.getPosY(), player.getPosZ()) >= 100);

        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON && !renderInFirstPerson.getValue())
            return;

        particles.add(new Particle3D(new Vector3d(player.getPosX(), player.getPosY() + (player.getHeight() * 0.4F), player.getPosZ()), new Vector3d(0,0,0), particles.size()));
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        particles.clear();
    }

    @Subscribe
    public void onPre(EventPreRender3D event) {
        MatrixStack matrix = event.getMatrix();

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
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        float pos = 0F;
        matrix.push();
        if (!particles.isEmpty()) {
            for (int i = 0; i < mc.getRenderPartialTicks(); i++) {
                particles.forEach(Particle3D::update);
            }
            int index = 0;
            for (final Particle3D particle : particles) {
                if ((int) particle.getAnimation().getValue() != 255 && !particle.getTimerUtil().isReached(250)) {
                    particle.getAnimation().run(255);
                }
                if ((int) particle.getAnimation().getValue() != 0 && particle.getTimerUtil().isReached(delay.getValue().intValue() - 250)) {
                    particle.getAnimation().run(0);
                }
                int color = ColorUtility.reAlphaInt(ColorUtility.getColor(particle.getIndex() * 30), (int) (particle.getAnimation().getValue()));

                Particle3D prevParticle = particles.get((int) Math.clamp(0, particles.size(), index - 1));
                Vector3d prevPosition = prevParticle.getPosition();

                float smooth = 10F;
                prevParticle.position.set(
                        MathUtility.lerp(prevPosition.x, particle.position.x, smooth),
                        MathUtility.lerp(prevPosition.y, particle.position.y, smooth),
                        MathUtility.lerp(prevPosition.z, particle.position.z, smooth)
                );

                final Vector3d vec = particle.getPosition();

                final float x = (float) vec.x;
                final float y = (float) vec.y;
                final float z = (float) vec.z;

                matrix.push();
                RectUtility.setupOrientationMatrix(matrix, x, y, z);
                matrix.rotate(mc.getRenderManager().getCameraOrientation());
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                matrix.translate(0, pos / 2F, 0);
                float bloomSize = (float) Math.clamp(0.1, 1, particle.position.distance(prevPosition) * 4);
                RectUtility.bindTexture(bloom);
                RectUtility.drawRect(matrix, -bloomSize, -bloomSize, bloomSize, bloomSize, color, color, color, color, true, true);
                RectUtility.bindTexture(bloom2);
                RectUtility.drawRect(matrix, -bloomSize, -bloomSize, bloomSize, bloomSize, color, color, color, color, true, true);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                matrix.pop();
                index++;
            }
        }
        matrix.pop();

        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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

    @Getter
    private static class Particle3D {
        private final int index;
        private final TimerUtility timerUtil = new TimerUtility();
        private final CompactAnimation animation = new CompactAnimation(Easing.LINEAR, 250);

        public final Vector3d position;
        private final Vector3d delta;

        public Particle3D(final Vector3d position, final int index) {
            this.position = position;
            this.delta = new Vector3d((Math.random() * 0.5 - 0.25) * 0.01, (Math.random() * 0.25) * 0.01, (Math.random() * 0.5 - 0.25) * 0.01);
            this.index = index;
            this.timerUtil.reset();
        }

        public Particle3D(final Vector3d position, final Vector3d velocity, final int index) {
            this.position = position;
            this.delta = new Vector3d(velocity.x * 0.01, velocity.y * 0.01, velocity.z * 0.01);
            this.index = index;
            this.timerUtil.reset();
        }

        public void update() {
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

            this.updateWithoutPhysics();
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

        public void updateWithoutPhysics() {
            this.position.x += this.delta.x;
            this.position.y += this.delta.y;
            this.position.z += this.delta.z;
            this.delta.x /= 0.999999F;
            this.delta.y = 0;
            this.delta.z /= 0.999999F;
        }
    }
}
