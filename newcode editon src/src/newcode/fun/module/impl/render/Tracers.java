package newcode.fun.module.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.module.TypeList;
import org.lwjgl.opengl.GL11;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ColorSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.render.ColorUtils;

@Annotation(name = "Tracers", type = TypeList.Render)
public class Tracers extends Module {
    public ColorSetting color = new ColorSetting("Цвет", ColorUtils.rgba(255, 255, 255, 255));

    public ColorSetting friendColor = new ColorSetting("Цвет друзей", ColorUtils.rgba(0, 255, 0, 255));

    public SliderSetting width = new SliderSetting("Ширина", 1, 1, 10, 0.1f);

    public SliderSetting alpha = new SliderSetting("Прозрачность", 255, 0, 255, 1);

    public BooleanOption ignoreNaked = new BooleanOption("Игнорировать голых", true);

    public Tracers() {
        addSettings(color, friendColor, width, alpha, ignoreNaked);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender eventRender) {
            if (eventRender.isRender3D()) {

                GL11.glPushMatrix();
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glLineWidth(width.getValue().floatValue());
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                Vector3d vec = new Vector3d(0, 0, 150)
                        .rotatePitch((float) -(Math.toRadians(mc.player.rotationPitch)))
                        .rotateYaw((float) -Math.toRadians(mc.player.rotationYaw));
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

                float alpha = ((int) this.alpha.getValue().floatValue()) / 255F;

                for (PlayerEntity entity : mc.world.getPlayers()) {
                    if (entity instanceof RemoteClientPlayerEntity && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                        int tracersColor = Manager.FRIEND_MANAGER.isFriend(entity.getName().getString()) ? friendColor.get() : color.get();

                        double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
                        double y = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY();
                        double z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();

                        ColorUtils.setColor(tracersColor);

                        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
                        bufferBuilder.pos(vec.x, vec.y, vec.z).endVertex();
                        bufferBuilder.pos(x, y, z).endVertex();
                        Tessellator.getInstance().draw();
                    }
                }
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_BLEND);
                GlStateManager.color4f(1, 1, 1, 1);
                GL11.glPopMatrix();
            }
        }
        return false;
    }
}
