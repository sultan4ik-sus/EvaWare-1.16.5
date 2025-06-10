package eva.ware.modules.impl.combat;

import eva.ware.manager.friend.FriendManager;
import eva.ware.events.EventPacket;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CUseEntityPacket;
import com.google.common.eventbus.Subscribe;

@ModuleRegister(name = "NoFriendHurt", category = Category.Combat)
public class NoFriendHurt extends Module {
	
	// дайте мне по роже пж
	
	@Subscribe
    public void onEvent(EventPacket event) {
        if (event.getPacket() instanceof CUseEntityPacket) {
            CUseEntityPacket cUseEntityPacket = (CUseEntityPacket) event.getPacket();
            Entity entity = cUseEntityPacket.getEntityFromWorld(mc.world);
            if (entity instanceof RemoteClientPlayerEntity &&
                    FriendManager.isFriend(entity.getName().getString()) &&
                    cUseEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                event.cancel();
            }
        }
    }
	
}
