/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils;

import baritone.api.utils.Helper;
import baritone.api.utils.IPlayerContext;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import java.util.Random;

/**
 * @author Brady
 * @since 8/25/2018
 */

public final class BlockBreakHelper implements Helper {

    private final IPlayerContext ctx;
    private boolean didBreakLastTick;
    private final Random random = new Random();
    private int delayTicks = 0; // —чЄтчик задержки

    BlockBreakHelper(IPlayerContext ctx) {
        this.ctx = ctx;
    }

    public void stopBreakingBlock() {
        if (ctx.player() != null && didBreakLastTick) {
            if (!ctx.playerController().hasBrokenBlock()) {
                ctx.playerController().setHittingBlock(true);
            }
            ctx.playerController().resetBlockRemoving();
            didBreakLastTick = false;
        }
    }

    public void tick(boolean isLeftClick) {
        if (delayTicks > 0) {
            delayTicks--; // ”меньшаем таймер задержки
            return;
        }

        RayTraceResult trace = ctx.objectMouseOver();
        boolean isBlockTrace = trace != null && trace.getType() == RayTraceResult.Type.BLOCK;

        if (isLeftClick && isBlockTrace) {
            BlockPos pos = ((BlockRayTraceResult) trace).getPos();
            Direction face = ((BlockRayTraceResult) trace).getFace();

            if (!didBreakLastTick) {
                delayTicks = 3 + random.nextInt(6);

                ctx.playerController().syncHeldItem();
                ctx.playerController().clickBlock(pos, face);
                ctx.player().swingArm(Hand.MAIN_HAND);
            }

            if (ctx.playerController().onPlayerDamageBlock(pos, face)) {
                ctx.player().swingArm(Hand.MAIN_HAND);
            }

            ctx.playerController().setHittingBlock(false);
            didBreakLastTick = true;
        } else if (didBreakLastTick) {
            stopBreakingBlock();
            didBreakLastTick = false;
        }
    }
}
