package newcode.fun.utils.world;

import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import newcode.fun.utils.IMinecraft;

public class PotionUtils implements IMinecraft {
    public static boolean isChangingItem;
    private boolean isItemChangeRequested;
    private int previousSlot = -1;

    public void changeItemSlot(boolean resetAfter) {
        if (this.isItemChangeRequested && this.previousSlot != -1) {
            isChangingItem = true;
            mc.player.inventory.currentItem = this.previousSlot;
            if (resetAfter) {
                this.isItemChangeRequested = false;
                this.previousSlot = -1;
                isChangingItem = false;
            }
        }
    }

    public void setPreviousSlot(int slot) {
        this.previousSlot = slot;
    }


    public static void useItem(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.gameRenderer.itemRenderer.resetEquippedProgress(hand);
    }

}