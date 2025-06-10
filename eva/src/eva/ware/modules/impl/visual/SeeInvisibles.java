package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import net.minecraft.entity.player.PlayerEntity;

@ModuleRegister(name = "SeeInvisibles", category = Category.Visual)
public class SeeInvisibles extends Module {


    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isInvisible()) {
                player.setInvisible(false);
            }
        }
    }
}
