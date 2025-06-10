package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.MoveUtility;
import eva.ware.utils.player.PlayerUtility;
import eva.ware.utils.player.rotation.RayTraceUtility;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {
    private final CheckBoxSetting rayCast = new CheckBoxSetting("Проверка луча", false);
    private final CheckBoxSetting moveFix = new CheckBoxSetting("Коррекция движения", true);

    private BlockCache blockCache, lastBlockCache;
    public Vector2f rotation;
    private float savedY;

    public boolean check() {
        return rotation != null && moveFix.getValue();
    }

    public Scaffold() {
        addSettings(rayCast, moveFix);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (moveFix.getValue()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
        rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);

    }

    @Override
    public void onEnable() {
        super.onEnable();
        mc.player.multiplyMotionXZ(0);
        rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        if (mc.player != null)
            savedY = (float) mc.player.getPosY();
    }

    public boolean sneak;
    public TimerUtility timerUtility = new TimerUtility();

    @Subscribe
    public void onMotion(EventMotion e) {
        mc.player.setSprinting(false);
        if (mc.player.isOnGround()) {
            savedY = (float) Math.floor(mc.player.getPosY() - 1.0);
        }

        blockCache = getBlockInfo();
        if (blockCache != null) {
            lastBlockCache = getBlockInfo();
        } else {
            return;
        }

        float[] rot = getRotations(blockCache.position, blockCache.facing);
        rotation = new Vector2f(rot[0], rot[1]);

        if (!moveFix.getValue()) {
            rotation.x = MoveUtility.moveYaw(mc.player.rotationYaw + 180);
        }

        e.setYaw(rotation.x);
        e.setPitch(rotation.y);

        mc.player.rotationYawHead = rotation.x;
        mc.player.renderYawOffset = PlayerUtility.calculateCorrectYawOffset(rotation.x);
        mc.player.rotationPitchHead = rotation.y;
    }

    @Subscribe
    public void onInput(EventInput e) {
        if (check()) {
            MoveUtility.negativeFixMovement(e, rotation.x);
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (blockCache == null || lastBlockCache == null) return;

        int block = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() instanceof BlockItem) {
                block = i;
                break;
            }
        }

        if (block == -1) {
            print("Не найдено блоков!");
            toggle();
            return;
        }

        if (rotation == null)
            return;

        RayTraceResult result = RayTraceUtility.rayTrace(3, rotation.x, rotation.y, mc.player);

        if (rayCast.getValue()) {
            if (!(mc.world.getBlockState(mc.player.getPosition().add(0, -0.5f, 0)).getBlock() == Blocks.AIR && result.getType() == RayTraceResult.Type.BLOCK)) {
                return;
            }
        }

        int last = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = block;
        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(getVector(lastBlockCache), lastBlockCache.getFacing(), lastBlockCache.getPosition(), false));
        mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
        mc.player.inventory.currentItem = last;
        blockCache = null;
    }

    public float[] getRotations(BlockPos blockPos, Direction enumFacing) {
        double d = (double) blockPos.getX() + 0.5 - mc.player.getPosX() + (double) enumFacing.getXOffset() * 0.25;
        double d2 = (double) blockPos.getZ() + 0.5 - mc.player.getPosZ() + (double) enumFacing.getZOffset() * 0.25;
        double d3 = mc.player.getPosY() + (double) mc.player.getEyeHeight() - blockPos.getY() - (double) enumFacing.getYOffset() * 0.25;
        double d4 = MathHelper.sqrt(d * d + d2 * d2);

        double playerX = mc.player.getPosX();
        double playerY = mc.player.getPosY() + mc.player.getEyeHeight();
        double playerZ = mc.player.getPosZ();

        double blockMinX = blockPos.getX();
        double blockMaxX = blockPos.getX() + 1;
        double blockMinY = blockPos.getY();
        double blockMaxY = blockPos.getY() + 1;
        double blockMinZ = blockPos.getZ();
        double blockMaxZ = blockPos.getZ() + 1;

        double closestX = Math.max(blockMinX, Math.min(playerX, blockMaxX));
        double closestY = Math.max(blockMinY, Math.min(playerY, blockMaxY));
        double closestZ = Math.max(blockMinZ, Math.min(playerZ, blockMaxZ));

        double dX = closestX - playerX;
        double dZ = closestZ - playerZ;
        double dY = closestY - playerY;

        double distance = Math.sqrt(dX * dX + dZ * dZ);

//        float yaw = (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (Math.atan2(dY, distance) * 180.0 / Math.PI);
        float yaw = MoveUtility.negativeMoveYaw(mc.player.rotationYaw);

        return new float[]{MathHelper.wrapDegrees(yaw), -pitch};
    }

    public class BlockCache {
        private final BlockPos position;
        private final Direction facing;

        public BlockCache(final BlockPos position, final Direction facing) {
            this.position = position;
            this.facing = facing;
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public Direction getFacing() {
            return this.facing;
        }
    }


    public BlockCache getBlockInfo() {
        int y = (int) (mc.player.getPosY() - 1.0 >= savedY && Math.abs(mc.player.getPosY() - savedY) <= 3.0 && !mc.gameSettings.keyBindJump.isKeyDown() ? savedY : mc.player.getPosY() - 1.0);

        final BlockPos belowBlockPos = new BlockPos(mc.player.getPosX(), y - (mc.player.isSneaking() ? -1 : 0), mc.player.getPosZ());

        for (Direction direction : Direction.values()) {
            final BlockPos block = belowBlockPos.offset(direction);
            final Material material = mc.world.getBlockState(block).getBlock().getDefaultState().getMaterial();

            if (material.isSolid() && !material.isLiquid()) {
                return new BlockCache(block, direction.getOpposite());
            }
        }

        return null;
    }

    public Vector3d getVector(BlockCache data) {
        BlockPos pos = data.position;
        Direction face = data.facing;
        double x = (double) pos.getX() + 0.5, y = (double) pos.getY() + 0.5, z = (double) pos.getZ() + 0.5;
        if (face != Direction.UP && face != Direction.DOWN) {
            y += 0.5;
        } else {
            x += 0.3;
            z += 0.3;
        }
        if (face == Direction.WEST || face == Direction.EAST) {
            z += 0.15;
        }
        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += 0.15;
        }
        return new Vector3d(x, y, z);
    }

}
