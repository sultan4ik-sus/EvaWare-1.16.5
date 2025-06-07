package newcode.fun.module.impl.movement;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventTravel;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.move.MoveUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.minecraft.client.Minecraft.player;

@Annotation(name = "WaterSpeed", type = TypeList.Movement)
public class WaterSpeed extends Module {
    private final ModeSetting mode = new ModeSetting("Мод", "Обычный", "Обычный", "Дисейблер");
    public SliderSetting speed = new SliderSetting("Скорость", 0.5f, 0.1f, 3f, 0.1f).setVisible(() -> mode.is("Обычный"));
    public SliderSetting motionY = new SliderSetting("Скорость по Y", 0.0f, 0.0f, 0.1f, 0.01f).setVisible(() -> mode.is("Обычный"));
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public WaterSpeed() {
        addSettings(mode, speed, motionY);
    }

    @Override
    public boolean onEvent(Event event) {
        if (mode.is("Обычный")) {
            if (event instanceof EventTravel move) {
                if (player.collidedVertically || player.collidedHorizontally) {
                    return false;
                }
                if (player.isSwimming()) {
                    float speed = this.speed.getValue().floatValue() / 10F;
                    if (mc.gameSettings.keyBindJump.pressed) {
                        player.motion.y += motionY.getValue().floatValue();
                    }
                    if (mc.gameSettings.keyBindSneak.pressed) {
                        player.motion.y -= motionY.getValue().floatValue();
                    }

                    MoveUtil.setMotion(MoveUtil.getMotion());
                    move.speed += speed;
                }
            }
        } else {
            if (event instanceof EventTravel move) {
                if (player.collidedVertically || player.collidedHorizontally) {
                    return false;
                }

                if (player != null && player.isAlive() && player.isSwimming() && player.isInWater()) {
                    boolean isIceNearby = false;

                    // Check all blocks within a 2-block radius around the player
                    for (float x = -0.5f; x <= 0.5f; x++) {
                        for (float y = -2f; y <= 2f; y++) {
                            for (float z = -0.5f; z <= 0.5f; z++) {
                                BlockPos pos = player.getPosition().add(x, y, z);
                                BlockState blockState = player.getEntityWorld().getBlockState(pos);

                                if (blockState.getBlock() == Blocks.ICE || blockState.getBlock() == Blocks.FROSTED_ICE) {
                                    isIceNearby = true;
                                    break;
                                }
                            }
                            if (isIceNearby) break;
                        }
                        if (isIceNearby) break;
                    }

                    if (isIceNearby) {
                        player.setMotion(player.getMotion().x * 1.045, player.getMotion().y, player.getMotion().z * 1.045);
                    } else {
                        player.setMotion(player.getMotion().x * 1.012, player.getMotion().y, player.getMotion().z * 1.012);
                    }
                }
            }



        }
        return false;
    }
}