package newcode.fun.module.impl.combat;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.utils.ClientUtils;

@Annotation(name = "SyncTps", type = TypeList.Combat)
public class SyncTps extends Module {

    public SyncTps() {
        addSettings();
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        ClientUtils.sendMessage("Если сервер не лагает то лучше будет выключить " + TextFormatting.RED + "SyncTps");
        super.onEnable();
    }
}
