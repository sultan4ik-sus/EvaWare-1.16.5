package newcode.fun.module.impl.display;

import newcode.fun.control.events.Event;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;

@Annotation(name = "ClickGui", type = TypeList.Display)
public class ClickGui extends Module {
    public final BooleanOption sounds = new BooleanOption("Звуки панели", true);

    public ClickGui() {
        addSettings(sounds);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        setState(false);
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
