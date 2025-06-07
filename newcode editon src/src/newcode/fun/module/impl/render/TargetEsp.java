package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(
        name = "TargetEsp",
        type = TypeList.Render
)
public class TargetEsp extends Module {
    public final ModeSetting mode = new ModeSetting("����� ������", "�������", new String[]{"�������", "��������", "������� ����"});
    public final SliderSetting size = (new SliderSetting("������", 60F, 50F, 100F, 1F)).setVisible(() -> mode.is("�������"));
    public final BooleanOption attack = new BooleanOption("������� ��� �����", true).setVisible(() -> (mode.is("�������") || mode.is("��������")));

    public TargetEsp() {
        this.addSettings(mode, size, attack);
    }

    public boolean onEvent(Event event) {
        return false;
    }
}

