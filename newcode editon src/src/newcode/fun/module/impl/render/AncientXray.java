package newcode.fun.module.impl.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.utils.render.RenderUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Annotation(
        name = "AncientXray",
        type = TypeList.Render,
        desc = "Подсвечивает незер обломки"
)
public class AncientXray extends Module {

    private final Map<BlockState, Integer> blocks = new HashMap<>();

    public AncientXray() {
        addBlock(Blocks.ANCIENT_DEBRIS.getDefaultState(), new Color(255, 255, 255).getRGB());
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender) {
            handleRender();
        }
        return false;
    }

    private void handleRender() {
        if (mc.world == null || mc.player == null) return;

        BlockPos playerPos = mc.player.getPosition();
        int range = 29;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    Integer color = blocks.get(state);
                    if (color != null) {
                        RenderUtils.Render3D.drawBlockBox(pos, color);
                    }
                }
            }
        }
    }

    private void addBlock(BlockState blockState, int color) {
        blocks.put(blockState, color);
    }
}
