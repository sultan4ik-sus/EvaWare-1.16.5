package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;

@Annotation(name = "Chams", type = TypeList.Render)
public class Chams extends Module {

    @Override
    public boolean onEvent(Event event) {

        return false;
    }
}


