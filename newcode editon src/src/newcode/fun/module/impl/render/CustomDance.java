package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.ModeSetting;

@Annotation(name = "CustomDance", type = TypeList.Render, desc = "��������� ��������� �������� ������ ���������")
public class CustomDance extends Module {

    public ModeSetting modes = new ModeSetting("���", "�������", "�������", "������");
    public CustomDance() {
        addSettings(modes);
    }


    @Override
    public boolean onEvent(final Event event) {
        return false;
    }
}
