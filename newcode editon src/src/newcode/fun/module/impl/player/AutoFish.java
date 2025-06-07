package newcode.fun.module.impl.player;

import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.Hand;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(name = "AutoFish", type = TypeList.Player)
public class AutoFish extends Module {

    private final TimerUtil delay = new TimerUtil();
    private boolean isHooked = false;
    private boolean needToHook = false;

    @Override
    public boolean onEvent(final Event event) {
        if (mc.player == null || mc.world == null) return false;

        if (event instanceof EventPacket e) {
            if (e.getPacket() instanceof SPlaySoundEffectPacket p) {
                if (p.getSound().getName().getPath().equals("entity.fishing_bobber.splash")) {
                    isHooked = true;
                    delay.reset();
                }
            }
        }

        if (event instanceof EventUpdate e) {
            if (delay.hasTimeElapsed(600) && isHooked) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                isHooked = false;
                needToHook = true;
                delay.reset();
            }

            if (delay.hasTimeElapsed(300) && needToHook) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                needToHook = false;
                delay.reset();
            }
        }
        return false;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        delay.reset();
        isHooked = false;
        needToHook = false;
    }
}
