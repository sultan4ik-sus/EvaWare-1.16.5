package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.ButtonSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "ViewModel", type = TypeList.Render, desc = "������ ������� ����� ���")
public class ViewMode extends Module {
    public final SliderSetting right_x = new SliderSetting("����� �� X", 0.0F, -2, 2, 0.05F);
    public final SliderSetting right_y = new SliderSetting("����� �� Y", 0.0F, -2, 2, 0.05F);
    public final SliderSetting right_z = new SliderSetting("����� �� Z", 0.0F, -2, 2, 0.05F);
    public final SliderSetting left_x = new SliderSetting("���� �� X", 0.0F, -2, 2, 0.05F);
    public final SliderSetting left_y = new SliderSetting("���� �� Y", 0.0F, -2, 2, 0.05F);
    public final SliderSetting left_z = new SliderSetting("���� �� Z", 0.0F, -2, 2, 0.05F);
    public ButtonSetting reset = new ButtonSetting("��������", () -> {
        right_x.setValue(0.0F);
        right_y.setValue(0.0F);
        right_z.setValue(0.0F);
        left_x.setValue(0.0F);
        left_y.setValue(0.0F);
        left_z.setValue(0.0F);
    });

    public ViewMode() {
        addSettings(right_x, right_y, right_z, left_x, left_y, left_z, reset);
    }
    @Override
    public boolean onEvent(Event event) {

        return false;
    }
}
