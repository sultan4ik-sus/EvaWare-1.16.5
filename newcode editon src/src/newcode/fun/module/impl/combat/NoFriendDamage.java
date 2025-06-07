package newcode.fun.module.impl.combat;

import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CUseEntityPacket;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.Manager;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;


@Annotation(name = "NoFriendDamage", type = TypeList.Combat)
public class NoFriendDamage extends Module {

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventPacket packet) {
            if (packet.getPacket() instanceof CUseEntityPacket useEntityPacket) {
                Entity entity = useEntityPacket.getEntityFromWorld(mc.world);
                if (entity instanceof RemoteClientPlayerEntity
                        && Manager.FRIEND_MANAGER.isFriend(entity.getName().getString())
                        && useEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                    event.setCancel(true);
                }
            }
        }
        return false;
    }
}
