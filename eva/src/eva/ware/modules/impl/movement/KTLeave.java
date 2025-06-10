package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventChangeWorld;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.utils.math.MathUtility;
import net.minecraft.network.play.client.CPlayerPacket;

@ModuleRegister(name = "KTLeave", category = Category.Movement)
public class KTLeave extends Module {

    // без сомнения, базовый модуль для целестиала (.bind add ktleave space)

    final ModeSetting mode = new ModeSetting("Способ телепорта", "Packet", "Vanilla", "Packet", "VClipSpam");

    public KTLeave() {
        addSettings(mode);
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        toggle();
    }

    @Override
    public void onEnable() {
        int xCoord = MathUtility.randomInt(-500, 500);
        int zCoord = MathUtility.randomInt(-500, 500);
        if (mode.is("Packet")) {
            for (int i = 0; i < 12; ++i) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(xCoord, 180.0, zCoord, true));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(xCoord, 180.0, zCoord, true));
            }
            for (int i = 0; i < 12; ++i) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), 180.0, mc.player.getPosZ(), true));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), 180.0, mc.player.getPosZ(), true));
            }
        }

        if (mode.is("Vanilla")) {
            mc.player.setPositionAndUpdate(xCoord, 180, zCoord);
        }

        if (mode.is("VClipSpam")) {
            for (int j = 0; j < 5; ++j) {
                int packetsCount = Math.max((20 / 1000), 3);
                for (int i = 0; i < packetsCount; i++) {
                    mc.player.connection.sendPacket(new CPlayerPacket(mc.player.isOnGround()));
                }
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 20, mc.player.getPosZ(), false));
                mc.player.setPosition(mc.player.getPosX(), mc.player.getPosY() + 20, mc.player.getPosZ());
            }
        }

        super.onEnable();
    }
}
