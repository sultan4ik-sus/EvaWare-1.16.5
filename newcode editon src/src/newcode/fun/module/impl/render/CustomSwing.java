package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "CustomSwing", type = TypeList.Render, desc = "������ �������� �����")
public class CustomSwing extends Module {
    public final ModeSetting swordAnim = new ModeSetting("���", "������������", "������������", "��������������", "������������");
    public final SliderSetting angle = new SliderSetting("����", 110, 0, 360, 5).setVisible(() -> swordAnim.is("��������������"));
    public final SliderSetting swipePower = new SliderSetting("���� ������", 35, 10, 100, 1);
    public final SliderSetting swipeSpeed = new SliderSetting("��������� ������", 6, 1, 10, 1);
    public static BooleanOption item360 = new BooleanOption("������� �� 360", false);

    public CustomSwing() {
        addSettings(swordAnim, angle, swipePower, swipeSpeed, item360);
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
