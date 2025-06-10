package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.manager.friend.FriendManager;
import eva.ware.events.EventRender3D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.combat.AntiBot;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import eva.ware.utils.player.EntityUtility;
import eva.ware.utils.render.color.ColorUtility;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import static org.lwjgl.opengl.GL11.*;

@ModuleRegister(name = "Tracers", category = Category.Visual)
public class Tracers extends Module {

    public ModeListSetting targets = new ModeListSetting("Отображать",
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Предметы", false),
            new CheckBoxSetting("Мобы", false)
    );

    public Tracers() {
        addSettings(targets);
    }

    @Subscribe
    public void onRender(EventRender3D e) {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        glLineWidth(1);

        Vector3d cam = new Vector3d(0, 0, 150)
                .rotatePitch((float) -(Math.toRadians(mc.getRenderManager().info.getPitch())))
                .rotateYaw((float) -Math.toRadians(mc.getRenderManager().info.getYaw()));

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof PlayerEntity && entity != mc.player && targets.is("Игроки").getValue()
                    || entity instanceof ItemEntity && targets.is("Предметы").getValue()
                    || (entity instanceof AnimalEntity || entity instanceof MobEntity) && targets.is("Мобы").getValue()
            )) continue;
            if (AntiBot.isBot(entity) || !entity.isAlive()) continue;

            Vector3d pos = EntityUtility.getInterpolatedPositionVec(entity).subtract(mc.getRenderManager().info.getProjectedView());

            ColorUtility.setColor(FriendManager.isFriend(entity.getName().getString()) ? FriendManager.getColor() : -1);

            buffer.begin(1, DefaultVertexFormats.POSITION);

            buffer.pos(cam.x, cam.y, cam.z).endVertex();
            buffer.pos(pos.x, pos.y, pos.z).endVertex();


            tessellator.draw();
        }

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
    }
}
