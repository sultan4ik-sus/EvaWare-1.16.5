package newcode.fun.module.impl.movement;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventLivingUpdate;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.FreeCamUtils;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.move.MoveUtil;

@SuppressWarnings("all")
@Annotation(name = "FreeCam", type = TypeList.Movement)
public class FreeCam extends Module {
    private final SliderSetting speed = new SliderSetting("Скорость по XZ", 0.85f, 0.1f, 2.0f, 0.05f);
    private final SliderSetting motionY = new SliderSetting("Скорость Y", 0.1f, 0.1f, 0.5f, 0.05f);
    private Vector3d clientPosition = null;
    public FreeCamUtils player = null;
    boolean oldIsFlying;

    public FreeCam() {
        addSettings(speed, motionY);
    }

    @Override
    public boolean onEvent(Event event) {
        mc.player.setVelocity(0, 0, 0);
        MoveUtil.setMotion(0);
        if (event instanceof EventLivingUpdate livingUpdateEvent) {
            if (player != null) {
                player.noClip = true;
                player.setOnGround(false);
                MoveUtil.setMotion(speed.getValue().floatValue(), player);
                if (Minecraft.getInstance().gameSettings.keyBindJump.isKeyDown()) {
                    player.setPosition(player.getPosX(), player.getPosY() + (motionY.getValue().floatValue() / 2), player.getPosZ());
                }
                if (Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown()) {
                    player.setPosition(player.getPosX(), player.getPosY() - (motionY.getValue().floatValue() / 2), player.getPosZ());
                }
                player.abilities.isFlying = true;

            }
       }

        if (event instanceof EventPacket e) {
            if (e.getPacket() instanceof CPlayerPacket p) {
                if (p.moving) {
                    p.x = player.getPosX();
                    p.y = player.getPosY();
                    p.z = player.getPosZ();
                }
                p.onGround = player.isOnGround();
                if (p.rotating) {
                    p.yaw = player.rotationYaw;
                    p.pitch = player.rotationPitch;
                }
            }
        }
        if (event instanceof EventMotion motionEvent) {
            handleMotionEvent(motionEvent); // Вызываем обработчик MotionEvent
        }

        if (event instanceof EventRender && ((EventRender) event).isRender2D()) {
            handleRender2DEvent((EventRender) event);
        }
        return false;
    }

    private Vector3d initialPosition;

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) {
            return;
        }

        // Сохраняем начальную позицию игрока
        initialPosition = mc.player.getPositionVec();

        // Устанавливаем нулевую скорость и блокируем движение игрока
        mc.player.setMotion(Vector3d.ZERO);
        mc.player.moveForward = 0;
        mc.player.moveStrafing = 0;
        mc.player.moveVertical = 0;

        // Заменяем управление игрока пустым движением
        mc.player.movementInput = new MovementInput() {
            public void updatePlayerMoveState() {
                this.forwardKeyDown = false;
                this.backKeyDown = false;
                this.leftKeyDown = false;
                this.rightKeyDown = false;
                this.jump = false;
            }
        };

        // Инициализируем и добавляем фейкового игрока
        initializeFakePlayer();
        addFakePlayer();
        player.spawn();
        mc.setRenderViewEntity(player); // Устанавливаем камеру на фейкового игрока
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) {
            return;
        }

        // Возвращаем управление реальному игроку
        mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);

        // Убираем фейкового игрока и возвращаем камеру на реального игрока
        removeFakePlayer();
        mc.setRenderViewEntity(mc.player);
    }



    /**
     * Обработчик события EventLivingUpdate.
     * Устанавливает необходимые значения и состояния для игрока.
     */
    private void handleLivingUpdate() {
        player.noClip = true;
        player.setOnGround(false);
        MoveUtil.setMotion(speed.getValue().floatValue(), player);

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            player.motion.y = motionY.getValue().floatValue();
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            player.motion.y = -motionY.getValue().floatValue();
        }

        // Синхронизация здоровья
        player.setHealth(mc.player.getHealth());
        player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(
                mc.player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue()
        );
        player.setAbsorptionAmount(mc.player.getAbsorptionAmount());

        oldIsFlying = player.abilities.isFlying;
        player.abilities.isFlying = true;
    }


    /**
     * Обработчик события EventMotion.
     * Отправляет пакет CPlayerPacket на сервер(если игрок находится на Sunrise) и отменяет событие.
     */
    private void handleMotionEvent(EventMotion motionEvent) {
        motionEvent.setCancel(true);
    }

    /**
     * Обработчик события EventRender.
     * Отображает информацию о перемещении игрока в 2D рендере.
     */
    private void handleRender2DEvent(EventRender renderEvent) {
        MainWindow resolution = mc.getMainWindow();

        if (clientPosition == null) {
            return;
        }

        int xPosition = (int) (player.getPosX() - mc.player.getPosX());
        int yPosition = (int) (player.getPosY() - mc.player.getPosY());
        int zPosition = (int) (player.getPosZ() - mc.player.getPosZ());

        String position = "X:" + (float) xPosition + " Y:" + (float) yPosition + " Z:" + (float) zPosition;
        Fonts.newcode[14].drawCenteredStringWithOutline(renderEvent.matrixStack, position, (double)((float)resolution.getScaledWidth() / 2.0F), (double)((float)resolution.getScaledHeight() / 2.0F + 47.0F), -1);
        Fonts.newcode[14].drawCenteredStringWithOutline(renderEvent.matrixStack, "Ваша визуальная позиция", (double)((float)resolution.getScaledWidth() / 2.0F), (double)((float)resolution.getScaledHeight() / 2.0F + 40.0F), -1);

    }

    /**
     * Инициализирует фейкового игрока.
     * Устанавливает начальные значения позиции и углов поворота.
     */
    private void initializeFakePlayer() {
        // Сохраняем текущую позицию реального игрока
        clientPosition = mc.player.getPositionVec();

        // Создаем фейкового игрока с уникальным ID
        player = new FreeCamUtils(1337228);

        // Копируем местоположение и углы вращения
        player.copyLocationAndAnglesFrom(mc.player);
        player.rotationYawHead = mc.player.rotationYawHead;

        // Копируем здоровье и максимальное здоровье
        player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(
                mc.player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue()
        );
        player.setHealth(mc.player.getHealth());
        player.setAbsorptionAmount(mc.player.getAbsorptionAmount());

        // Копируем инвентарь
        player.inventory.copyInventory(mc.player.inventory);

        // Копируем эффекты зелья
        player.clearActivePotions();
        mc.player.getActivePotionEffects().forEach(player::addPotionEffect);

        // Копируем состояния (например, горение, мокрость, голод и т.д.)
        player.setFire(mc.player.getFireTimer());
        player.setSneaking(mc.player.isSneaking());
        player.setSprinting(mc.player.isSprinting());

        // Устанавливаем опыт и уровень
        player.experience = mc.player.experience;
        player.experienceLevel = mc.player.experienceLevel;
        player.experienceTotal = mc.player.experienceTotal;
    }

    /**
     * Добавляет фейкового игрока в мир и сохраняет текущую позицию игрока.
     */
    private void addFakePlayer() {
        clientPosition = mc.player.getPositionVec();
        mc.world.addEntity(1337228, player);
    }

    /**
     * Удаляет фейкового игрока из мира.
     * Восстанавливает состояния и позицию игрока.
     */
    private void removeFakePlayer() {
        resetFlying();
        mc.world.removeEntityFromWorld(1337228);
        player = null;
        clientPosition = null;
    }

    /**
     * Сбрасывает состояние полета игрока, если оно было выключено до работы модуля.
     */
    private void resetFlying() {
        if (oldIsFlying) {
            mc.player.abilities.isFlying = false;
            oldIsFlying = false;
        }
    }
}
