package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;

@Annotation(name = "ChinaHat", type = TypeList.Render, desc = "—оздаЄт китайскую шл€пу над головой")
public class ChinaHat extends Module {

    public ChinaHat() {
        this.addSettings();
    }

    @Override
    public boolean onEvent(Event event) {

        return false;
    }
}