package newcode.fun.module.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.IMinecraft;

@Annotation(name = "NoEntityTrace",
        type = TypeList.Combat, desc = "Удаляет хитбокс всех энтити"
)
public class NoPlayerInteract extends Module {

    public final SliderSetting size = new SliderSetting("Размер",  -5f, 0.f, 3.f, 0.05f);
    public final BooleanOption invisible = new BooleanOption("Невидимые", true);

    public NoPlayerInteract() {
        addSettings();
    }

    @Override
    public boolean onEvent(final Event event) {
        handleEvent(event);
        return false;
    }

    private void handleEvent(Event event) {
        if (!(event instanceof EventRender && ((EventRender) event).isRender3D()))
            return;

        adjustBoundingBoxesForPlayers();
    }

    private void adjustBoundingBoxesForPlayers() {
        for (PlayerEntity player : IMinecraft.mc.world.getPlayers()) {

            if (shouldSkipPlayer(player))
                continue;

            float sizeMultiplier = this.size.getValue().floatValue() * -2.5F;
            setBoundingBox(player, sizeMultiplier);
        }
    }

    private boolean shouldSkipPlayer(PlayerEntity player) {
        return player == IMinecraft.mc.player || !player.isAlive();
    }

    private void setBoundingBox(Entity entity, float size) {
        AxisAlignedBB newBoundingBox = calculateBoundingBox(entity, size);
        entity.setBoundingBox(newBoundingBox);
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
