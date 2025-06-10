package eva.ware.modules.impl.movement;

import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;

@ModuleRegister(name = "AutoSprint", category = Category.Movement)
public class AutoSprint extends Module {
    public CheckBoxSetting saveSprint = new CheckBoxSetting("Сохранять спринт", true);
    public AutoSprint() {
        addSettings(saveSprint);
    }
}
