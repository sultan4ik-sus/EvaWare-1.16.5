package newcode.fun.utils;

import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import newcode.fun.utils.world.InventoryUtils;

public class JoinerUtils implements IMinecraft {

    public static void selectCompass() {
        int slot = InventoryUtils.getHotBarSlot(Items.COMPASS);

        if (slot == -1) {
            return;
        }

        mc.player.inventory.currentItem = slot;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
    }
}
