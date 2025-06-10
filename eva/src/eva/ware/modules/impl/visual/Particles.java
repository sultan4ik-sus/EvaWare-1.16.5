package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eva.ware.events.*;
import eva.ware.manager.Theme;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "Particles", category = Category.Visual)
public class Particles extends Module {
    private final ModeSetting textureMode = new ModeSetting("Текстура", "Bloom", "Bloom", "Random");

    private final ModeListSetting elements = new ModeListSetting("Триггер",
                    new CheckBoxSetting("Удар", true),
                    new CheckBoxSetting("Ходьба", true),
                    new CheckBoxSetting("Бросаемый предмет", true),
                    new CheckBoxSetting("Прыжок", true)
            );
    public final SliderSetting sizeOfParticles = new SliderSetting("Количество при атаке", 15, 3, 50, 1).visibleIf(() -> elements.is("Удар").getValue());
    public final SliderSetting sizeOfParticlesWhenWalk = new SliderSetting("Количество при ходьбе", 3, 1, 5, 1).visibleIf(() -> elements.is("Ходьба").getValue());
    public final CheckBoxSetting randomColor = new CheckBoxSetting("Рандомный цвет", false);

    private final List<Particle3D> targetParticles = new ArrayList<>();
    private final List<Particle3D> flameParticles = new ArrayList<>();

    private ResourceLocation texture() {
        int r =  MathUtility.randomInt(1, 12);
        if (textureMode.is("Bloom")) {
            return new ResourceLocation("eva/images/firefly.png");
        } else {
            return new ResourceLocation("eva/images/modules/particle/p" + r + ".png");
        }
    }
    public Particles() {
        addSettings(textureMode, elements, sizeOfParticles, sizeOfParticlesWhenWalk, randomColor);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        targetParticles.clear();
        flameParticles.clear();
    }

    @Subscribe
    public void onAttack(AttackEvent event) {
        Entity target = event.entity;
        float motion = 3;
        if (elements.is("Удар").getValue()) {
            for (int i = 0; i < sizeOfParticles.getValue(); i++) {
                targetParticles.add(new Particle3D(new Vector3d(target.getPosX(), target.getPosY() + MathUtility.random(0, target.getHeight()), target.getPosZ()),
                        new Vector3d(MathUtility.random(-motion, motion), MathUtility.random(-2, 0.1f), MathUtility.random(-motion, motion)), targetParticles.size(), ColorUtility.random().hashCode()));
            }
        }
    }

    @Subscribe
    public void onJump(EventJump e) {
        LivingEntity target = mc.player;
        float motion = 3;
        if (elements.is("Прыжок").getValue()) {
            for (int i = 0; i < sizeOfParticles.getValue(); i++) {
                flameParticles.add(new Particle3D(new Vector3d(target.getPosX(), target.getPosY(), target.getPosZ()), new Vector3d(MathUtility.random(-motion, motion), MathUtility.random(-2, 0.1f), MathUtility.random(-motion, motion)), targetParticles.size(), ColorUtility.random().hashCode()));
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (elements.is("Ходьба").getValue()) {
            if (mc.player.lastTickPosX != mc.player.getPosX() || mc.player.lastTickPosY != mc.player.getPosY() || mc.player.lastTickPosZ != mc.player.getPosZ()) {
                for (int i = 0; i < sizeOfParticlesWhenWalk.getValue(); i++) {
                    flameParticles.add(new Particle3D(new Vector3d(mc.player.getPosX() + MathUtility.random(-0.5f, 0.5f), mc.player.getPosY() + MathUtility.random(0.4f, mc.player.getHeight() / 2), mc.player.getPosZ() + MathUtility.random(-0.5f, 0.5f)), new Vector3d(MathUtility.random(-0.1f, 0.1f), 0, MathUtility.random(-0.1f, 0.1f)).mul(2 * (1 + Math.random())), flameParticles.size(), ColorUtility.random().hashCode()));
                }
            }
        }

        if (elements.is("Бросаемый предмет").getValue()) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof ThrowableEntity p) {
                    for (int i = 0; i < 3; i++) {
                        flameParticles.add(new Particle3D(new Vector3d(p.getPosX() + MathUtility.random(-0.5f, 0.5f), p.getPosY() + MathUtility.random(0, p.getHeight()), p.getPosZ() + MathUtility.random(-0.5f, 0.5f)), new Vector3d(MathUtility.random(-0.5f, 0.5f), MathUtility.random(-0.3f, 0.3f), MathUtility.random(-0.5f, 0.5f)).mul(2 * (1 + Math.random())), flameParticles.size(), ColorUtility.random().hashCode()));
                    }
                }
            }
        }

        targetParticles.removeIf(particle -> particle.getTime().isReached(5000));
        flameParticles.removeIf(particle -> particle.getTime().isReached(3500));
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        targetParticles.clear();
        flameParticles.clear();
    }

    @Subscribe
    public void onRender(EventPreRender3D event) {
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

        float pos = 0.1F;

        matrix.push();
        if (!targetParticles.isEmpty()) {
            targetParticles.forEach(Particle3D::update);
            for (final Particle3D particle : targetParticles) {
                RectUtility.bindTexture(particle.getTexture());
                if ((int) particle.getAnimation().getValue() != 255 && !particle.getTime().isReached(500)) {
                    particle.getAnimation().run(255);
                }
                if ((int) particle.getAnimation().getValue() != 0 && particle.getTime().isReached(2000)) {
                    particle.getAnimation().run(0);
                }
                int color = ColorUtility.setAlpha(Theme.rectColor, (int) (particle.getAnimation().getValue()));
                if (randomColor.getValue())
                    color = ColorUtility.setAlpha(particle.getColor(), (int) (particle.getAnimation().getValue()));
                final Vector3d v = particle.getPosition();

                final float x = (float) v.x;
                final float y = (float) v.y;
                final float z = (float) v.z;

                matrix.push();
                RectUtility.setupOrientationMatrix(matrix, x, y, z);

                matrix.rotate(mc.getRenderManager().getCameraOrientation());
                matrix.rotate(new Quaternion(new Vector3f(0, 0, 1), particle.rotation, false));
                matrix.push();
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                matrix.translate(0, pos / 2F, 0);
                RectUtility.drawRect(matrix, -pos, -pos, pos, pos, color, color, color, color, true, true);
                float size = pos / 2F;
                color = ColorUtility.setAlpha(-1, (int) (particle.getAnimation().getValue()));
                RectUtility.drawRect(matrix, -size, -size, size, size, color, color, color, color, true, true);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                matrix.pop();
                matrix.pop();
            }

        }
        matrix.pop();

        matrix.push();
        if (!flameParticles.isEmpty()) {
            flameParticles.forEach(Particle3D::update);
            for (final Particle3D particle : flameParticles) {
                RectUtility.bindTexture(particle.getTexture());
                if ((int) particle.getAnimation().getValue() != 255 && !particle.getTime().isReached(500)) {
                    particle.getAnimation().run(255);
                }
                if ((int) particle.getAnimation().getValue() != 0 && particle.getTime().isReached(800)) {
                    particle.getAnimation().run(0);
                }
                int color = ColorUtility.setAlpha(Theme.rectColor, (int) (particle.getAnimation().getValue()));
                if (randomColor.getValue())
                    color = ColorUtility.setAlpha(particle.getColor(), (int) (particle.getAnimation().getValue()));

                final Vector3d v = particle.getPosition();

                final float x = (float) v.x;
                final float y = (float) v.y;
                final float z = (float) v.z;

                matrix.push();

                RectUtility.setupOrientationMatrix(matrix, x, y, z);

                matrix.rotate(mc.getRenderManager().getCameraOrientation());
                matrix.rotate(new Quaternion(new Vector3f(0, 0, 1), particle.rotation, false));
                matrix.push();
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                matrix.translate(0, pos / 2F, 0);
                RectUtility.drawRect(matrix, -pos, -pos, pos, pos, color, color, color, color, true, true);
                float size = pos / 2F;
                color = ColorUtility.setAlpha(-1, (int) (particle.getAnimation().getValue()));
                RectUtility.drawRect(matrix, -size, -size, size, size, color, color, color, color, true, true);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                matrix.pop();
                matrix.pop();
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
    public class Particle3D {
        private final int index;
        private final int color;
        private final TimerUtility time = new TimerUtility();
        private final CompactAnimation animation = new CompactAnimation(Easing.LINEAR, 500);
        private ResourceLocation texture;
        public final Vector3d position;
        private final Vector3d delta;

        private float rotate = 0;
        private float rotation;

        public Particle3D(final Vector3d position, final Vector3d velocity, final int index, int color) {
            this.position = position;
            this.delta = new Vector3d(velocity.x * 0.01, velocity.y * 0.01, velocity.z * 0.01);
            this.index = index;
            this.color = color;
            this.texture = texture();
            this.time.reset();
        }

        public void update() {
            rotation = rotate % 1000f / 50f;

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
            rotate += 1;
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
            this.delta.y -= 0.00005F;
            this.delta.z /= 0.999999F;
        }
    }
}
