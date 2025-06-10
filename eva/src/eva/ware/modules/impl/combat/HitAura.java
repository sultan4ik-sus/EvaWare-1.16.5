package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.manager.friend.FriendManager;
import eva.ware.events.EventInput;
import eva.ware.events.EventMotion;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.ui.clickgui.ClickGuiScreen;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.SensUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@ModuleRegister(name = "HitAura", category = Category.Combat)
public class HitAura extends Module {
    @Getter
    final ModeSetting type = new ModeSetting("Тип", "Плавная", "Плавная", "Резкая", "HolyWorld");
    final ModeSetting visualType = new ModeSetting("Визуальная наводка", "Обычная", "Обычная", "Спин-бот", "Никакая");
    final SliderSetting attackRange = new SliderSetting("Дистанция аттаки", 3f, 2.5f, 6f, 0.05f);
    final SliderSetting elytraRange = new SliderSetting("Дистанция на элитре", 6f, 0f, 16f, 0.05f);
    final SliderSetting preRange = new SliderSetting("Дистанция наводки", 0.3f, 0f, 3f, 0.05f).visibleIf(() -> !type.is("Резкая"));
    final SliderSetting tick = new SliderSetting("Тики", 2f, 1f, 10f, 1f).visibleIf(() -> type.is("Резкая"));

    final ModeSetting clickType = new ModeSetting("Режим кликов", "1.9", "1.8", "1.9");
    final SliderSetting minCPS = new SliderSetting("Мин. CPS", 7f, 1f, 10f, 1f).visibleIf(() -> !clickType.is("1.9"));
    final SliderSetting maxCPS = new SliderSetting("Макс. CPS", 10f, 1, 20f, 1f).visibleIf(() -> !clickType.is("1.9"));

    final ModeListSetting targets = new ModeListSetting("Таргеты",
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Голые", true),
            new CheckBoxSetting("Мобы", false),
            new CheckBoxSetting("Животные", false),
            new CheckBoxSetting("Друзья", false),
            new CheckBoxSetting("Голые невидимки", true),
            new CheckBoxSetting("Невидимки", true)
    );

    final ModeListSetting consider = new ModeListSetting("Учитывать",
            new CheckBoxSetting("Хп", true),
            new CheckBoxSetting("Броню", true),
            new CheckBoxSetting("Дистанцию", true),
            new CheckBoxSetting("Баффы", true)
    );

    @Getter
    final ModeListSetting options = new ModeListSetting("Опции",
            new CheckBoxSetting("Только криты", true),
            new CheckBoxSetting("Ломать щит", true),
            new CheckBoxSetting("Отжимать щит", false),
            new CheckBoxSetting("Синхронизировать с TPS", false),
            new CheckBoxSetting("Фокусировать одну цель", true),
            new CheckBoxSetting("Коррекция движения", true),
            new CheckBoxSetting("Оптимальная дистанция", false),
            new CheckBoxSetting("Резольвер", true)
    );

    @Getter
    final ModeListSetting moreOptions = new ModeListSetting("Дополнительное",
            new CheckBoxSetting("Проверка луча", true),
            new CheckBoxSetting("Перелетать противника", true),
            new CheckBoxSetting("Бить через стены", true),
            new CheckBoxSetting("Не бить если кушаешь", false),
            new CheckBoxSetting("Не бить если в гуи", false),
            new CheckBoxSetting("Рандомизация", true)
    );
    final SliderSetting elytraForward = new SliderSetting("Значение перелета", 75.0F, 1.0F, 100.0F, 1.0F).visibleIf(() -> moreOptions.is("Перелетать противника").getValue());
    public CheckBoxSetting smartCrits = new CheckBoxSetting("Умные криты", false).visibleIf(() -> (options.is("Только криты").getValue()));
    final ModeSetting correctionType = new ModeSetting("Тип коррекции", "Незаметный", "Незаметный", "Сфокусированный").visibleIf(() -> options.is("Коррекция движения").getValue());
    final ModeSetting critType = new ModeSetting("Крит хелпер", "None", "None", "Matrix", "NCP", "NCP+", "Grim");
    @Getter
    private final TimerUtility timerUtility = new TimerUtility();
    public Vector2f rotate = new Vector2f(0, 0);
    public Vector2f visualRotateVector = new Vector2f(0, 0);
    @Getter
    @Setter
    private LivingEntity target;
    private Entity selected;

    int ticks = 0;
    boolean isRotated = false, isAttacking = false, crystalAuraRule = true, elytraTargetRule;
    float lastYaw, lastPitch;
    boolean isReversing = false;
    private float rotationSpeed;
    private float rotationAngle;
    final PotionThrower autoPotion;

    float aimDistance() {
        return (!type.is("Резкая") ? preRange.getValue() : 0);
    }

    float maxRange() {
        return attackDistance() + (mc.player.isElytraFlying() ? elytraRange.getValue() : 0) + aimDistance();
    }

    public HitAura(PotionThrower autoPotion) {
        this.autoPotion = autoPotion;
        addSettings(type, visualType, attackRange, preRange, elytraRange, tick, clickType, minCPS, maxCPS, targets, consider, options, moreOptions, elytraForward, smartCrits, correctionType, critType);
    }

    // TODO: Class: PlayerRenderer#200 reallyworld hp bypass

    @Subscribe
    public void onInput(EventInput eventInput) {
        if (options.is("Коррекция движения").getValue() && correctionType.is("Незаметный") && crystalAuraRule) {
            MoveUtility.fixMovement(eventInput, rotate.x);
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (options.is("Фокусировать одну цель").getValue() && (target == null || !isValid(target)) || !options.is("Фокусировать одну цель").getValue()) {
            updateTarget();
        }

        if (options.is("Резольвер").getValue()) {
            resolvePlayers();
            releaseResolver();
        }

        if (target != null && !(autoPotion.isEnabled() && autoPotion.isActive())) {
            isRotated = false;

            visualRotationUpdate();

            if (shouldPlayerFalling() && timerUtility.hasTimeElapsed() && crystalAuraRule) {
                ticks = tick.getValue().intValue();
                forceAttack();
            }

            if (!mc.player.isElytraFlying()) {
                if (type.is("Резкая")) {
                    if (ticks > 0) {
                        setRotate();
                        ticks--;
                    } else {
                        reset();
                    }
                } else {
                    if (!isRotated) {
                        setRotate();
                    }
                }
            } else {
                if (!isRotated) {
                    setRotate();
                }
            }

        } else {
            timerUtility.setLastMS(0);
            reset();
        }

        if (target != null && isRotated && !mc.player.isElytraFlying() && mc.player.getDistanceEyePos(target) <= attackDistance()) {
            critHelper();
        }
    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (target == null || autoPotion.isEnabled() && autoPotion.isActive() || crystalAuraRule) return;

        float yaw = visualRotateVector.x;
        float pitch = visualRotateVector.y;

        e.setYaw(rotate.x);
        e.setPitch(rotate.y);
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = PlayerUtility.calculateCorrectYawOffset(yaw);
        mc.player.rotationPitchHead = pitch;
    }

    public void setRotate() {
        elytraTargetRule = mc.player.isElytraFlying() && target.isElytraFlying();

        if (type.is("Резкая")) {
            updateSpinRotation(2.1474836E9F);
        } else {
            updateSpinRotation(9999.0F);
        }
    }

    public float attackDistance() {
        if (options.is("Оптимальная дистанция").getValue() && !Evaware.getInst().getModuleManager().getTPInfluence().isEnabled()) {
            if (!mc.player.isSwimming())
                return (float) (3.6 + moreAttackDistanceOnElytra);
            else
                return (float) (3.0 + moreAttackDistanceOnElytra);
        }

        if (Evaware.getInst().getModuleManager().getTPInfluence().isEnabled() && !Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) {
            return Evaware.getInst().getModuleManager().getTPInfluence().range.getValue();
        }

        return (float) (attackRange.getValue() + moreAttackDistanceOnElytra);
    }

    public void resolvePlayers() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof RemoteClientPlayerEntity) {
                ((RemoteClientPlayerEntity) player).resolve();
            }
        }
    }

    public void releaseResolver() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof RemoteClientPlayerEntity) {
                ((RemoteClientPlayerEntity) player).releaseResolver();
            }
        }
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!this.isValid(living)) continue;

            targets.add(living);
        }

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (targets.size() == 1) {
            target = targets.get(0);
            return;
        }

        targets.sort(Comparator.comparingDouble(entity -> MathUtility.entity(entity, consider.is("Хп").getValue(), consider.is("Броню").getValue(), consider.is("Дистанцию").getValue(), maxRange(), consider.is("Баффы").getValue())));
        target = targets.get(0);
    }

    double moreAttackDistanceOnElytra = 0;

    private void updateSpinRotation(float rotationYawSpeed) {
        Vector3d vec;
        float yawToTarget;
        float pitchToTarget;
        float yawDelta;
        float pitchDelta;
        int roundedYaw;
        float yaw;
        float pitch;
        float gcd;
        float scaleFactor = moreOptions.is("Перелетать противника").getValue() ? (PlayerUtility.getEntityBPS(target, false) > 8 ? elytraForward.getValue() / 20 : 1) : 1;
        if (elytraTargetRule) {
            vec = target.getPositionVec().add(0.0, MathHelper.clamp(target.getPosY() - target.getHeight(), 0.0, (target.getHeight() / 2.0F)), 0.0).subtract(mc.player.getEyePosition(1.0F)).add(target.getMotion().mul(elytraTargetRule ? scaleFactor : 1.0, mc.player.isElytraFlying() && target.isElytraFlying() ? scaleFactor : 1.0, mc.player.isElytraFlying() && target.isElytraFlying() ? scaleFactor : 1.0));
            yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
            pitchToTarget = (float) -Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z)));
            yawDelta = MathHelper.wrapDegrees(yawToTarget - rotate.x);
            pitchDelta = MathHelper.wrapDegrees(pitchToTarget - rotate.y);
            switch (type.getValue()) {
                case "Плавная" -> {
                    yaw = Math.min(Math.max(Math.abs(yawDelta), 1.0F), rotationYawSpeed);
                    pitch = Math.max(Math.abs(pitchDelta), 1.0F);
                    gcd = rotate.x + (yawDelta > 0.0F ? yaw : -yaw) + SensUtility.getSensitivity((float) (Math.cos(((double) System.currentTimeMillis() / 50L)) * 7.0));
                    pitch = MathHelper.clamp(rotate.y + (pitchDelta > 0.0F ? pitch : -pitch), -89.0F, 89.0F) + SensUtility.getSensitivity((float) (Math.cos(((double) System.currentTimeMillis() / 50L)) * 7.0));
                    gcd -= (gcd - rotate.x) % SensUtility.getGCDValue();
                    pitch -= (pitch - rotate.y) % SensUtility.getGCDValue();
                    rotate = new Vector2f(gcd, pitch);
                    this.lastYaw = yaw;
                    this.lastPitch = pitch;
                }
                
                case "Резкая" -> {
                    yaw = rotate.x + yawDelta;
                    pitch = MathHelper.clamp(rotate.y + pitchDelta, -90.0F, 90.0F);
                    gcd = SensUtility.getGCDValue();
                    yaw -= (yaw - rotate.x) % gcd;
                    pitch -= (pitch - rotate.y) % gcd;
                    rotate = new Vector2f(yaw, pitch);
                }
                
                case "HolyWorld" -> {
                    float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0.0001f), 52f);
                    float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0.0001f), 22f);

                    float randomYawFactor = (float) (Math.random() * 1.5 - 1.5);
                    float randomPitchFactor = (float) (Math.random() * 1.5 - 1.0);
                    float randomThreshold = (float) (Math.random() * 1.5);
                    float randomAddition = (float) (Math.random() * 2.5 + 1.5);

                    if (selected != target) {
                        clampedPitch = Math.max(Math.abs(pitchDelta), 1.0f);
                    } else {
                        clampedPitch /= 3f;
                    }

                    if (Math.abs(clampedYaw - this.lastYaw) <= randomThreshold) {
                        clampedYaw = this.lastYaw + randomAddition;
                    }

                    clampedYaw += randomYawFactor;
                    clampedPitch += randomPitchFactor;

                    yaw = rotate.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                    pitch = clamp(rotate.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -90.0F, 90.0F);
                    gcd = SensUtility.getGCDValue();
                    
                    yaw -= (yaw - rotate.x) % gcd;
                    pitch -= (pitch - rotate.y) % gcd;

                    rotate = new Vector2f(yaw, pitch);

                    lastYaw = clampedYaw;
                    lastPitch = clampedPitch;
                }
            }
        } else {
            vec = target.getPositionVec().add(0.0, MathHelper.clamp(target.getPosY() - target.getHeight(), 0.0, (target.getHeight() / 2.0F)), 0.0).subtract(mc.player.getEyePosition(1.0F));
            yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
            pitchToTarget = (float) -Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z)));
            yawDelta = MathHelper.wrapDegrees(yawToTarget - rotate.x);
            pitchDelta = MathHelper.wrapDegrees(pitchToTarget - rotate.y);
            roundedYaw = (int)yawDelta;
            switch (type.getValue()) {
                case "Плавная" -> {
                    yaw = Math.min(Math.max(Math.abs(yawDelta), 1.0F), rotationYawSpeed);
                    pitch = Math.max(Math.abs(pitchDelta), 1.0F);
                    gcd = rotate.x + (yawDelta > 0.0F ? yaw : -yaw);
                    pitch = MathHelper.clamp(rotate.y + (pitchDelta > 0.0F ? pitch : -pitch), -89.0F, 89.0F);
                    gcd -= (gcd - rotate.x) % SensUtility.getGCDValue();
                    pitch -= (pitch - rotate.y) % SensUtility.getGCDValue();
                    rotate = new Vector2f(gcd, pitch);
                    this.lastYaw = yaw;
                    this.lastPitch = pitch;
                }
                
                case "Резкая" -> {
                    yaw = rotate.x + roundedYaw;
                    pitch = MathHelper.clamp(rotate.y + pitchDelta, -90.0F, 90.0F);
                    gcd = SensUtility.getGCDValue();
                    yaw -= (yaw - rotate.x) % gcd;
                    pitch -= (pitch - rotate.y) % gcd;
                    rotate = new Vector2f(yaw, pitch);
                } 
                
                case "HolyWorld" -> {
                    float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0.0001f), 52f);
                    float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0.0001f), 22f);

                    float randomYawFactor = (float) (Math.random() * 1.5 - 1.5);
                    float randomPitchFactor = (float) (Math.random() * 1.5 - 1.0);
                    float randomThreshold = (float) (Math.random() * 1.5);
                    float randomAddition = (float) (Math.random() * 2.5 + 1.5);

                    if (selected != target) {
                        clampedPitch = Math.max(Math.abs(pitchDelta), 1.0f);
                    } else {
                        clampedPitch /= 3f;
                    }

                    if (Math.abs(clampedYaw - this.lastYaw) <= randomThreshold) {
                        clampedYaw = this.lastYaw + randomAddition;
                    }

                    clampedYaw += randomYawFactor;
                    clampedPitch += randomPitchFactor;

                    yaw = rotate.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                    pitch = clamp(rotate.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -90.0F, 90.0F);
                    gcd = SensUtility.getGCDValue();
                    
                    yaw -= (yaw - rotate.x) % gcd;
                    pitch -= (pitch - rotate.y) % gcd;

                    rotate = new Vector2f(yaw, pitch);

                    lastYaw = clampedYaw;
                    lastPitch = clampedPitch;
                }
            }
        }

        if (options.is("Коррекция движения").getValue()) {
            mc.player.rotationYawOffset = rotate.x;
        }
    }

    private void visualRotationUpdate() {
        updateSpinRotation();
        Vector3d vec = target.getPositionVec().add(0, clamp(mc.player.getPosYEye() - target.getPosY(), 0, target.getHeight() * (mc.player.getDistanceEyePos(target) / attackDistance())), 0).subtract(mc.player.getEyePosition(1.0F));

        float yawDelta = (float) wrapDegrees( wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90) - visualRotateVector.x);
        float pitchDelta = (float) wrapDegrees( (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z)))) - visualRotateVector.y);
        int roundYawDelta = (int) Math.abs(yawDelta);
        int roundPitchDelta = (int) Math.abs(pitchDelta);
        float gcd = SensUtility.getGCDValue();

        float yaw = 0, pitch = 0;
        float clampedPitch = 0, clampedYaw = 0;

        switch (visualType.getValue()) {
            case "Обычная" -> {
                clampedYaw = Math.min(roundYawDelta, 40);
                clampedPitch = Math.min(roundPitchDelta * 0.33f, 30);

                yaw = visualRotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                pitch = clamp(visualRotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);
                yaw -= (yaw - visualRotateVector.x) % gcd;
                pitch -= (pitch - visualRotateVector.y) % gcd;
            }

            case "Спин-бот" -> {
                clampedPitch = Math.min(roundPitchDelta * 0.33f, 30);

                pitch = clamp(visualRotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);
                pitch -= (pitch - visualRotateVector.y) % gcd;

                yaw = rotationAngle;
            }

            case "Никакая" -> {
                yaw = mc.player.rotationYaw;
                pitch = mc.player.rotationPitch;
            }
        }

        visualRotateVector = new Vector2f(yaw, pitch);

        lastYaw = clampedYaw;
        lastPitch = clampedPitch;
    }

    public void updateSpinRotation() {
        float limitSpeed = 60;
        float reverseSpeed = 5;

        if (!isReversing) {
            rotationSpeed += reverseSpeed;
            if (rotationSpeed > limitSpeed) {
                rotationSpeed = limitSpeed;
                isReversing = true;
            }
        } else {
            rotationSpeed -= reverseSpeed;
            if (rotationSpeed < -limitSpeed) {
                rotationSpeed = -limitSpeed;
                isReversing = false;
            }
        }

        rotationAngle += rotationSpeed;
        rotationAngle = (rotationAngle + 360.0F) % 360.0F;
    }

    public void critHelper() {
        switch (critType.getValue()) {
            case "None"-> {
                return;
            }

            case "Matrix" -> {
                if (mc.player.isJumping && mc.player.motion.getY() < -0.1 && mc.player.fallDistance > 0.5 && MoveUtility.getMotion() < 0.12) {
                    mc.player.motion.y = -1.0;
                }
            }

            case "NCP" -> {
                if (!mc.player.isJumping || mc.player.fallDistance == 0.0f) break;
                mc.player.motion.y -= 0.078;
            }

            case "NCP+" -> {
                if ((mc.player.fallDistance > 0.7 && mc.player.fallDistance < 0.8) && target != null) {
                    mc.timer.timerSpeed = 2.0f;
                } else {
                    mc.timer.timerSpeed = 1;
                }
            }

            case "Grim" -> {
                if (mc.player.isJumping && mc.player.fallDistance > 0.0F && mc.player.fallDistance <= 1.2 && !MoveUtility.moveKeysPressed()) {
                    mc.player.jumpTicks = 0;
                    if (mc.timer.timerSpeed == 1.0) {
                        mc.timer.timerSpeed = 1.0049999952316284f;
                    }
                }
            }
        }
    }

    private void forceAttack() {
        if (!isAttacking) {
            if (!canAttack()) {
                return;
            }
        }

        isAttacking = true;

        try {
            preAttack();
            attack();
            postAttack();
        } finally {
            isAttacking = false;
        }
    }

    private boolean canAttack() {
        selected = MouseUtility.getMouseOver(target, rotate.x, rotate.y, attackDistance());

        if ((mc.player.getDistanceEyePos(target)) > attackDistance()) {
            return false;
        }

        if (!crystalAuraRule) {
            return false;
        }

        if (moreOptions.is("Проверка луча").getValue() && !elytraTargetRule) {
            if (selected == null) {
                return false;
            }
        }

        if (!moreOptions.is("Бить через стены").getValue()) {
            if (!mc.player.canEntityBeSeen(target)) {
                return false;
            }
        }

        if (moreOptions.is("Не бить если кушаешь").getValue()) {
            if (mc.player.isHandActive() && mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT) {
                return false;
            }
        }

        if (moreOptions.is("Не бить если в гуи").getValue()) {
            if (mc.currentScreen != null && !(mc.currentScreen instanceof ClickGuiScreen || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof IngameMenuScreen)) {
                return false;
            }
        }

        return true;
    }

    private void preAttack() {
        Evaware.getInst().getModuleManager().getTPInfluence().hitAuraTPPRe(target);

        if (mc.player.isBlocking() && options.is("Отжимать щит").getValue()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }
    }


    private void attack() {
        int minCPSValue = minCPS.getValue().intValue();
        int maxCPSValue = maxCPS.getValue().intValue();

        if (minCPSValue > maxCPSValue) {
            maxCPSValue = minCPSValue;
        }

        int minMS = 1000 / maxCPSValue;
        int maxMS = 1000 / minCPSValue;

        Random random = new Random();
        int randomMS = random.nextInt(maxMS - minMS + 1) + minMS;

        timerUtility.setLastMS(clickType.is("1.9") ? 500 : randomMS);
        Criticals.cancelCrit = true;

        if (Evaware.getInst().getModuleManager().getCriticals().isEnabled() && Criticals.canUseCriticals()) {
            Evaware.getInst().getModuleManager().getCriticals().sendCrit();
        }

        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);

        Criticals.cancelCrit = false;
    }

    private void postAttack() {
        if (target instanceof PlayerEntity player && options.is("Ломать щит").getValue()) {
            breakShieldPlayer(player);
        }

        Evaware.getInst().getModuleManager().getTPInfluence().hitAuraTPPost(target);
    }


    public boolean shouldPlayerFalling() {
        return AttackUtility.isPlayerFalling(options.is("Только криты").getValue() && !Criticals.canUseCriticals(), smartCrits.getValue(), options.is("Синхронизировать с TPS").getValue(), clickType.is("1.9"));
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;

        if (entity.ticksExisted < 3) return false;
        if ((mc.player.getDistanceEyePos(entity)) > maxRange()) return false;

        if (entity == Evaware.getInst().getModuleManager().getFreeCam().fakePlayer) return false;

        if (entity instanceof PlayerEntity p) {
            if (AntiBot.isBot(entity)) {
                return false;
            }
            if (!targets.is("Друзья").getValue() && FriendManager.isFriend(p.getName().getString())) {
                return false;
            }

            if (p.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;
        }

        if (entity instanceof PlayerEntity && !targets.is("Игроки").getValue()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !targets.is("Голые").getValue()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getTotalArmorValue() == 0 && !targets.is("Голые невидимки").getValue()) {
            return false;
        }

        if (entity instanceof PlayerEntity && entity.isInvisible() && !targets.is("Невидимки").getValue()) {
            return false;
        }

        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
            return false;
        }

        if ((entity instanceof MonsterEntity || entity instanceof SlimeEntity || entity instanceof VillagerEntity) && !targets.is("Мобы").getValue()) {
            return false;
        }
        if (entity instanceof AnimalEntity && !targets.is("Животные").getValue()) {
            return false;
        }

        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void breakShieldPlayer(PlayerEntity entity) {
        if (entity.isBlocking()) {
            int invSlot = InventoryUtility.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtility.getInstance().getAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtility.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);

                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }

            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }


    private void reset() {
        if (options.is("Коррекция движения").getValue()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
        rotate = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        timerUtility.setLastMS(0);
        target = null;
        mc.timer.timerSpeed = 1;
    }
}