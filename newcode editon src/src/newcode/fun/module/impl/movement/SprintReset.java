package newcode.fun.module.impl.movement;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.ModeSetting;

@Annotation(name = "SprintReset", type = TypeList.Movement)
public class SprintReset extends Module {
    public final ModeSetting mode = new ModeSetting("Выбор режимов", "WTap", "WTap", "STap", "ShiftTap", "Blatant");

    public SprintReset() {
        this.addSettings(mode);
    }

    @Override
    public boolean onEvent(Event event) {
        Event e;
        CUseEntityPacket use;
        EventPacket e2;
        IPacket var4;

        if (event instanceof EventPacket && (var4 = (e2 = (EventPacket)event).getPacket()) instanceof CUseEntityPacket && (use = (CUseEntityPacket)var4).getAction() == CUseEntityPacket.Action.ATTACK) {
            handleSprintResetCompletion();
        }
        return false;
    }

    private void handleSprintResetCompletion() {
        switch (mode.get()) {
            case "WTap":
                activateForwardKey();
                break;
            case "STap":
                deactivateBackKey();
                break;
            case "ShiftTap":
                deactivateSneakKey();
                break;
            case "Blatant":
                mc.player.setSprinting(true);
        }
    }

    private void activateForwardKey() {
        mc.gameSettings.keyBindForward.setPressed(true);
        mc.gameSettings.keyBindSprint.setPressed(true);
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                mc.gameSettings.keyBindForward.setPressed(false);
                mc.gameSettings.keyBindSprint.setPressed(false);
            }
        }, 60);
    }

    private void deactivateBackKey() {
        mc.gameSettings.keyBindBack.setPressed(true);
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                mc.gameSettings.keyBindBack.setPressed(false);
            }
        }, 60);
    }

    private void deactivateSneakKey() {
        mc.gameSettings.keyBindSneak.setPressed(true);
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                mc.gameSettings.keyBindSneak.setPressed(false);
            }
        }, 60);
    }
}
