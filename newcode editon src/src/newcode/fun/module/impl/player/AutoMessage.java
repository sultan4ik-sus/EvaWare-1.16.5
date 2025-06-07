package newcode.fun.module.impl.player;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;

@Annotation(
        name = "AutoMessage",
        type = TypeList.Player,
        desc = "������������� ���������� ����� ������� �������"
)
public class AutoMessage extends Module {
    public BooleanOption shar = new BooleanOption("����� ���", true);
    public BooleanOption kill = new BooleanOption("����� ����", true);

    public AutoMessage() {
        this.addSettings(shar, kill);
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}