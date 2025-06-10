package eva.ware.modules.impl.misc;

import eva.ware.Evaware;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;

@ModuleRegister(name = "BetterMinecraft", category = Category.Misc)
public class BetterMinecraft extends Module {

    public final CheckBoxSetting smoothCamera = new CheckBoxSetting("Плавная камера", true);
    public final CheckBoxSetting smoothTab = new CheckBoxSetting("Плавный таб", true);
    public final CheckBoxSetting smoothChat = new CheckBoxSetting("Плавный чат", true);
    public final CheckBoxSetting betterTab = new CheckBoxSetting("Улучшенный таб", true);
    public final CheckBoxSetting betterChat = new CheckBoxSetting("Улучшенный чат", true);
    public static CheckBoxSetting fpsBoot = new CheckBoxSetting("Оптимизировать", false);

    public BetterMinecraft() {
        addSettings(betterTab, betterChat, smoothCamera, smoothTab, smoothChat, fpsBoot);
    }

    public static boolean isFpsMode() {
        return Evaware.getInst().getModuleManager().getBetterMinecraft().isEnabled() && fpsBoot.getValue();
    }
}
