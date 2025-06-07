package newcode.fun.module.impl.player;

import newcode.fun.control.events.Event;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;


@Annotation(name = "NoPush", type = TypeList.Player)
public class NoPush extends Module {

    public final MultiBoxSetting modes = new MultiBoxSetting("Тип",
            new BooleanOption("Игроки", true),
            new BooleanOption("Блоки", true));

    public NoPush() {
        addSettings(modes);
    }

    @Override
    public boolean onEvent(final Event event) {
        return false;
    }
}
