package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import eva.ware.Evaware;
import eva.ware.events.EventEntityLeave;
import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.ui.notify.impl.WarningNotify;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;

@ModuleRegister(name = "PlayerHelper", category = Category.Misc)
public class PlayerHelper extends Module {

    public final CheckBoxSetting portalgodmode = new CheckBoxSetting("PortalGodMode", false);
    public final CheckBoxSetting srpspoofer = new CheckBoxSetting("SrpSpoofer", false);
    public final CheckBoxSetting leaveTracker = new CheckBoxSetting("LeaveTracker", true);
    public final CheckBoxSetting speedmine = new CheckBoxSetting("SpeedMine", false);
    public final CheckBoxSetting deathPosition = new CheckBoxSetting("DeathPosition", false);

    public PlayerHelper() {
        addSettings(portalgodmode, srpspoofer, leaveTracker, speedmine, deathPosition);
    }

    @Subscribe
    private void onEntityLeave(EventEntityLeave eel) {
        if (leaveTracker.getValue()) {
            Entity entity = eel.getEntity();

            if (!isEntityValid(entity)) {
                return;
            }

            String message = "Игрок " + entity.getDisplayName().getString() + " ливнул на " + entity.getStringPosition();

            print(message);
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CConfirmTeleportPacket && portalgodmode.getValue()) {
            e.cancel();
        }

        if (e.getPacket() instanceof SSendResourcePackPacket && srpspoofer.getValue()) {
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
            if (mc.currentScreen != null) {
                mc.player.closeScreen();
            }

            e.cancel();
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!portalgodmode.getValue() && !srpspoofer.getValue() && !deathPosition.getValue() && !leaveTracker.getValue() && !speedmine.getValue()) {
            toggle();
            Evaware.getInst().getNotifyManager().add(0, new WarningNotify("Включите что-нибудь!", 3000));
        }

        if (speedmine.getValue()) {
            mc.playerController.blockHitDelay = 0;
            mc.playerController.resetBlockRemoving();
        }
    }

    private boolean isEntityValid(Entity entity) {
        if (!(entity instanceof AbstractClientPlayerEntity) || entity instanceof ClientPlayerEntity) {
            return false;
        }

        return !(mc.player.getDistance(entity) < 100);
    }
}
