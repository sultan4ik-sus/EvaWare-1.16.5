package newcode.fun.module.impl.movement;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(
        name = "ElytraBooster",
        type = TypeList.Movement,
        desc = "�������� ���� �� �������"
)
public class ElytraBooster extends Module {
    public static ModeSetting mode = new ModeSetting("�����", "���������", "���������", "������������");
    public static SliderSetting speed = new SliderSetting("�������� ��� �������", 1.6f, 1.51f, 2.15f, 0.01f).setVisible(() -> mode.is("���������"));
    public static SliderSetting speed2 = new SliderSetting("�������� � ��������", 1.64f, 1.51f, 2.2f, 0.01f).setVisible(() -> mode.is("���������"));
    public static SliderSetting speed4 = new SliderSetting("������ �������", 0.04f, 0.01f, 0.1f, 0.01f).setVisible(() -> mode.is("������������"));

    public ElytraBooster() {
        this.addSettings(mode, speed, speed2, speed4);
    }

    public boolean onEvent(Event event) {
        return false;
    }
}
