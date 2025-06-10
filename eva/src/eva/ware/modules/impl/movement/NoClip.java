package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "NoClip", category = Category.Movement)
public class NoClip extends Module {

    @Subscribe
    private void onMoving(EventMoving move) {
            if (!collisionPredict(move.getTo())) {
                if (move.isCollidedHorizontal())
                    move.setIgnoreHorizontal(true);
                if (move.getMotion().y > 0 || mc.player.isSneaking()) {
                    move.setIgnoreVertical(true);
                }
                move.getMotion().y = Math.min(move.getMotion().y, 99999);
        }
    }


    public boolean collisionPredict(Vector3d to) {
        boolean prevCollision = mc.world
                .getCollisionShapes(mc.player, mc.player.getBoundingBox().shrink(0.0625D)).toList().isEmpty();
        Vector3d backUp = new Vector3d(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        mc.player.setPosition(to.x, to.y, to.z);
        boolean collision = mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().shrink(0.0625D))
                .toList().isEmpty() && prevCollision;
        mc.player.setPosition(backUp.x, backUp.y, backUp.z);
        return collision;
    }
}

