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
    private final SliderSetting speed = new SliderSetting("�������� �� XZ", 0.85f, 0.1f, 2.0f, 0.05f);
    private final SliderSetting motionY = new SliderSetting("�������� Y", 0.1f, 0.1f, 0.5f, 0.05f);
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
            handleMotionEvent(motionEvent); // �������� ���������� MotionEvent
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

        // ��������� ��������� ������� ������
        initialPosition = mc.player.getPositionVec();

        // ������������� ������� �������� � ��������� �������� ������
        mc.player.setMotion(Vector3d.ZERO);
        mc.player.moveForward = 0;
        mc.player.moveStrafing = 0;
        mc.player.moveVertical = 0;

        // �������� ���������� ������ ������ ���������
        mc.player.movementInput = new MovementInput() {
            public void updatePlayerMoveState() {
                this.forwardKeyDown = false;
                this.backKeyDown = false;
                this.leftKeyDown = false;
                this.rightKeyDown = false;
                this.jump = false;
            }
        };

        // �������������� � ��������� ��������� ������
        initializeFakePlayer();
        addFakePlayer();
        player.spawn();
        mc.setRenderViewEntity(player); // ������������� ������ �� ��������� ������
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) {
            return;
        }

        // ���������� ���������� ��������� ������
        mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);

        // ������� ��������� ������ � ���������� ������ �� ��������� ������
        removeFakePlayer();
        mc.setRenderViewEntity(mc.player);
    }



    /**
     * ���������� ������� EventLivingUpdate.
     * ������������� ����������� �������� � ��������� ��� ������.
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

        // ������������� ��������
        player.setHealth(mc.player.getHealth());
        player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(
                mc.player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue()
        );
        player.setAbsorptionAmount(mc.player.getAbsorptionAmount());

        oldIsFlying = player.abilities.isFlying;
        player.abilities.isFlying = true;
    }


    /**
     * ���������� ������� EventMotion.
     * ���������� ����� CPlayerPacket �� ������(���� ����� ��������� �� Sunrise) � �������� �������.
     */
    private void handleMotionEvent(EventMotion motionEvent) {
        motionEvent.setCancel(true);
    }

    /**
     * ���������� ������� EventRender.
     * ���������� ���������� � ����������� ������ � 2D �������.
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
        Fonts.newcode[14].drawCenteredStringWithOutline(renderEvent.matrixStack, "���� ���������� �������", (double)((float)resolution.getScaledWidth() / 2.0F), (double)((float)resolution.getScaledHeight() / 2.0F + 40.0F), -1);

    }

    /**
     * �������������� ��������� ������.
     * ������������� ��������� �������� ������� � ����� ��������.
     */
    private void initializeFakePlayer() {
        // ��������� ������� ������� ��������� ������
        clientPosition = mc.player.getPositionVec();

        // ������� ��������� ������ � ���������� ID
        player = new FreeCamUtils(1337228);

        // �������� �������������� � ���� ��������
        player.copyLocationAndAnglesFrom(mc.player);
        player.rotationYawHead = mc.player.rotationYawHead;

        // �������� �������� � ������������ ��������
        player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(
                mc.player.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue()
        );
        player.setHealth(mc.player.getHealth());
        player.setAbsorptionAmount(mc.player.getAbsorptionAmount());

        // �������� ���������
        player.inventory.copyInventory(mc.player.inventory);

        // �������� ������� �����
        player.clearActivePotions();
        mc.player.getActivePotionEffects().forEach(player::addPotionEffect);

        // �������� ��������� (��������, �������, ��������, ����� � �.�.)
        player.setFire(mc.player.getFireTimer());
        player.setSneaking(mc.player.isSneaking());
        player.setSprinting(mc.player.isSprinting());

        // ������������� ���� � �������
        player.experience = mc.player.experience;
        player.experienceLevel = mc.player.experienceLevel;
        player.experienceTotal = mc.player.experienceTotal;
    }

    /**
     * ��������� ��������� ������ � ��� � ��������� ������� ������� ������.
     */
    private void addFakePlayer() {
        clientPosition = mc.player.getPositionVec();
        mc.world.addEntity(1337228, player);
    }

    /**
     * ������� ��������� ������ �� ����.
     * ��������������� ��������� � ������� ������.
     */
    private void removeFakePlayer() {
        resetFlying();
        mc.world.removeEntityFromWorld(1337228);
        player = null;
        clientPosition = null;
    }

    /**
     * ���������� ��������� ������ ������, ���� ��� ���� ��������� �� ������ ������.
     */
    private void resetFlying() {
        if (oldIsFlying) {
            mc.player.abilities.isFlying = false;
            oldIsFlying = false;
        }
    }
}
