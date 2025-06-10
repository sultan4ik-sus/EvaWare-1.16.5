package eva.ware.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

@ModuleRegister(name = "EntityBox", category = Category.Combat)
public class EntityBox extends Module {
    public final SliderSetting size = new SliderSetting("Размер", 0.2f, 0.f, 3.f, 0.05f);
    public final CheckBoxSetting visible = new CheckBoxSetting("Видимые", false);
    public EntityBox() {
        addSettings(size,visible);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!visible.getValue() || mc.player == null) {
            return;
        }

        float sizeMultiplier = this.size.getValue() * 2.5F;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isNotValid(player)) {
                player.setBoundingBox(calculateBoundingBox(player, sizeMultiplier));
            }
        }
    }

    private boolean isNotValid(PlayerEntity player) {
        return player == mc.player || !player.isAlive();
    }

    private AxisAlignedBB calculateBoundingBox(Entity entity, float size) {
        double minX = entity.getPosX() - size;
        double minY = entity.getBoundingBox().minY;
        double minZ = entity.getPosZ() - size;
        double maxX = entity.getPosX() + size;
        double maxY = entity.getBoundingBox().maxY;
        double maxZ = entity.getPosZ() + size;

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
