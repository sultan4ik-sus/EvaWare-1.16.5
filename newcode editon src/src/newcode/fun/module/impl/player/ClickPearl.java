package newcode.fun.module.impl.player;

import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import newcode.fun.control.Manager;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.game.EventKey;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BindSetting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.utils.world.InventoryUtils;

@Annotation(name = "ClickPearl", type = TypeList.Player)
public class ClickPearl extends Module {
    private BindSetting clickKey = new BindSetting("Кнопка", -98);
    public ClickPearl() {
        addSettings(clickKey);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (event instanceof EventKey e) {
            if (e.key == clickKey.getKey()) {
                handleMouseTickEvent();
            }
        }
        return false;
    }

    private void handleMouseTickEvent() {
        if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL) && InventoryUtils.getItemSlot(Items.ENDER_PEARL) != -1) {
            InventoryUtils.inventorySwapClick(Items.ENDER_PEARL, false);

            sendPlayerRotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround());
            useItem(Hand.MAIN_HAND);
        }
    }

    private void sendPlayerRotationPacket(float yaw, float pitch, boolean onGround) {
        if (Manager.FUNCTION_MANAGER.auraFunction.target != null) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(yaw, pitch, onGround));
        }
    }

    private void useItem(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.player.swingArm(hand);
    }
}
