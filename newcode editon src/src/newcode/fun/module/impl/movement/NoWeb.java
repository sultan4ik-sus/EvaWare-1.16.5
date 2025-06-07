package newcode.fun.module.impl.movement;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.utils.move.MoveUtil;

@Annotation(
        name = "NoWeb",
        type = TypeList.Movement,
        desc = "Делает вас быстрее в паутине"
)
public class NoWeb extends Module {
    public NoWeb() {
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            boolean headInWeb = false;
            boolean feetInWeb = false;

            double x;
            double z;
            for(x = -0.3; x <= 0.3; x += 0.3) {
                for(z = -0.3; z <= 0.3; z += 0.3) {
                    for(double y = (double)mc.player.getEyeHeight(); y >= 0.0; y -= 0.1) {
                        BlockPos headPos = new BlockPos(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z);
                        if (mc.world.getBlockState(headPos).getBlock() == Blocks.COBWEB) {
                            headInWeb = true;
                            break;
                        }
                    }
                }
            }

            if (!headInWeb) {
                for(x = -0.3; x <= 0.3; x += 0.3) {
                    for(z = -0.3; z <= 0.3; z += 0.3) {
                        BlockPos pos = new BlockPos(mc.player.getPosX() + x, mc.player.getPosY(), mc.player.getPosZ() + z);
                        if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                            feetInWeb = true;
                            break;
                        }
                    }
                }
            }

            if (!headInWeb && !feetInWeb) {
                BlockPos aboveHeadPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() + (double)mc.player.getEyeHeight() + 1.65, mc.player.getPosZ());
                if (mc.world.getBlockState(aboveHeadPos).getBlock() == Blocks.COBWEB) {
                    headInWeb = true;
                }
            }

            if (headInWeb || feetInWeb) {
                mc.player.motion.y = 0.0;
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motion.y = 1.15;
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motion.y = -1.15;
                }

                MoveUtil.setMotion(0.223);
            }
        }

        return false;
    }
}

