package newcode.fun.module.impl.player;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.utils.ClientUtils;


@Annotation(name = "DeathCoords", type = TypeList.Player)
public class DeathCoords extends Module {
    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            checkDeathCoordinates();
        }
        return false;
    }

    public void checkDeathCoordinates() {
        if (isPlayerDead()) {
            int positionX = mc.player.getPosition().getX();
            int positionY = mc.player.getPosition().getY();
            int positionZ = mc.player.getPosition().getZ();

            if (mc.player.deathTime < 1) {
                printDeathCoordinates(positionX, positionY, positionZ);
            }
        }
    }

    private boolean isPlayerDead() {
        return mc.player.getHealth() < 1.0f && mc.currentScreen instanceof DeathScreen;
    }

    private void printDeathCoordinates(int x, int y, int z) {
        String message = "Координаты смерти: " + TextFormatting.GRAY + "X: " + x + " Y: " + y + " Z: " + z + TextFormatting.RESET;
        ClientUtils.sendMessage(message);
    }
}
