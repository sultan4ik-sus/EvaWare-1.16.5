package newcode.fun.module.impl.player;

import newcode.fun.control.events.Event;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.SliderSetting;



@Annotation(name = "ItemScroller", type = TypeList.Player)
public class ItemScroller extends Module {

    public SliderSetting delay = new SliderSetting("Задержка", 80, 0, 1000, 1);


    public ItemScroller() {
        addSettings(delay);
    }

    @Override
    public boolean onEvent(Event event) {

        return false;
    }
}
