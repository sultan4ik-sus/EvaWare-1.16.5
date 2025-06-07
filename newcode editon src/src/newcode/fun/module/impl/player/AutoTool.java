package newcode.fun.module.impl.player;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.math.BlockRayTraceResult;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(name = "AutoTool", type = TypeList.Player)
public class AutoTool extends Module {
    private int oldSlot = -1;
    private int originalSlot = -1;
    private boolean status;
    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil returnTimer = new TimerUtil();
    private static final long SWAP_DELAY = 300;
    private static final long RETURN_DELAY = 300;

    @Override
    public boolean onEvent(final Event event) {
        if (mc.player == null || mc.world == null) return false;

        if (event instanceof EventUpdate) {
            if (mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.pressed) {
                if (!timer.hasTimeElapsed(SWAP_DELAY)) {
                    return false;
                }

                int bestSlot = findBestSlot();

                if (bestSlot == -1) return false;

                status = true;

                if (oldSlot == -1) oldSlot = mc.player.inventory.currentItem;

                if (bestSlot >= 9 && bestSlot < 36) {
                    int hotbarSlot = findEmptyHotbarSlot();
                    if (hotbarSlot == -1) hotbarSlot = mc.player.inventory.currentItem;

                    originalSlot = bestSlot;

                    swapSlots(bestSlot, hotbarSlot);
                    bestSlot = hotbarSlot;
                }

                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.player.inventory.currentItem = bestSlot;

                timer.reset();
            } else if (status) {
                if (!returnTimer.hasTimeElapsed(RETURN_DELAY)) {
                    return false;
                }

                if (originalSlot != -1) {
                    swapSlots(mc.player.inventory.currentItem, originalSlot);
                    originalSlot = -1;
                }

                mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
                mc.player.inventory.currentItem = oldSlot;
                reset();

                returnTimer.reset();
            }
        }
        return false;
    }

    private void reset() {
        oldSlot = -1;
        status = false;
    }

    private int findBestSlot() {
        if (mc.objectMouseOver instanceof BlockRayTraceResult) {
            BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
            Block block = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(slot);
                if (stack.isEmpty()) continue;

                float speed = stack.getDestroySpeed(block.getDefaultState());
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = slot;
                }
            }
            return bestSlot;
        }
        return -1;
    }

    private int findEmptyHotbarSlot() {
        for (int slot = 0; slot < 9; slot++) {
            if (mc.player.inventory.getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private void swapSlots(int slot1, int slot2) {
        mc.playerController.windowClick(0, slot1 < 9 ? slot1 + 36 : slot1, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, slot2 < 9 ? slot2 + 36 : slot2, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, slot1 < 9 ? slot1 + 36 : slot1, 0, ClickType.PICKUP, mc.player);
    }

    @Override
    protected void onDisable() {
        if (originalSlot != -1) {
            swapSlots(mc.player.inventory.currentItem, originalSlot);
            originalSlot = -1;
        }

        if (oldSlot != -1) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
            mc.player.inventory.currentItem = oldSlot;
        }

        reset();
        super.onDisable();
    }
}