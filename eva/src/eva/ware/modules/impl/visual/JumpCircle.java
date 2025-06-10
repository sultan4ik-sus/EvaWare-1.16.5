package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import eva.ware.events.EventChangeWorld;
import eva.ware.events.EventPreRender3D;
import eva.ware.events.EventJump;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RectUtility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14C;

import java.util.List;

@ModuleRegister(name = "JumpCircle", category = Category.Visual)
public class JumpCircle extends Module {
    private final ModeSetting texture = new ModeSetting("Моды", "Circle", "Circle", "KonchalEbal", "Glow");
    private final SliderSetting maxRadius = new SliderSetting("Радиус", 4, 1, 6, 0.1f);
    private final String staticLoc = "eva/images/modules/jumpcircles/";

    private ResourceLocation jumpTexture() {
        return new ResourceLocation(staticLoc + texture.getValue().toLowerCase() + ".png");
    }

    public JumpCircle() {
        addSettings(texture, maxRadius);
    }

    private void addCircleForEntity(final Entity entity) {
        Vector3d vec = getVec3dFromEntity(entity).add(0.D, .005D, 0.D);
        BlockPos pos = new BlockPos(vec);
        BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() == Blocks.SNOW) {
            vec = vec.add(0.D, .125D, 0.D);
        }
        circles.add(new JumpRenderer(vec, circles.size()));
    }

    @Subscribe
    public void onJump(EventJump e) {
        addCircleForEntity(mc.player);
    }

    @Subscribe
    public void onRender(EventPreRender3D event) {
        circles.removeIf((final JumpRenderer circle) -> circle.getDeltaTime() >= 5.D);

        if (circles.isEmpty()) return;

        setupDraw(event.getMatrix(), () -> circles.forEach(circle -> doCircle(event.getMatrix(), circle.pos, maxRadius.getValue(), 1.F - circle.getDeltaTime(), circle.getIndex() * 30)));
    }

    private void doCircle(MatrixStack stack, final Vector3d pos, double maxRadius, float deltaTime, int index) {
        float waveDelta = valWave1(1.F - deltaTime);
        float alphaPC = (float) (valWave2(waveDelta));
        float radius = (float) (easeOutCirc(valWave2(1.F - deltaTime)) * (!texture.is("Circle") ? maxRadius : maxRadius * 1.5));

        double rotate = 1;
        ResourceLocation res = jumpTexture();
        mc.getTextureManager().bindTexture(res);
        stack.push();
        stack.translate(pos.x - radius / 2.D, pos.y, pos.z - radius / 2.D);
        stack.rotate(Vector3f.XP.rotationDegrees(90.F));
        customRotatedObject2D(stack, 0, 0, radius, radius, rotate);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(stack.getLast().getMatrix(), 0, 0, 0).tex(0, 0).color(getColor(index, alphaPC)).endVertex();
        buffer.pos(stack.getLast().getMatrix(), 0, radius, 0).tex(0, 1).color(getColor(90 + index, alphaPC)).endVertex();
        buffer.pos(stack.getLast().getMatrix(), radius, radius, 0).tex(1, 1).color(getColor(180 + index, alphaPC)).endVertex();
        buffer.pos(stack.getLast().getMatrix(), radius, 0, 0).tex(1, 0).color(getColor(270 + index, alphaPC)).endVertex();
        tessellator.draw();
        stack.pop();
        {
            int[] colors = new int[4];
            colors[0] = getColor(index, alphaPC);
            colors[1] = getColor(90 + index, alphaPC);
            colors[2] = getColor(180 + index, alphaPC);
            colors[3] = getColor(270 + index, alphaPC);
            stack.push();
            stack.translate(pos.x, pos.y, pos.z);
            stack.rotate(Vector3f.YN.rotationDegrees((float) rotate));
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            tessellator.draw();
            stack.pop();
        }
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        reset();
    }

    @Override
    public void onEnable() {
        reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }

    private void reset() {
        if (!circles.isEmpty()) circles.clear();
    }


    private final static List<JumpRenderer> circles = new java.util.ArrayList<>();

    private static Vector3d getVec3dFromEntity(final Entity entityIn) {
        final float PT = mc.getRenderPartialTicks();
        final double dx = entityIn.getPosX() - entityIn.lastTickPosX, dy = entityIn.getPosY() - entityIn.lastTickPosY, dz = entityIn.getPosZ() - entityIn.lastTickPosZ;
        return new Vector3d((entityIn.lastTickPosX + dx * PT + dx * 2.D), (entityIn.lastTickPosY + dy * PT), (entityIn.lastTickPosZ + dz * PT + dz * 2.D));
    }

    private void setupDraw(MatrixStack stack, final Runnable render) {
        final boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);
        stack.push();
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL14C.GL_GREATER, 0);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        if (light) RenderSystem.disableLighting();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.blendFunc(GL14C.GL_SRC_ALPHA, GL14C.GL_ONE_MINUS_CONSTANT_ALPHA);
        RectUtility.setupOrientationMatrix(stack, 0, 0, 0);
        render.run();
        RenderSystem.blendFunc(GL14C.GL_SRC_ALPHA, GL14C.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.color3f(1.F, 1.F, 1.F);
        RenderSystem.shadeModel(GL11.GL_FLAT);
        if (light) RenderSystem.enableLighting();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.alphaFunc(GL14C.GL_GREATER, .1F);
        RenderSystem.enableAlphaTest();
        stack.pop();
    }

    private int getColor(int index, float alphaPC) {
        int colorize = ColorUtility.getColor(index);
        return ColorUtility.multAlpha(colorize, alphaPC);
    }

    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = tessellator.getBuffer();

    private final class JumpRenderer {
        private final long time = System.currentTimeMillis();
        private final Vector3d pos;
        int index;

        private JumpRenderer(Vector3d pos, int index) {
            this.pos = pos;
            this.index = index;
        }

        private int getIndex() {
            return this.index;
        }

        private float getDeltaTime() {
            return (float) (System.currentTimeMillis() - time) / 3000;
        }
    }

    public static float valWave1(float value) {
        return (value > .5 ? 1 - value : value) * 2.F;
    }

    public static float valWave2(float value) {
        return Math.max(0, Math.min(1, value));
    }

    public static double easeOutCirc(double value) {
        return Math.sqrt(1 - Math.pow(value - 1, 2));
    }

    public static double easeOutBack(double value) {
        double c1 = 1.70158, c3 = c1 + 1;
        return 1 + c3 * Math.pow(value - 1, 3) + c1 * Math.pow(value - 1, 2);
    }

    public static float lerp(float value, float to, float pc) {
        return value + pc * (to - value);
    }

    public static void customRotatedObject2D(MatrixStack stack, float oXpos, float oYpos, float oWidth, float oHeight, double rotate) {
        stack.translate(oXpos + oWidth / 2, oYpos + oHeight / 2, 0);
        stack.rotate(Vector3f.ZP.rotationDegrees((float) rotate));
        stack.translate(-oXpos - oWidth / 2, -oYpos - oHeight / 2, 0);
    }
}
