package eva.ware.modules.impl.visual;

import eva.ware.Evaware;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;

@ModuleRegister(name = "WallHack", category = Category.Visual)
public class WallHack extends Module {

    @Override
    public void onEnable() {
        super.onEnable();
        betterComp(Evaware.getInst().getModuleManager().getShaderESP());
    }

}
