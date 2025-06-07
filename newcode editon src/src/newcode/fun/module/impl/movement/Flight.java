package newcode.fun.module.impl.movement;

import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.move.MoveUtil;

import java.util.concurrent.CopyOnWriteArrayList;

@Annotation(name = "Flight", type = TypeList.Movement)
public class Flight extends Module {
    private final ModeSetting flMode = new ModeSetting("Flight Mode",
            "Motion",
            "Motion", "Glide", "OnlyHit");

    private final SliderSetting motion = new SliderSetting("Скорость по XZ", 1F, 0F, 8F, 0.1F).setVisible(() -> !flMode.is("OnlyHit"));
    private final SliderSetting motionY = new SliderSetting("Скорость по Y", 1F, 0F, 8F, 0.1F).setVisible(() -> !flMode.is("OnlyHit"));

    public final BooleanOption elytraonly = new BooleanOption("Только в элитрах", true).setVisible(() -> flMode.is("OnlyHit"));

    public CopyOnWriteArrayList<IPacket<?>> packets = new CopyOnWriteArrayList<>();

    public Flight() {
        addSettings(flMode, elytraonly, motion, motionY);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (!flMode.is("OnlyHit")) {
            if (event instanceof EventUpdate) {
                handleFlyMode();
            }
        } else {
            if (elytraonly.get()) {
                if (mc.player.isElytraFlying()) {
                    if (event instanceof EventPacket) {
                        EventPacket e = (EventPacket) event;
                        if (e.getPacket() instanceof CUseEntityPacket) {
                            CUseEntityPacket use = (CUseEntityPacket) e.getPacket();
                            if (use.getAction() == CUseEntityPacket.Action.ATTACK) {
                                Entity entity = use.getEntityFromWorld(mc.world);
                                if (mc.world != null && entity != null) {
                                    mc.player.setVelocity(0, 0.022, 0);
                                    MoveUtil.setMotion(1.8);
                                }
                            }
                        }
                    }
                }
            } else {
                if (event instanceof EventPacket) {
                    EventPacket e = (EventPacket) event;
                    if (e.getPacket() instanceof CUseEntityPacket) {
                        CUseEntityPacket use = (CUseEntityPacket) e.getPacket();
                        if (use.getAction() == CUseEntityPacket.Action.ATTACK) {
                            Entity entity = use.getEntityFromWorld(mc.world);
                            if (mc.world != null && entity != null) {
                                mc.player.setVelocity(0, 0.022, 0);
                                MoveUtil.setMotion(1.8);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    private void handleFlyMode() {
        switch (flMode.get()) {
            case "Motion" -> handleMotionFly();
            case "Glide" -> handleGlideFly();
        }
    }

    private void handleMotionFly() {
        final float motionY = this.motionY.getValue().floatValue();
        final float speed = this.motion.getValue().floatValue();

        mc.player.motion.y = 0;

        if (mc.gameSettings.keyBindJump.pressed) {
            mc.player.motion.y = motionY;
        } else if (mc.player.isSneaking()) {
            mc.player.motion.y = -motionY;
        }

        MoveUtil.setMotion(speed);
    }


    private void handleGlideFly() {
        mc.player.setVelocity(0, 0.023, 0);
        MoveUtil.setMotion(motion.getValue().floatValue());
    }

    @Override
    protected void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }
}
