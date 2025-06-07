package newcode.fun.module.impl.player;

import net.minecraft.client.gui.screen.DeathScreen;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;

@Annotation(name = "AutoRespawn", type = TypeList.Player)
public class AutoRespawn extends Module {

    @Override
    public boolean onEvent(final Event event) {
        if (event instanceof EventUpdate) {
            if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime > 2) {
                mc.player.respawnPlayer();
                mc.displayGuiScreen(null);
            }
        }
        return false;
    }
}
