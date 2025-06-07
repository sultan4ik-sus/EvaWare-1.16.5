package newcode.fun.module.impl.movement;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.move.MoveUtil;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Annotation(
        name = "Speed",
        type = TypeList.Movement, desc = "Ускоряет бега игрока"
)
public class Speed extends Module {
    public ModeSetting mode = new ModeSetting("Выбор режимов", "Grim", new String[]{"Grim", "Collision", "Matrix"});
    public SliderSetting speed2 = new SliderSetting("Скорость", 1.09f, 1f, 1.2f, 0.01f).setVisible(() -> mode.is("Grim"));
    public SliderSetting size = new SliderSetting("Радиус",  2.1f, 1f, 3f, 0.01f).setVisible(() -> mode.is("Grim"));
    public BooleanOption onlyaura = new BooleanOption("Только с аурой", false).setVisible(() -> mode.is("Grim"));
    public SliderSetting speed3 = new SliderSetting("Скорocть", 0.36f, 0.10f, 1f, 0.01f).setVisible(() -> mode.is("Matrix"));
    public SliderSetting speed4 = new SliderSetting("Скоpоcть", 1.6f, 1f, 2, 0.1f).setVisible(() -> mode.is("Collision"));
    public BooleanOption autojump = new BooleanOption("Авто прыжок", true).setVisible(() -> mode.is("Matrix"));
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Speed() {
        this.addSettings(mode, autojump, speed3, speed4, onlyaura,  speed2, size);
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate && (this.mode.is("Grim") || this.mode.is("Collision") || this.mode.is("Matrix"))) {
            if (this.mode.is("Matrix")) {
                if (!mc.player.isInWater() && !mc.player.isOnGround()) {
                    MoveUtil.setSpeed(speed3.getValue().floatValue());
                }
                if (autojump.get()) {
                    if (MoveUtil.isMoving() && mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.jump();
                    }
                }
            }

            if (this.mode.is("Collision")) {
                AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.1);
                int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
                boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
                if (canBoost && !mc.player.isOnGround()) {
                    mc.player.jumpMovementFactor = armorstans > 1 ? 1.0F / (float)armorstans : (speed4.getValue().floatValue() / 10);
                }
            }

            if (this.onlyaura.get() && Manager.FUNCTION_MANAGER.auraFunction.target == null || mc.player.ticksExisted < 20) {
                return false;
            }

            Iterator var2 = mc.world.getPlayers().iterator();
            while (var2.hasNext()) {
                PlayerEntity entity = (PlayerEntity) var2.next();
                if (mc.player != entity && this.mode.is("Grim") && mc.player.getDistance(entity) <= size.getValue().floatValue() && (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown())) {
                    float speed = speed2.getValue().floatValue();
                    Vector3d motion = mc.player.getMotion();
                    mc.player.setMotion(motion.x * speed, motion.y, motion.z * speed);
                }
            }
        }
        return false;
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }
}
