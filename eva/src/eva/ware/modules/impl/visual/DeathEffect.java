package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.*;
import eva.ware.manager.Theme;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "DeathEffect", category = Category.Visual)
public class DeathEffect extends Module {
    private Animation animate = new Animation();
    private Animation animation = new Animation();
    private boolean useAnimation;

    private final CheckBoxSetting onlyPlayer = new CheckBoxSetting("Только на игроков", true);

    LivingEntity target;
    long time;
    public TimerUtility timerUtility = new TimerUtility();

    private float yaw, pitch;

    private final List<Vector3d> position = new ArrayList<>();

    private int current;

    private Vector3d setPosition;

    public DeathEffect() {
        addSettings(onlyPlayer);
    }

    @Subscribe
    public void onPacket(AttackEvent e) {
        if (mc.player == null || mc.world == null)
            return;

        // НЕ БЕЙТЕ X5
        if (onlyPlayer.getValue()) {
            if (e.entity instanceof PlayerEntity)
                target = (LivingEntity) e.entity;
        } else {
            target = (LivingEntity) e.entity;
        }
        time = System.currentTimeMillis();

    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null)
            return;

        if (e.getPacket() instanceof SDestroyEntitiesPacket p) {
            for (int ids : p.getEntityIDs()) {
                if (target != null) {
                    if (ids == mc.player.getEntityId())
                        continue;

                    if (time + 400 >= System.currentTimeMillis() && target.getEntityId() == ids) {
                        if (((LivingEntity) mc.world.getEntityByID(ids)).getHealth() < 5) {
                            onKill(target);
                            target = null;
                        }
                    }
                }
            }
        }

    }

    public float back;

    @Subscribe
    public void onUpdate(EventMotion e) {
        if (mc.player == null || mc.world == null)
            return;

        if (useAnimation) {
            if (mc.player.ticksExisted % 5 == 0)
                current++;
            Vector3d player = new Vector3d(
                    MathUtility.interpolate(mc.player.getPosX(), mc.player.lastTickPosX, mc.getRenderPartialTicks()),
                    MathUtility.interpolate(mc.player.getPosY(), mc.player.lastTickPosY, mc.getRenderPartialTicks()),
                    MathUtility.interpolate(mc.player.getPosZ(), mc.player.lastTickPosZ, mc.getRenderPartialTicks()))
                    .add(0, mc.player.getEyeHeight(), 0);

            position.add(player);
        }

        if (target != null) {
            if (time + 1000 >= System.currentTimeMillis() && target.getHealth() <= 0f) {
                onKill(target);
                target = null;
            }

        }

        if (timerUtility.isReached(500)) {
            animate = animate.animate(0, 1f, Easings.CIRC_OUT);
            animation = animation.animate(0, 1f, Easings.EXPO_OUT);
        }
        if (timerUtility.isReached(1000)) {
            useAnimation = false;
            last = null;
        }
    }

    public Vector2f last;

    @Subscribe
    public void onCameraController(EventCamera e) {
        if (useAnimation) {
            mc.getRenderManager().info.setDirection(
                    (float) (yaw + (6 * animate.getValue())),
                    (float) (pitch + (6 * animate.getValue())));

            back = MathUtility.fast(back, timerUtility.isReached(700) ? 1 : 0, 10);
            Vector3d player = new Vector3d(
                    MathUtility.interpolate(mc.player.getPosX(), mc.player.lastTickPosX, mc.getRenderPartialTicks()),
                    MathUtility.interpolate(mc.player.getPosY(), mc.player.lastTickPosY, mc.getRenderPartialTicks()),
                    MathUtility.interpolate(mc.player.getPosZ(), mc.player.lastTickPosZ, mc.getRenderPartialTicks()))
                    .add(0, mc.player.getEyeHeight(), 0);

            if (setPosition != null) {
                mc.getRenderManager().info.setDirection(
                        (float) MathUtility.interpolate((float) (yaw + (6 * animate.getValue())), mc.player.getYaw(e.partialTicks), 1 - back),
                        (float) MathUtility.interpolate((float) (pitch + (6 * animate.getValue())), mc.player.getPitch(e.partialTicks), 1 - back));
                mc.getRenderManager().info.setPosition(MathUtility.interpolate(setPosition, player, 1 - back));
            }
            mc.getRenderManager().info.moveForward(2f * animate.getValue());
        }

    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.player == null || mc.world == null || e.getType() != EventRender2D.Type.POST) {
            return;
        }
        animate.update();
        animation.update();
        float animateImage = (float) (200 - 200 * animation.getValue());
        if (useAnimation && setPosition != null && position.size() > 1) {
            setPosition = MathUtility.fast(setPosition, position.get(current), 1);
            RenderUtility.drawWhite((float) animate.getValue());
            RenderUtility.drawImage(new ResourceLocation("eva/images/modules/frag.png"), 0 - animateImage, 0 - animateImage, window.getScaledWidth() + animateImage * 2, window.getScaledHeight() + animateImage * 2, ColorUtility.reAlphaInt(Theme.mainRectColor, (int) (140 * animation.getValue())));
        }
    }

    public void onKill(LivingEntity entity) {
        position.clear();
        current = 0;
        animate = animate.animate(1, 1f, Easings.CIRC_OUT);
        animation = animation.animate(1, 1f, Easings.EXPO_OUT);
        useAnimation = true;
        timerUtility.reset();
        Vector3d player = new Vector3d(
                MathUtility.interpolate(mc.player.getPosX(), mc.player.lastTickPosX, mc.getRenderPartialTicks()),
                MathUtility.interpolate(mc.player.getPosY(), mc.player.lastTickPosY, mc.getRenderPartialTicks()),
                MathUtility.interpolate(mc.player.getPosZ(), mc.player.lastTickPosZ, mc.getRenderPartialTicks()))
                .add(0, mc.player.getEyeHeight(), 0);

        setPosition = player;
        SoundPlayer.playSound("frag.wav");
        yaw = mc.player.getYaw(mc.getRenderPartialTicks());
        pitch = mc.player.getPitch(mc.getRenderPartialTicks());
    }
}
