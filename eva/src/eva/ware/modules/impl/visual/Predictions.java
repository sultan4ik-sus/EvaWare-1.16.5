package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventRender2D;
import eva.ware.events.EventRender3D;
import eva.ware.manager.Theme;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.api.Module;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.render.engine2d.ProjectionUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.font.ClientFonts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

@ModuleRegister(name = "Predictions", category = Category.Visual)
public class Predictions extends Module {

    record PearlPoint(Vector3d position, int ticks) {}

    private Map<String, CompactAnimation> animations = new HashMap<>();
    final List<PearlPoint> pearlPoints = new ArrayList<>();

    @Subscribe
    public void fuckingRender(EventRender2D e) {
        for (PearlPoint pearlPoint : pearlPoints) {
            Vector3d pos = pearlPoint.position;
            Vector2f projection = ProjectionUtility.project(pos.x, pos.y - 0.3F, pos.z);
            int ticks = pearlPoint.ticks;

            if (projection.equals(new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE))) {
                continue;
            }

            double time = ticks * 50 / 1000.0;
            String text = String.format("%.1f" + " сек.", time);
            float width = Fonts.montserrat.getWidth(text, 7);

            float textWidth = width + 11 + 11;

            float posX = projection.x - textWidth / 2;
            float posX1 = projection.x / 2;
            float posY = projection.y;

            int black = ColorUtility.getColor(10, 10, 10, 140);
            RenderUtility.drawRoundedRect(posX + 3, posY + 2 - 3, textWidth - 4, 16 - 3, 2, black);
            RenderUtility.drawImage(new ResourceLocation("eva/images/modules/timers/pearl.png"), posX + 5 , posY, 10, 10, ColorUtility.setAlpha(-1, (int) (255)));

            ClientFonts.interBold[16].drawString(e.getMatrixStack(), text, posX + 17, posY + 3f, -1);
        }
    }

    @Subscribe
    public void onRender(EventRender3D event) {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();

        glTranslated(-renderOffset.x, -renderOffset.y, -renderOffset.z);

        glLineWidth(2);

        buffer.begin(1, DefaultVertexFormats.POSITION);

        pearlPoints.clear();
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderPearlEntity throwable) {
                Vector3d motion = throwable.getMotion();
                Vector3d pos = throwable.getPositionVec();
                Vector3d prevPos;
                int ticks = 0;
                for (int i = 0; i < 150; i++) {
                    prevPos = pos;
                    pos = pos.add(motion);
                    motion = getNextMotion(throwable, motion);
                    ColorUtility.setColor(Theme.mainRectColor);

                    buffer.pos(prevPos.x, prevPos.y, prevPos.z).endVertex();

                    RayTraceContext rayTraceContext = new RayTraceContext(prevPos, pos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, throwable);
                    BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
                    boolean isLast = blockHitResult.getType() == RayTraceResult.Type.BLOCK;

                    if (isLast) {
                        pos = blockHitResult.getHitVec();
                    }

                    buffer.pos(pos.x, pos.y, pos.z).endVertex();

                    if (blockHitResult.getType() == BlockRayTraceResult.Type.BLOCK || pos.y < -128) {
                        pearlPoints.add(new PearlPoint(pos, ticks));
                        break;
                    }
                    ticks++;
                }
            }
        }

        tessellator.draw();

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
    }

    private Vector3d getNextMotion(ThrowableEntity throwable, Vector3d motion) {
        if (throwable.isInWater()) {
            motion = motion.scale(0.8);
        } else {
            motion = motion.scale(0.99);
        }

        if (!throwable.hasNoGravity()) {
            motion.y -= throwable.getGravityVelocity();
        }

        return motion;
    }
}