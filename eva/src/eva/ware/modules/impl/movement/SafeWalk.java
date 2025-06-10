package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

@ModuleRegister(name = "SafeWalk", category = Category.Movement)
public class SafeWalk extends Module {
    @Subscribe
    private void onUpdate(EventUpdate e) {
    	assert mc.player != null;
        BlockPos pos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 1.0, mc.player.getPosZ());
        assert mc.world != null;
        mc.gameSettings.keyBindSneak.setPressed((MoveUtility.isBlockUnder(0.005f) || mc.world.getBlockState(pos).getBlock() == Blocks.AIR) && !(mc.player.isInWater() || mc.player.isInLava()));
        
    }
}
