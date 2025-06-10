package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.EventLivingUpdate;
import eva.ware.events.EventMotion;
import eva.ware.events.EventPacket;
import eva.ware.events.EventRender2D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.combat.HitAura;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.player.FreeCameraUtility;
import eva.ware.utils.player.MoveUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.font.ClientFonts;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "FreeCam", category = Category.Movement)
public class FreeCam extends Module {

    private final SliderSetting speedXZ = new SliderSetting("Скорость по XZ", 1.0f, 0.1f, 5.0f, 0.05f);
    private final SliderSetting speedY = new SliderSetting("Скорость по Y", 0.5f, 0.1f, 1.0f, 0.05f);

    public FreeCam() {
        addSettings(speedXZ, speedY);
    }

    public FreeCameraUtility fakePlayer = null;
    boolean oldIsFlying;

    @Subscribe
    public void onLivingUpdate(EventLivingUpdate e) {
        if (fakePlayer != null) {
            mc.player.movementInput.jump = mc.player.movementInput.sneaking = mc.player.movementInput.forwardKeyDown = mc.player.movementInput.backKeyDown = mc.player.movementInput.leftKeyDown = mc.player.movementInput.rightKeyDown = false;
            mc.player.moveForward = 0;
            mc.player.moveStrafing = 0;
            mc.player.motion = new Vector3d(0, 0, 0);

            fakePlayer.noClip = true;
            fakePlayer.setOnGround(false);

            MoveUtility.setMotion(speedXZ.getValue(), fakePlayer);
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                fakePlayer.motion.y = speedY.getValue();
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                fakePlayer.motion.y = -speedY.getValue();
            }
            fakePlayer.abilities.isFlying = true;
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        assert mc.player != null;

        HitAura hitAura = Evaware.getInst().getModuleManager().getHitAura();

        if (mc.player.ticksExisted % 10 == 0) {
            mc.player.connection.sendPacket(new CPlayerPacket(mc.player.isOnGround()));
        }

        if (fakePlayer == null) toggle();

        if (hitAura.isEnabled()) {
            if (hitAura.getTarget() != null) {
                mc.player.rotationYaw = hitAura.rotate.x;
                mc.player.rotationPitch = hitAura.rotate.y;
            }
        }

        mc.player.motion = Vector3d.ZERO;
        e.cancel();
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CPlayerPacket p) {
            if (p.moving) {
                p.x = fakePlayer.getPosX();
                p.y = fakePlayer.getPosY();
                p.z = fakePlayer.getPosZ();
            }
            p.onGround = fakePlayer.isOnGround();
            if (p.rotating) {
                p.yaw = fakePlayer.rotationYaw;
                p.pitch = fakePlayer.rotationPitch;
            }
        }
    }

    @Subscribe
    public void onRender2D(EventRender2D e) {
        float getAnim = (float) getAnimation().getValue();

        int fakePlayerPosX = (int) fakePlayer.getPosX(), fakePlayerPosY = (int) fakePlayer.getPosY(), fakePlayerPosZ = (int) fakePlayer.getPosZ();
        int playerPosX = (int) mc.player.getPosX(), playerPosY = (int) mc.player.getPosY(), playerPosZ = (int) mc.player.getPosZ();

        int deltaX = (int) Math.abs(playerPosX - fakePlayerPosX);
        int deltaY = (int) Math.abs(playerPosY - fakePlayerPosY);
        int deltaZ = (int) Math.abs(playerPosZ - fakePlayerPosZ);

        String minusXPrefix = (playerPosX == fakePlayerPosX) ? "" : (fakePlayerPosX > playerPosX ? "" : "-");
        String minusYPrefix = (playerPosY == fakePlayerPosY) ? "" : (fakePlayerPosY > playerPosY ? "" : "-");
        String minusZPrefix = (playerPosZ == fakePlayerPosZ) ? "" : (fakePlayerPosZ > playerPosZ ? "" : "-");

        String renderPos = "X: " + minusXPrefix + deltaX + " Y: " + minusYPrefix + deltaY + " Z: " + minusZPrefix + deltaZ;

        float xCoord = mc.getMainWindow().getScaledWidth() / 2f;
        float yCoord = (float) (mc.getMainWindow().getScaledHeight() / 2f);

        RenderUtility.drawContrast(1 - (float) (getAnimation().getValue() / 3f) * 0.2f);
        RenderUtility.drawWhite((float) getAnimation().getValue() * 0.2f);

        ClientFonts.msBold[16].drawCenteredString(e.getMatrixStack(), renderPos, xCoord, yCoord + 10 * getAnimation().getValue(), -1);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) {
            return;
        }
        mc.player.setJumping(false);
        mc.player.movementInput = new MovementInput();
        mc.player.moveForward = 0;
        mc.player.moveStrafing = 0;
        mc.player.motion = new Vector3d(0, 0, 0);
        mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindSneak.pressed = mc.gameSettings.keyBindForward.pressed = mc.gameSettings.keyBindBack.pressed = mc.gameSettings.keyBindLeft.pressed = mc.gameSettings.keyBindRight.pressed = false;
        initializeFakePlayer();
        addFakePlayer();
        fakePlayer.spawn();
        mc.setRenderViewEntity(fakePlayer);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) {
            return;
        }
        removeFakePlayer();
        mc.setRenderViewEntity(null);
        mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
    }

    private void initializeFakePlayer() {
        assert mc.player != null;
        fakePlayer = new FreeCameraUtility(1337228);
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.inventory = mc.player.inventory;
    }

    private void addFakePlayer() {
        assert mc.world != null;
        mc.world.addEntity(1337228, fakePlayer);
    }

    private void removeFakePlayer() {
        assert mc.world != null;
        resetFlying();
        mc.world.removeEntityFromWorld(1337228);
        fakePlayer = null;
    }

    private void resetFlying() {
        assert mc.player != null;
        if (oldIsFlying) {
            mc.player.abilities.isFlying = false;
            oldIsFlying = false;
        }
    }
}
