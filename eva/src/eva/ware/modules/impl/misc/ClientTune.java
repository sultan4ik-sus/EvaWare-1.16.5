package eva.ware.modules.impl.misc;

import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;

import static java.lang.Math.signum;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@ModuleRegister(name = "ClientTune", category = Category.Misc)
public class ClientTune extends Module {

    public ModeSetting mode = new ModeSetting("Тип", "VL", "Default", "Bubbles", "Pop", "Heavy", "Windows", "Slide", "Droplet", "VL");
    public SliderSetting volume = new SliderSetting("Громкость", 60.0f, 0.0f, 100.0f, 1.0f);

    public ClientTune() {
        addSettings(mode, volume);
        toggle();
    }

    public String getFileName(boolean state) {
        switch (mode.getValue()) {
            case "Default" -> {
                return state ? "enable" : "disable";
            }
            case "Bubbles" -> {
                return state ? "enableBubbles" : "disableBubbles";
            }
            case "Pop" -> {
                return state ? "popenable" : "popdisable";
            }
            case "Heavy" -> {
                return state ? "heavyenable" : "heavydisable";
            }
            case "Windows" -> {
                return state ? "winenable" : "windisable";
            }
            case "Droplet" -> {
                return state ? "dropletenable" : "dropletdisable";
            }
            case "Slide" -> {
                return state ? "slideenable" : "slidedisable";
            }
            case "VL" -> {
                return state ? "enablevl" : "disablevl";
            }
        }
        return "";
    }
}
