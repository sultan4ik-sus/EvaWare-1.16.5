package newcode.fun.module.impl.player;

import net.minecraft.network.IPacket;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.SliderSetting;

import java.util.concurrent.ConcurrentLinkedQueue;

@Annotation(name = "SlowPackets", type = TypeList.Player)
public class SlowPackets extends Module {

    private SliderSetting delay = new SliderSetting("Задержка", 1000,100,5000, 100);

    public SlowPackets() {
        super();
        addSettings(delay);
    }

    public static final ConcurrentLinkedQueue<Scaffold.TimedPacket> packets = new ConcurrentLinkedQueue<>();

    @Override
    protected void onDisable() {
        super.onDisable();
        for (Scaffold.TimedPacket p : packets) {
            mc.player.connection.getNetworkManager().sendPacketWithoutEvent(p.getPacket());
        }
        packets.clear();

    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventPacket e) {
            if (e.isSendPacket()) {
                IPacket<?> packet = e.getPacket();
                packets.add(new Scaffold.TimedPacket(packet, System.currentTimeMillis()));
                e.setCancel(true);
            }
        }
        if (event instanceof EventMotion e) {
            for (Scaffold.TimedPacket timedPacket : packets) {
                if (System.currentTimeMillis() - timedPacket.getTime() >= delay.getValue().intValue()) {
                    mc.player.connection.getNetworkManager().sendPacketWithoutEvent(timedPacket.getPacket());
                    packets.remove(timedPacket);
                }
            }
        }
        return false;
    }
}
