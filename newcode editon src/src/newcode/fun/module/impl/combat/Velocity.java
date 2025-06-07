package newcode.fun.module.impl.combat;

import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.ModeSetting;

@Annotation(name = "Velocity", type = TypeList.Combat)
public class Velocity extends Module {

    private final ModeSetting mode = new ModeSetting("Моды", "Cancel", "Cancel", "Grim", "Grim Air");

    public Velocity() {
        addSettings(mode);
    }

    private int toSkip;
    private int await;

    @Override
    public boolean onEvent(final Event event) {
        if (mc.player == null || mc.world == null) return false;

        if (event instanceof EventPacket e && e.isReceivePacket()) {
            switch (mode.get()) {
                case "Cancel" -> {
                    if (e.getPacket() instanceof SEntityVelocityPacket p) {
                        if (p.getEntityID() != mc.player.getEntityId()) return false;

                        e.setCancel(true);
                    }
                }

                case "Grim" -> {
                    if (e.getPacket() instanceof SEntityVelocityPacket p) {
                        if (p.getEntityID() != mc.player.getEntityId() || toSkip < 0) return false;

                        toSkip = 8;
                        event.setCancel(true);
                    }

                    if (e.getPacket() instanceof SConfirmTransactionPacket) {
                        if (toSkip < 0) toSkip++;

                        else if (toSkip > 1) {
                            toSkip--;
                            event.setCancel(true);
                        }
                    }

                    if (e.getPacket() instanceof SPlayerPositionLookPacket) toSkip = -8;
                }
                case "Grim Air" -> {
                    if (Manager.FUNCTION_MANAGER.freeCam.state || Manager.FUNCTION_MANAGER.flightFunction.state || mc.player.isOnGround() || mc.player.isElytraFlying() || mc.player.isInWater() || mc.player.isInLava()) {
                        return false;
                    }

                    if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
                        mc.player.func_242277_a(new Vector3d(p.getX(), p.getY(), p.getZ()));
                        mc.player.setRawPosition(p.getX(), p.getY(), p.getZ());
                        return false;
                    }
                    if (!mc.player.isElytraFlying()) {
                        if (mc.player.fallDistance < 2F) {
                            if (e.getPacket() instanceof SConfirmTransactionPacket && mc.player.fallDistance > 0.7) {
                                e.cancel();
                            }

                            if (e.getPacket() instanceof SEntityVelocityPacket && mc.player.fallDistance > 0.7 && ((SEntityVelocityPacket) e.getPacket()).getEntityID() == mc.player.getEntityId()) {
                                e.cancel();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void reset() {
        toSkip = 0;
        await = 0;
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        reset();
    }
}