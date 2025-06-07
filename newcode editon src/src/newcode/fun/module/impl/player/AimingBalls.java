package newcode.fun.module.impl.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.SkullItem;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;

import java.util.Iterator;

@Annotation(
        name = "AimingBalls", type = TypeList.Player
)
public class AimingBalls extends Module {
    public MultiBoxSetting element = new MultiBoxSetting("Наводится на",
            new BooleanOption("Шары", true),
            new BooleanOption("Элитры", true));
    public AimingBalls() {
        addSettings(element);
    }


    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            Iterator var2 = mc.world.getAllEntities().iterator();

            while(var2.hasNext()) {
                Entity entity = (Entity)var2.next();
                if ((element.get(0) && entity instanceof ItemEntity && ((ItemEntity)entity).getItem().getItem() instanceof SkullItem)) {
                    mc.player.rotationYaw = this.rotations(entity)[0];
                    mc.player.rotationPitch = this.rotations(entity)[1];
                }
                if ((element.get(1) && entity instanceof ItemEntity && ((ItemEntity)entity).getItem().getItem() instanceof ElytraItem)) {
                    mc.player.rotationYaw = this.rotations(entity)[0];
                    mc.player.rotationPitch = this.rotations(entity)[1];
                }
            }
        }

        return false;
    }

    public float[] rotations(Entity entity) {
        double x = entity.getPosX() - mc.player.getPosX();
        double y = entity.getPosY() - mc.player.getPosY() - 1.5;
        double z = entity.getPosZ() - mc.player.getPosZ();
        double u = (double) MathHelper.sqrt(x * x + z * z);
        float u2 = (float)(MathHelper.atan2(z, x) * 57.29577951308232 - 90.0);
        float u3 = (float)(-MathHelper.atan2(y, u) * 57.29577951308232);
        return new float[]{u2, u3};
    }
}
