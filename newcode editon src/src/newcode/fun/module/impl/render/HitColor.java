package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "HitColor", type = TypeList.Render)
public class HitColor extends Module {

    public SliderSetting intensivity = new SliderSetting("Интенсивность", 0.3f, 0.1f, 1, 0.1f);

    public HitColor() {
        super();
        addSettings(intensivity);
    }

    @Override
    public boolean onEvent(Event event) {

        return false;
    }
}
