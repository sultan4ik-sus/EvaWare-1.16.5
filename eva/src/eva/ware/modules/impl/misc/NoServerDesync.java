package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import eva.ware.Evaware;
import eva.ware.events.EventPacket;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.movement.MoveHelper;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

@ModuleRegister(name = "NoServerDesync", category = Category.Misc)
public class NoServerDesync extends Module {

    private float targetYaw;
    private float targetPitch;
    private boolean isPacketSent;

    @Subscribe
    private void onPacket(EventPacket e) {
        IPacket<?> iPacket = e.getPacket();
        MoveHelper moveHelper = Evaware.getInst().getModuleManager().getMoveHelper();
        if (mc.player == null) return;
        if (moveHelper.isEnabled() && moveHelper.noSlow.getValue() && moveHelper.noSlowMode.is("GrimLatest")) {
            SHeldItemChangePacket wrapper;
            int serverSlot;
            if (mc.player != null && (iPacket = e.getPacket()) instanceof SHeldItemChangePacket && (serverSlot = (wrapper = (SHeldItemChangePacket)iPacket).getHeldItemHotbarIndex()) != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(Math.max(mc.player.inventory.currentItem - 1, 0)));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                e.cancel();
            }
        } else {
            if (iPacket instanceof SHeldItemChangePacket wrapper) {
                final int serverSlot = wrapper.getHeldItemHotbarIndex();
                if (serverSlot != mc.player.inventory.currentItem) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    e.cancel();
                }
            }
        }
        if (e.getPacket() instanceof SPlayerPositionLookPacket && e.isSend()) {
            SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket)iPacket;
            packet.setYaw(mc.player == null ? packet.getYaw() : mc.player.rotationYaw);
            packet.setPitch(mc.player == null ? packet.getPitch() : mc.player.rotationPitch);
        }
    }
}
