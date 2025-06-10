package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventPacket;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.SliderSetting;
import net.minecraft.item.BowItem;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.MathHelper;

@ModuleRegister(name = "AWP", category = Category.Combat)
public class AWP extends Module {

    final SliderSetting power = new SliderSetting("Сила", 40, 1, 100, 1);

    public AWP() {
        addSettings(power);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CPlayerDiggingPacket p && e.isSend() && isBowInHand()) {
            if (p.getAction() == CPlayerDiggingPacket.Action.RELEASE_USE_ITEM) {
                mc.player.connection.preSendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                for (int i = 0; i < power.getValue(); i++) {
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() - 0.000000001, mc.player.getPosZ(), true));
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 0.000000001, mc.player.getPosZ(), false));
                }
                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, (float) MathHelper.clamp(mc.player.rotationPitch * 1.0001, -89.9, 89.9), false));
            }
        }
    }

    private boolean isBowInHand() {
        if (mc.player.getHeldItemMainhand().getItem() instanceof BowItem) {
            return true;
        }
        if (mc.player.getHeldItemOffhand().getItem() instanceof BowItem) {
            return true;
        }
        return false;
    }
}
