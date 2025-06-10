package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventMouseButtonPress;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import net.minecraft.util.math.RayTraceResult;

@ModuleRegister(name = "PushAttack", category = Category.Combat)
public class PushAttack extends Module {

    @Subscribe
    public void onMouseClicK(EventMouseButtonPress e) {
        if (e.getButton() == 0 && mc.player != null && mc.player.isHandActive() && mc.currentScreen == null && (mc.objectMouseOver == null || mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK)) {
            mc.clickMouse();
        }
    }

}
