package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(
        name = "SeeInvisibles",
        type = TypeList.Render
)

public class SeeInvisibles extends Module {
    public SliderSetting alpha = new SliderSetting("Прозрачность", 0.5F, 0.3F, 1.0F, 0.1F);
    public SeeInvisibles() {
        this.addSettings(alpha);
    }

    public boolean onEvent(Event event) {
        return false;
    }
}
