package newcode.fun.module.impl.movement;

import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMove;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;

@Annotation(name = "NoClip", type = TypeList.Movement)
public class NoClip extends Module {

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventMove move) {
            if (!collisionPredict(move.to())) {
                if (move.isCollidedHorizontal())
                    move.setIgnoreHorizontalCollision();
                if (move.motion().y > 0 || mc.player.isSneaking()) {
                    move.setIgnoreVerticalCollision();
                }
                move.motion().y = Math.min(move.motion().y, 99999);
            }
        }
        return false;
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
