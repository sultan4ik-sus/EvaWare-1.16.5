package newcode.fun.module.impl.render;

import net.minecraft.network.play.server.SUpdateTimePacket;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.*;


@Annotation(name = "Ambience", type = TypeList.Render)
public class Ambience extends Module {

    private ModeSetting timeOfDay = new ModeSetting("�����", "����", "����", "����", "�����", "�������");
    public BooleanOption fog = new BooleanOption("�������� �����", false);
    public SliderSetting distanceFog = new SliderSetting("��������� ������", 4.0F, 1.1f, 50.0F, 0.01f).setVisible(() -> fog.get());

    public Ambience() {
        addSettings(timeOfDay, distanceFog, fog);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventPacket eventPacket && ((EventPacket) event).isReceivePacket()) {
            if (eventPacket.getPacket() instanceof SUpdateTimePacket) {
                eventPacket.setCancel(true);
            }
        }
        if (event instanceof EventUpdate) {
            float time = 0;
            switch (timeOfDay.get()) {
                case "����" -> time = 1000;
                case "�����" -> time = 12400;
                case "�������" -> time = 23000;
                case "����" -> time = 15000;
            }
            mc.world.setDayTime((long) time);
        }
        return false;
    }
}
