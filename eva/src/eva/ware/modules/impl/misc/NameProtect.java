package eva.ware.modules.impl.misc;

import eva.ware.Evaware;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

@ModuleRegister(name = "NameProtect", category = Category.Misc)
public class NameProtect extends Module {
    public static String getReplaced(String input) {
        if (Evaware.getInst() != null && Evaware.getInst().getModuleManager().getNameProtect().isEnabled()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), TextFormatting.LIGHT_PURPLE + "♡Evachka♡" + TextFormatting.RESET);
        }
        return input;
    }
}
