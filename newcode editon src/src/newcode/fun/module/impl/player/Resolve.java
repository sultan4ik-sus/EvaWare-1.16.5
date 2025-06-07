package newcode.fun.module.impl.player;

import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import newcode.fun.control.Manager;
import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;

@Annotation(name = "Resolve", type = TypeList.Player, desc = "Создаёт фейк лаги для ударов немного дальше")
public class Resolve extends Module {
    public BooleanOption onlyaura = new BooleanOption("Только с аурой", false);
    public BooleanOption onlyelytra = new BooleanOption("Только на элитре", false);

    public Resolve() {
        this.addSettings(onlyaura);
    }

    public boolean onEvent(Event event) {
        if (mc.player == null || mc.world == null) {
            return false;
        }

        if ((this.onlyelytra.get() && mc.player.isElytraFlying())) {
            return false;
        }

        if ((this.onlyaura.get() && (Manager.FUNCTION_MANAGER.auraFunction == null || Manager.FUNCTION_MANAGER.auraFunction.target == null)) || mc.player.ticksExisted < 20) {
            return false;
        }

        releaseResolver();
        return false;
    }

    public void releaseResolver() {
        if (mc.world == null) {
            return;
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof RemoteClientPlayerEntity) {
                try {
                    ((RemoteClientPlayerEntity) player).resolve();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}