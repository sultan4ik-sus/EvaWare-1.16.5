package newcode.fun.module.impl.player;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;

@Annotation(name = "Optimization", type = TypeList.Player)
public class Optimization extends Module {

    public final MultiBoxSetting optimizeSelection = new MultiBoxSetting("��������������", new BooleanOption("����", true), new BooleanOption("��������", true), new BooleanOption("��������", false));

     public Optimization() {
         addSettings(optimizeSelection);
     }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
