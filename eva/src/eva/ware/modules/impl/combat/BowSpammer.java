package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.SliderSetting;
import net.minecraft.item.BowItem;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@ModuleRegister(name = "BowSpammer", category = Category.Combat)
public class BowSpammer extends Module {

    private final SliderSetting delay = new SliderSetting("Задержка", 1.5f, 1, 5, 0.1f);

    public BowSpammer() {
        addSettings(delay);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof BowItem && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= delay.getValue()) {
            mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, new BlockPos(0, 0, 0), mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.stopActiveHand();
        }
    }

}
