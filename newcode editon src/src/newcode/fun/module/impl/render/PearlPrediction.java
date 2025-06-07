//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package newcode.fun.module.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.ui.midnight.Style;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.math.PlayerPositionTracker;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.RenderUtils.IntColor;
import org.joml.Vector4d;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

@Annotation(
        name = "PearlPrediction",
        type = TypeList.Render
)

public class PearlPrediction extends Module {
    public final BooleanOption element = (new BooleanOption("Время падение", true));
    public PearlPrediction() {
        this.addSettings(element);
    }
    public HashMap<Vector4d, EnderPearlEntity> positions = new HashMap();

    public boolean onEvent(Event event) {
        if (event instanceof EventRender render) {
            if (render.isRender3D()) {
                this.updateItemsPositions(render.partialTicks);
            }
            if (render.isRender2D()) {
                this.renderItemPearl(render.matrixStack);
            }
        }
        if (event instanceof EventRender && ((EventRender)event).isRender3D()) {
            RenderSystem.pushMatrix();
            RenderSystem.translated(-mc.getRenderManager().renderPosX(), -mc.getRenderManager().renderPosY(), -mc.getRenderManager().renderPosZ());
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.disableDepthTest();
            GL11.glEnable(2848);
            RenderSystem.lineWidth(1.0F);
            buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            Iterator var2 = mc.world.getAllEntities().iterator();

            while(var2.hasNext()) {
                Entity e = (Entity)var2.next();
                if (e instanceof EnderPearlEntity) {
                    EnderPearlEntity pearl = (EnderPearlEntity)e;
                    this.renderLine(pearl);
                }
            }

            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);
            RenderSystem.enableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            GL11.glDisable(2848);
            RenderSystem.popMatrix();
        }

        return false;
    }

    private double calculateInterpolatedPosition(double previousPos, double currentPos, float partialTicks) {
        return -(previousPos + (currentPos - previousPos) * (double)partialTicks);
    }

    private void renderLine(EnderPearlEntity pearl) {
        Vector3d pearlPosition = pearl.getPositionVec().add(0.0, 0.0, 0.0);
        Vector3d pearlMotion = pearl.getMotion();

        for(int i = 0; i <= 300; ++i) {
            Vector3d lastPosition = pearlPosition;
            pearlPosition = pearlPosition.add(pearlMotion);
            pearlMotion = this.updatePearlMotion(pearl, pearlMotion);
            if (this.shouldEntityHit(pearlPosition.add(0.0, 1.0, 0.0), lastPosition.add(0.0, 1.0, 0.0)) || pearlPosition.y <= 0.0) {
                break;
            }

            float[] colors = this.getLineColor(i);
            buffer.pos(lastPosition.x, lastPosition.y, lastPosition.z).color(colors[0], colors[1], colors[2], 1.0F).endVertex();
            buffer.pos(pearlPosition.x, pearlPosition.y, pearlPosition.z).color(colors[0], colors[1], colors[2], 1.0F).endVertex();
        }

    }
    private void updateItemsPositions(float partialTicks) {
        this.positions.clear();
        Iterator var2 = mc.world.getAllEntities().iterator();

        while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            if (PlayerPositionTracker.isInView(entity) && entity instanceof EnderPearlEntity item) {
                Vector4d position = PlayerPositionTracker.updatePlayerPositions(item, partialTicks);
                if (position != null) {
                    this.positions.put(position, item);
                }
            }
        }
    }
    private Vector3d updatePearlMotion(EnderPearlEntity pearl, Vector3d originalPearlMotion) {
        Vector3d pearlMotion = originalPearlMotion;
        if (pearl.isInWater()) {
            pearlMotion = pearlMotion.scale(0.800000011920929);
        } else {
            pearlMotion = pearlMotion.scale(0.9900000095367432);
        }

        if (!pearl.hasNoGravity()) {
            pearlMotion.y -= (double)pearl.getGravityVelocity();
        }

        return pearlMotion;
    }

    private boolean shouldEntityHit(Vector3d pearlPosition, Vector3d lastPosition) {
        Minecraft var10006 = mc;
        RayTraceContext rayTraceContext = new RayTraceContext(lastPosition, pearlPosition, BlockMode.COLLIDER, FluidMode.NONE, Minecraft.player);
        BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
        return blockHitResult.getType() == net.minecraft.util.math.RayTraceResult.Type.BLOCK;
    }
    private void renderItemPearl(MatrixStack stack) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int main = ColorUtils.rgba(255, 255, 255, 255);
        int back = ColorUtils.rgba(0, 0, 0, 128);
        Style current = Manager.STYLE_MANAGER.getCurrentStyle();
        Vector4i colors = new Vector4i(current.getColor(0), current.getColor(90), current.getColor(180), current.getColor(270));
        Iterator var2 = this.positions.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Vector4d, EnderPearlEntity> entry = (Map.Entry)var2.next();
            Vector4d position = (Vector4d)entry.getKey();
            EnderPearlEntity pearlEntity = entry.getValue();

            if (position != null && pearlEntity != null) {
                double x = position.x;
                double y = position.y + 6;
                double endX = position.z;

                long timeRemaining = getTimeRemaining(pearlEntity);
                String timeString = String.format("%02d:%02d", timeRemaining / 100, timeRemaining % 100); // Форматирование времени

                float width = Fonts.newcode[10].getWidth("     " + timeString + " сек");
                float height = 9;
                if (this.element.get()) {
                    RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/pearl.png"), (float) (x + (endX - x) / 2.0 - (double)(width / 2.0F + 2f)), (float) (y - 4.0 - 7 - 4.5f), 8F, 8, -1);
                    Fonts.msMedium[10].drawStringWithShadow(stack, "     " + timeString + " сек", x + (endX - x) / 2.0 - (double)(width / 2.0F), y - 4.0 - 6.5f, -1);
                }
            }
        }
    }
    private long getTimeRemaining(EnderPearlEntity pearlEntity) {
        Vector3d pearlPosition = pearlEntity.getPositionVec();
        Vector3d pearlMotion = pearlEntity.getMotion();
        double distanceToGround = pearlPosition.y;
        double verticalSpeed = pearlMotion.y;

        if (verticalSpeed > 0) {
            return 600;
        }

        long ticksRemaining = Math.round(-distanceToGround / verticalSpeed);
        return ticksRemaining;
    }
    private float[] getLineColor(int index) {
        StyleManager var10000 = Manager.STYLE_MANAGER;
        int color = StyleManager.getCurrentStyle().getColor(index * 2);
        return IntColor.rgb(color);
    }
}
