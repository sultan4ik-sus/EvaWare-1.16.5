package newcode.fun.module.impl.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.*;
import net.minecraft.tileentity.*;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ColorSetting;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;

import java.util.Iterator;

@Annotation(
        name = "BlockEsp",
        type = TypeList.Render, desc = "Подсвечивает определённые блоки"
)
public class BlockEsp extends Module {
    private final MultiBoxSetting blocksSelection = new MultiBoxSetting("Выбрать блоки", new BooleanOption[]{new BooleanOption("Сундуки", true), new BooleanOption("Эндер Сундуки", false), new BooleanOption("Спавнера", true), new BooleanOption("Шалкера", false), new BooleanOption("Кровати", false), new BooleanOption("Вагонетка", false), new BooleanOption("Бочка", false), new BooleanOption("Головы", false)});

    public ColorSetting colorchest = new ColorSetting("Сундуков", ColorUtils.rgba(255, 255, 255, 255));
    public ColorSetting colorenderchest = new ColorSetting("Эндер сундуков", ColorUtils.rgba(255, 255, 255, 255));
    public ColorSetting colorspawner = new ColorSetting("Спавнеров", ColorUtils.rgba(255, 255, 255, 255));
    public ColorSetting colorshulker = new ColorSetting("Шалкеров", ColorUtils.rgba(255, 255, 255, 255));
    public ColorSetting colorbed = new ColorSetting("Кровати", ColorUtils.rgba(255, 255, 255, 255));
    public ColorSetting colormin = new ColorSetting("Вагонетки", ColorUtils.rgba(255, 255, 255, 255));
    public ColorSetting colorbarrels = new ColorSetting("Бочки", ColorUtils.rgba(255, 255, 255, 255));
    public ColorSetting hade = new ColorSetting("Головы", ColorUtils.rgba(255, 255, 255, 255));

    public BlockEsp() {
        this.addSettings(new Setting[]{this.blocksSelection, colorchest, colorenderchest, colorspawner, colorshulker, colorbed, colormin, colorbarrels, hade});
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventRender e) {
            if (e.isRender3D()) {
                Iterator<TileEntity> tileEntityIterator = mc.world.loadedTileEntityList.iterator();

                while (tileEntityIterator.hasNext()) {
                    TileEntity tileEntity = tileEntityIterator.next();

                    if (tileEntity instanceof ChestTileEntity && this.blocksSelection.get(0)) {
                        RenderUtils.Render3D.drawBlockBox(tileEntity.getPos(), colorchest.get());
                    }

                    if (tileEntity instanceof EnderChestTileEntity && this.blocksSelection.get(1)) {
                        RenderUtils.Render3D.drawBlockBox(tileEntity.getPos(), colorenderchest.get());
                    }

                    if (tileEntity instanceof MobSpawnerTileEntity && this.blocksSelection.get(2)) {
                        RenderUtils.Render3D.drawBlockBox(tileEntity.getPos(), colorspawner.get());
                    }

                    if (tileEntity instanceof ShulkerBoxTileEntity && this.blocksSelection.get(3)) {
                        RenderUtils.Render3D.drawBlockBox(tileEntity.getPos(), colorshulker.get());
                    }

                    if (tileEntity instanceof BedTileEntity && this.blocksSelection.get(4)) {
                        RenderUtils.Render3D.drawBlockBox(tileEntity.getPos(), colorbed.get());
                    }

                    if (tileEntity instanceof BarrelTileEntity && this.blocksSelection.get(6)) {
                        RenderUtils.Render3D.drawBlockBox(tileEntity.getPos(), colorbarrels.get());
                    }

                    if (tileEntity instanceof SkullTileEntity && this.blocksSelection.get(7)) {
                        RenderUtils.Render3D.drawBlockBoxSkull(tileEntity.getPos(), hade.get());
                    }
                }

                Iterator<Entity> entityIterator = mc.world.getAllEntities().iterator();

                while (entityIterator.hasNext()) {
                    Entity entity = entityIterator.next();

                    if ((entity instanceof MinecartEntity || entity instanceof ChestMinecartEntity || entity instanceof TNTMinecartEntity || entity instanceof FurnaceMinecartEntity || entity instanceof HopperMinecartEntity || entity instanceof CommandBlockMinecartEntity) && this.blocksSelection.get(5)) {
                        RenderUtils.Render3D.drawBlockBox(entity.getPosition(), colormin.get());
                    }
                }
            }
        }
        return false;
    }
}