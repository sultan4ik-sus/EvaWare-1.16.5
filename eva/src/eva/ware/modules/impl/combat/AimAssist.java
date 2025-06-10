package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.AttackEvent;
import eva.ware.events.EventUpdate;
import eva.ware.manager.friend.FriendManager;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.TimerUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

@ModuleRegister(name = "AimAssist", category = Category.Combat)
public class AimAssist extends Module {
    private final SliderSetting stopSetting = new SliderSetting("Тайм-аут", 1, 1, 5, 1);
    private final SliderSetting distanceSetting = new SliderSetting("Дистанция", 3.0F, 2.0F, 3.5F, 0.1F);
    private final SliderSetting yawSpeedSetting = new SliderSetting("Yaw Скорость", 30.0F, 0.0F, 200.0F, 1.0F);
    private final SliderSetting pitchSpeedSetting = new SliderSetting("Pitch Скорость", 50.0F, 0.0F, 100.0F, 1.0F);
    private final CheckBoxSetting targetInvisibleSetting = new CheckBoxSetting("Таргетить инвизок", false);
    private LivingEntity currentTarget;

    private TimerUtility wait = new TimerUtility();
    private double anim;

    public AimAssist() {
        addSettings(stopSetting, distanceSetting, yawSpeedSetting, pitchSpeedSetting, targetInvisibleSetting);
    }

    @Subscribe
    public void onAttack(AttackEvent e) {
        if (!FriendManager.isFriend(e.entity.getName().getString())) {
            currentTarget = (LivingEntity) e.entity;
            wait.reset();
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (wait.isReached(stopSetting.getValue().longValue() * 1000L) && currentTarget != null || currentTarget != null && !currentTarget.isAlive()) {
            currentTarget = null;
        }

        if (currentTarget != null) {
            if (mc.player.getDistance(currentTarget) <= distanceSetting.getValue()) {
                double[] rotations = calculateRotations(currentTarget);
                mc.player.rotationYaw = (float) smoothRotation(mc.player.rotationYaw, rotations[0], yawSpeedSetting.getValue() / 200.0F);
                mc.player.rotationPitch = (float) smoothRotation(mc.player.rotationPitch, rotations[1], pitchSpeedSetting.getValue() / 200.0F);
            } else {
                currentTarget = null;
            }
        } else {
            currentTarget = findClosestPlayer();
        }
    }

    private PlayerEntity findClosestPlayer() {
        PlayerEntity closestPlayer = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && (!player.isInvisible() || targetInvisibleSetting.getValue())) {
                double distanceSq = mc.player.getDistanceSq(player);
                if (distanceSq < closestDistanceSq && distanceSq <= Math.pow(distanceSetting.getValue(), 2)) {
                    closestPlayer = player;
                    closestDistanceSq = distanceSq;
                }
            }
        }
        return closestPlayer;
    }

    private double[] calculateRotations(Entity target) {
        double deltaX = target.getPosX() - mc.player.getPosX();
        double deltaY = target.getPosY() + target.getEyeHeight() - (mc.player.getPosY() + mc.player.getEyeHeight());
        double deltaZ = target.getPosZ() - mc.player.getPosZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(deltaY, distance) * 180.0 / Math.PI));

        return new double[]{yaw, pitch};
    }

    private double easeInOut(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    private double smoothRotation(double current, double target, double speed) {
        float delta = (float) MathHelper.wrapDegrees(target - current);

        if (Math.abs(delta) < 0.001) {
            return target;
        }

        double t = Math.min(speed, 1.0);
        t = easeInOut(t);

        double step = t * delta;
        return current + step;
    }
}
