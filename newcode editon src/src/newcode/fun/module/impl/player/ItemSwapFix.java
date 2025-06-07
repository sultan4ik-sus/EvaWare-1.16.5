package newcode.fun.module.impl.player;

import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;

@Annotation(name = "ItemSwapFix", type = TypeList.Player)
public class ItemSwapFix extends Module {

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventPacket packetEvent) {
            if (packetEvent.isReceivePacket()) {
                if (packetEvent.getPacket() instanceof SHeldItemChangePacket packetHeldItemChange) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    event.setCancel(true);
                }
            }
        }
        return false;
    }
}
