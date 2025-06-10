package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import eva.ware.events.EventPreRender3D;
import eva.ware.events.EventRender2D;
import eva.ware.manager.Theme;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.render.engine2d.ProjectionUtility;
import eva.ware.utils.render.other.ESPShader;
import eva.ware.utils.render.other.OutlineShader;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.other.CustomFramebuffer;
import eva.ware.utils.render.shader.ShaderUtility;
import net.minecraft.client.MainWindow;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector2d;

@ModuleRegister(name = "ShaderEsp", category = Category.Visual)
public class ShaderEsp extends Module {
    private final ShaderUtility gradient = new ShaderUtility("gradient");
    private final CustomFramebuffer buffer = new CustomFramebuffer(true);
    private final ESPShader bloom = new ESPShader();

    private final CheckBoxSetting glowEnabled = new CheckBoxSetting("Глоу", true);
    private final SliderSetting glow = new SliderSetting("Сила", 6, 1, 10, 1).visibleIf(() -> glowEnabled.getValue());
    private final SliderSetting outline = new SliderSetting("Обводка", 1, 1, 3, 1);

    private static final float MAX_DISTANCE = 128;

    public ShaderEsp() {
        addSettings(glowEnabled, glow, outline);
    }

    public Vector3d[] getCorners(AxisAlignedBB AABB) {
        return new Vector3d[]{
                new Vector3d(AABB.minX, AABB.minY, AABB.minZ),
                new Vector3d(AABB.minX, AABB.minY, AABB.maxZ),
                new Vector3d(AABB.minX, AABB.maxY, AABB.minZ),
                new Vector3d(AABB.minX, AABB.maxY, AABB.maxZ),
                new Vector3d(AABB.maxX, AABB.minY, AABB.minZ),
                new Vector3d(AABB.maxX, AABB.minY, AABB.maxZ),
                new Vector3d(AABB.maxX, AABB.maxY, AABB.minZ),
                new Vector3d(AABB.maxX, AABB.maxY, AABB.maxZ)
        };
    }

    public void patch(PlayerEntity entity, Runnable runnable) {
        float[] color = ColorUtility.getRGBAf(Theme.rectColor);
        Vector3d interpolated = entity.getPositionVec().subtract(entity.getPositionVec(mc.getRenderPartialTicks()));
        AxisAlignedBB AABB = entity.getBoundingBox().offset(interpolated.inverse().add(interpolated.scale(mc.getRenderPartialTicks())));
        Vector2d center = ProjectionUtility.project2D(AABB.getCenter());

        if (center == null) return;

        double minX = center.x, minY = center.y, maxX = center.x, maxY = center.y;

        for (Vector3d corner : getCorners(AABB)) {
            Vector2d vec = ProjectionUtility.project2D(corner);
            if (vec != null) {
                minX = Math.min(minX, vec.x);
                minY = Math.min(minY, vec.y);
                maxX = Math.max(maxX, vec.x);
                maxY = Math.max(maxY, vec.y);
            }
        }

        double posX = minX, posY = minY, width = maxX - minX, height = maxY - minY;
        MainWindow mw = mc.getMainWindow();
        gradient.attach();
        gradient.setUniformf("location", (float) (posX * mw.getScaleFactor()), (float) ((mw.getHeight() - (height * mw.getScaleFactor())) - (posY * mw.getScaleFactor())));
        gradient.setUniformf("rectSize", (float) (width * mw.getScaleFactor()), (float) (height * mw.getScaleFactor()));
        gradient.setUniformi("tex", 0);

        for (int i = 1; i <= 4; i++) {
            gradient.setUniformf("color" + i, color);
        }

        runnable.run();
        gradient.detach();
    }

    @Subscribe
    public void onRender3D(EventPreRender3D e) {
        MatrixStack stack = e.getMatrix();
        buffer.setup(false);

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity instanceof ClientPlayerEntity && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON || mc.player.getDistance(entity) > MAX_DISTANCE)
                continue;

            patch(entity, () -> {
                EntityRendererManager rendererManager = mc.getRenderManager();
                stack.push();
                stack.translate(-rendererManager.renderPosX(), -rendererManager.renderPosY(), -rendererManager.renderPosZ());
                GlStateManager.depthMask(true);
                rendererManager.setRenderShadow(false);
                rendererManager.setRenderName(false);
                IRenderTypeBuffer.Impl irendertypebuffer$impl = mc.getRenderTypeBuffers().getBufferSource();

                Vector3d pos = entity.getPositionVec(e.getPartialTicks());
                EntityRenderer<?> renderer = rendererManager.getRenderer(entity);
                boolean nameVisible = renderer.isRenderName();

                if (nameVisible) {
                    renderer.setRenderName(false);
                }

                rendererManager.renderEntityStaticGlow(entity, pos.getX(), pos.getY(), pos.getZ(), entity.rotationYaw, e.getPartialTicks(), stack, irendertypebuffer$impl, rendererManager.getPackedLight(entity, e.getPartialTicks()));

                if (nameVisible) {
                    renderer.setRenderName(true);
                }

                irendertypebuffer$impl.finish();
                rendererManager.setRenderName(true);
                rendererManager.setRenderShadow(true);
                GlStateManager.depthMask(false);
                GlStateManager.enableDepthTest();
                stack.pop();
            });
        }

        buffer.stop();
    }

    @Subscribe
    public void onRender2D(EventRender2D e) {
        OutlineShader.addTask2D(buffer::draw);
        OutlineShader.draw(outline.getValue().intValue());

        if (glowEnabled.getValue()) {
            bloom.addTask2D(buffer::draw);
            bloom.draw(Math.max(glow.getValue().intValue() / 2, 3), glow.getValue(), ESPShader.RenderType.DISPLAY, 2);
        }

        buffer.framebufferClear(false);
        mc.getFramebuffer().bindFramebuffer(true);
    }
}