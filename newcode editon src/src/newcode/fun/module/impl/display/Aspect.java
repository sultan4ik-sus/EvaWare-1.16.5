package newcode.fun.module.impl.display;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "Aspect", type = TypeList.Display)
public class Aspect extends Module {
    public SliderSetting widthe = new SliderSetting("Ширина", 0.3F, 0F, 0.8F, 0.05F);
    public Aspect() {
        this.addSettings(new Setting[]{widthe});
    }


    public boolean onEvent(Event event) {
        return false;
    }
}

