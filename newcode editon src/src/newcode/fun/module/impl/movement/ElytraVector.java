package newcode.fun.module.impl.movement;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(
        name = "ElytraVector",
        type = TypeList.Movement,
        desc = "������� ������� ���������� �� ����� ����� �� �������, ��� ������ �� �������"
)
public class ElytraVector extends Module {
    public static ModeSetting mode = new ModeSetting("������� ��", "�������", "�������", "��������");

    public final SliderSetting elytradistance = new SliderSetting("���� ��������", 60F, 0F, 100F, 1F);

    public final SliderSetting traska = new SliderSetting("���� ������", 2.5F, 0F, 7F, 0.25F);
    public final BooleanOption draw = new BooleanOption("�������� �������", true);

    public ElytraVector() {
        this.addSettings(mode, elytradistance, traska, draw);
    }

    public boolean onEvent(Event event) {
        return false;
    }
}
