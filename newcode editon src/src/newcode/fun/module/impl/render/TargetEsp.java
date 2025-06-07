package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(
        name = "TargetEsp",
        type = TypeList.Render
)
public class TargetEsp extends Module {
    public final ModeSetting mode = new ModeSetting("Выбор режима", "Квадрат", new String[]{"Квадрат", "Призраки", "Обычный круг"});
    public final SliderSetting size = (new SliderSetting("Размер", 60F, 50F, 100F, 1F)).setVisible(() -> mode.is("Квадрат"));
    public final BooleanOption attack = new BooleanOption("Красный при ударе", true).setVisible(() -> (mode.is("Квадрат") || mode.is("Призраки")));

    public TargetEsp() {
        this.addSettings(mode, size, attack);
    }

    public boolean onEvent(Event event) {
        return false;
    }
}

