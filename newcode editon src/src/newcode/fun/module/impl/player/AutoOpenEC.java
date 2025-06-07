package newcode.fun.module.impl.player;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.Manager;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(
        name = "AutoEC",
        type = TypeList.Player,
        desc = "Направляет ротацию на ближайший эндер сундук"
)
public class AutoOpenEC extends Module {
    private float yaw;
    private float pitch;
    private Vector3d lastPos = new Vector3d(0, 0, 0);
    private final TimerUtil timerUtil = new TimerUtil();

    public AutoOpenEC() {
        this.yaw = 0.0F;
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        lastPos = mc.player.getPositionVec();
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventMotion) {
            EventMotion eventMotion = (EventMotion) event;
            BlockPos chestPos = findNearestEnderChest();
            if (chestPos != null) {
                rotateToBlock(chestPos, eventMotion);
            }
        }
        return false;
    }

    private BlockPos findNearestEnderChest() {
        float searchRadius = 6.5f;
        BlockPos playerPos = mc.player.getPosition();
        BlockPos nearestChest = null;
        double minDistance = Double.MAX_VALUE;

        for (float x = playerPos.getX() - searchRadius; x <= playerPos.getX() + searchRadius; x++) {
            for (float y = playerPos.getY() - searchRadius; y <= playerPos.getY() + searchRadius; y++) {
                for (float z = playerPos.getZ() - searchRadius; z <= playerPos.getZ() + searchRadius; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    Block block = mc.world.getBlockState(currentPos).getBlock();

                    if (block == Blocks.ENDER_CHEST) {
                        double distanceSq = currentPos.distanceSq(playerPos);
                        if (distanceSq < minDistance) {
                            minDistance = distanceSq;
                            nearestChest = currentPos;
                        }
                    }
                }
            }
        }
        return nearestChest;
    }

    private void rotateToBlock(BlockPos pos, EventMotion eventMotion) {
        double dx = pos.getX() + 0.5 - mc.player.getPosX();
        double dy = pos.getY() + 0.5 - (mc.player.getPosY() + mc.player.getEyeHeight());
        double dz = pos.getZ() + 0.5 - mc.player.getPosZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        this.yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        this.pitch = (float) -Math.toDegrees(Math.atan2(dy, distance));

        eventMotion.setYaw(yaw);
        eventMotion.setPitch(pitch);
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = yaw;
        mc.player.rotationPitchHead = pitch;

        if (timerUtil.hasTimeElapsed(350L)) {
            BlockRayTraceResult blockRayTraceResult = new BlockRayTraceResult(
                    new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                    Direction.UP,
                    pos,
                    false
            );
            mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, blockRayTraceResult));
            timerUtil.reset();
        }
    }
}