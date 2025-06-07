package newcode.fun.module.impl.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.anim.Animation;
import newcode.fun.utils.anim.impl.DecelerateAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Annotation(name = "LegitAura", type = TypeList.Combat)
public class LegitAura extends Module {
    public final MultiBoxSetting settings2;
    public final MultiBoxSetting settings;
    private long cpsLimit = 0;
    private final SliderSetting distanceSetting;
    private final SliderSetting speed;
    private final Animation alpha = new DecelerateAnimation(600, 255);
    private PlayerEntity currentTarget;
    private static final Minecraft mc = Minecraft.getInstance();

    private float lastYaw, lastPitch;

    public LegitAura() {
        // Инициализация settings2
        this.settings2 = new MultiBoxSetting("Элементы",
                new BooleanOption("Аим-Ассист", true),
                new BooleanOption("Тригер-Бот", true));

        // Инициализация settings с проверкой на null
        this.settings = new MultiBoxSetting("Triger Bot",
                new BooleanOption("Только критами", true),
                new BooleanOption("Умные криты", false),
                new BooleanOption("Не бить когда ешь", true),
                new BooleanOption("Динамический", false))
                .setVisible(() -> settings2 != null && settings2.get("Тригер-Бот"));

        // Инициализация других настроек
        this.distanceSetting = new SliderSetting("Дистанция", 3.0f, 2.5f, 5.5f, 0.1f)
                .setVisible(() -> settings2 != null && settings2.get("Аим-Ассист"));
        this.speed = new SliderSetting("Скорость", 15.0f, 1.0f, 80.0f, 1.0f)
                .setVisible(() -> settings2 != null && settings2.get("Аим-Ассист"));

        // Добавление всех настроек
        this.addSettings(settings2, distanceSetting, speed, settings);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (settings2 != null && settings2.get(1)) {
            if (event instanceof EventUpdate e) {
                if (settings != null && settings.get(2) && mc.player.isHandActive() && !mc.player.isBlocking()) return false;

                if (cpsLimit > System.currentTimeMillis()) {
                    return false;
                }

                if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
                    Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();

                    if (entity instanceof PlayerEntity) {
                        if (Manager.FRIEND_MANAGER.isFriend(entity.getName().getString())) {
                            return false;
                        }

                        if (whenFalling()) {
                            cpsLimit = System.currentTimeMillis() + 550;

                            mc.playerController.attackEntity(mc.player, entity);
                            mc.player.swingArm(Hand.MAIN_HAND);
                        }
                    }
                }
            }
        }

        if (settings2 != null && settings2.get(0)) {
            if (currentTarget != null) {
                if (mc.player.getDistance(currentTarget) <= distanceSetting.getValue().floatValue()) {
                    float[] targetRotations = calculateRotations(currentTarget);
                    mc.player.rotationYaw = smoothRotation(mc.player.rotationYaw, targetRotations[0], speed.getValue().floatValue() / 300.0f);
                    mc.player.rotationPitch = smoothRotation(mc.player.rotationPitch, targetRotations[1], speed.getValue().floatValue() / 350f);
                    lastYaw = mc.player.rotationYaw;
                    lastPitch = mc.player.rotationPitch;
                } else {
                    currentTarget = null;
                }
            } else {
                currentTarget = findClosestPlayer();
            }
        }
        return false;
    }

    private PlayerEntity findClosestPlayer() {
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity living) {
                targets.add(living);
            }
        }
        PlayerEntity closestPlayer = null;
        double closestDistanceSq = Double.MAX_VALUE;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || Manager.FRIEND_MANAGER.isFriend(player.getName().getString())) continue;
            double distanceSq = mc.player.getDistanceSq(player);
            if (distanceSq < closestDistanceSq && distanceSq <= distanceSetting.getValue().floatValue() * distanceSetting.getValue().floatValue()) {
                closestPlayer = player;
                closestDistanceSq = distanceSq;
            }
        }
        return closestPlayer;
    }

    public boolean whenFalling() {
        boolean critWater = mc.player.areEyesInFluid(FluidTags.WATER);

        final boolean reasonForCancelCritical = mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isOnLadder()
                || mc.player.isInWater() && critWater
                || mc.player.isRidingHorse()
                || mc.player.abilities.isFlying
                || mc.player.isElytraFlying();

        final boolean onSpace = settings != null && settings.get(1)
                && mc.player.isOnGround()
                && !mc.gameSettings.keyBindJump.isKeyDown();

        if (mc.player.getCooledAttackStrength(1.5F) < 0.92F)
            return false;

        if (!reasonForCancelCritical && settings != null && settings.get(0)) {
            return onSpace || !mc.player.isOnGround() && mc.player.fallDistance > 0.0F;
        }

        return true;
    }

    private float smoothRotation(float current, float target, float speed) {
        float delta = MathHelper.wrapDegrees(target - current);

        float dynamicSpeed = speed * (1 + Math.abs(delta) / 180.0f); // Чем больше разница, тем выше скорость
        dynamicSpeed = Math.min(dynamicSpeed, 30.0f); // Ограничиваем максимальную скорость

        float adjustment = delta * dynamicSpeed * 0.05f;
        return current + MathHelper.clamp(adjustment, -dynamicSpeed, dynamicSpeed);
    }

    private float[] calculateRotations(Entity entity) {
        double playerX = mc.player.getPosX();
        double playerY = mc.player.getPosY() + mc.player.getEyeHeight();
        double playerZ = mc.player.getPosZ();

        double entityX = entity.getPosX();
        double entityY = entity.getPosY() + entity.getEyeHeight();
        double entityZ = entity.getPosZ();

        double width = entity.getWidth() / 2.0;
        double height = entity.getHeight();

        double closestX = MathHelper.clamp(playerX, entityX - width, entityX + width);
        double closestY = MathHelper.clamp(playerY, entityY - height, entityY);
        double closestZ = MathHelper.clamp(playerZ, entityZ - width, entityZ + width);

        double deltaX = closestX - playerX;
        double deltaY = closestY - playerY;
        double deltaZ = closestZ - playerZ;

        double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) (MathHelper.atan2(deltaZ, deltaX) * (180 / (float) Math.PI) - 90);
        float pitch = (float) -(MathHelper.atan2(deltaY, dist) * (180 / (float) Math.PI));

        yaw += (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.5f; // Уменьшили разброс
        pitch += (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.3f; // Уменьшили разброс

        return new float[]{yaw, pitch};
    }
}