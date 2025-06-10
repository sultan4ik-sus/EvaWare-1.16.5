package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.EventMotion;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;

@ModuleRegister(name = "AirJump", category = Category.Movement)
public class AirJump extends Module {
	
	private ModeSetting mode = new ModeSetting("Обход", "Matrix", "Default", "Matrix");
	
	public AirJump() {
		addSettings(mode);
	}
	
	@Subscribe
	public void onUpdate(EventMotion e) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;
		if (mode.is("Default")) {
			mc.player.onGround = true;
		}

		if (mode.is("Matrix")) {
			if (!mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0.5, 0, 0.5).offset(0, -1, 0)).toList().isEmpty() && mc.player.ticksExisted % 2 == 0) {
				mc.player.fallDistance = 0;
				mc.player.jumpTicks = 0;
				e.setOnGround(true);
				mc.player.onGround = true;
			}
		}
	}
}
