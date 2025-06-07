package newcode.fun.module.impl.combat;

import net.minecraft.item.BowItem;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(name = "FastBow",
        type = TypeList.Combat, desc = "Убирает задержку выстрела из лука"
)
public class FastBow extends Module {
    private boolean restart = false;
    private final SliderSetting tickBow = new SliderSetting("Скорость", 2.0F, 2.0F, 20.0F, 0.5F);
    private final TimerUtil timerUtil = new TimerUtil();

    public FastBow() {
        this.addSettings(new Setting[]{this.tickBow});
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate eventUpdate) {
            this.handleUpdateEvent(eventUpdate);
        }
        return false;
    }

    private void handleUpdateEvent(EventUpdate eventUpdate) {
        boolean mainHandBow = mc.player.getHeldItemMainhand().getItem() instanceof BowItem;
        boolean offHandBow = mc.player.getHeldItemOffhand().getItem() instanceof BowItem;

        if ((mainHandBow || offHandBow) && mc.player.isHandActive() && (float)mc.player.getItemInUseMaxCount() > this.tickBow.getValue().floatValue()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        if ((mainHandBow || offHandBow) && !mc.player.isHandActive() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            if (mainHandBow) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            } else if (offHandBow) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            }
        }
    }
}