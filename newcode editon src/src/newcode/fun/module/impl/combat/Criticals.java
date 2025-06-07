package newcode.fun.module.impl.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.ModeSetting;

@Annotation(name = "Criticals", type = TypeList.Combat, desc = "Наносит критический удар с места")

public class Criticals extends Module {
    public static boolean cancelCrit;

    public final ModeSetting mode = new ModeSetting("Обход", "Packet", "Packet", "Matrix");

    public Criticals() {
        addSettings(mode);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventPacket e) {
            if (e.isSend()) {
                if (e.getPacket() instanceof CUseEntityPacket packet) {
                    if (packet.getAction() == CUseEntityPacket.Action.ATTACK) {
                        Entity entity = packet.getEntityFromWorld(mc.world);
                        if (entity == null || entity instanceof EnderCrystalEntity || cancelCrit)
                            return false;
                        sendCrit();
                    }
                }
            }
        }
        return false;
    }

        public void sendCrit () {
            if (mc.player == null || mc.world == null && !isState())
                return;

            if (mode.is("Mini-Jump")) {
                miniJump();
            }

            if (mode.is("Packet")) {
                packetCrit();
            }

            if (mode.is("Matrix Packet 14")) {
                matrixPacketCrit();
            }

            if ((mc.player.isOnGround() || mc.player.abilities.isFlying || mode.is("Grim") && !mc.player.isInLava() && !mc.player.isInWater())) {
                if (mode.is("Grim")) {
                    if (!mc.player.isOnGround())
                        critPacket(-0.000001, false);
                }
                if (mode.is("Matrix")) {
                    critPacket(1.0E-6, false);
                    critPacket(0., false);
                }
                if (mode.is("FunTime")) {
                    if (mc.player.isOnGround()) critPacket(1e-8, false);
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-9, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-9, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                }
            }
        }

        private void miniJump () {
            if (mc.player != null && mc.player.isOnGround()) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 0.05, mc.player.getPosZ(), false));
            }
        }

        private void packetCrit () {
            double x = mc.player.getPosX();
            double y = mc.player.getPosY();
            double z = mc.player.getPosZ();
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.11, z, false));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.1100013579, z, false));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.0000013579, z, false));
        }

        private void matrixPacketCrit () {
            double x = mc.player.getPosX();
            double y = mc.player.getPosY();
            double z = mc.player.getPosZ();
            double yDelta = 1.0E-12;
            mc.player.fallDistance = (float) yDelta;
            mc.player.motionY = yDelta;
            mc.player.isCollidedVertically = true;
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + yDelta, z, false));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + yDelta * 2, z, false));
        }

        private void critPacket (double yDelta, boolean full) {
            if (full)
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), false));
            else
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
        }
    }
