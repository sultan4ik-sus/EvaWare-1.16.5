package eva.ware.command.feature;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.command.api.CommandException;
import eva.ware.command.interfaces.*;
import eva.ware.events.EventRender2D;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.ModuleManager;
import eva.ware.modules.impl.misc.SelfDestruct;
import eva.ware.ui.notify.impl.WarningNotify;
import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.impl.DecelerateAnimation;
import eva.ware.utils.client.IMinecraft;
import eva.ware.utils.render.engine2d.ProjectionUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TextFormatting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WayCommand implements Command, CommandWithAdvice, MultiNamedCommand, IMinecraft {
    final Prefix prefix;
    final Logger logger;
    private final AnimationUtility alpha = new DecelerateAnimation(255, 255);
    private final Map<String, Vector3i> waysMap = new LinkedHashMap<>();

    public WayCommand(Prefix prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
        Evaware.getInst().getEventBus().register(this);
    }

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "add" -> addGPS(parameters);
            case "remove" -> removeGPS(parameters);
            case "clear" -> {
                waysMap.clear();
                logger.log("Все пути были удалены!");
            }
            case "list" -> {
                logger.log("Список путей:");

                for (String s : waysMap.keySet()) {
                    logger.log("- " + s + " " + waysMap.get(s));
                }
            }
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear");
        }
    }

    private void addGPS(Parameters param) {
        String name = param.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя координаты!"));
        int x = param.asInt(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите первую координату!"));

        int y = param.asInt(3)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите вторую координату!"));

        int z = param.asInt(4)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите третью координату!"));

        Vector3i vec = new Vector3i(x, y, z);
        waysMap.put(name, vec);
        logger.log("Путь " + name + " был добавлен!");
    }

    private void removeGPS(Parameters param) {
        String name = param.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя координаты!"));

        waysMap.remove(name);
        logger.log("Путь " + name + " был удалён!");
    }

    @Override
    public String name() {
        return "way";
    }

    @Override
    public String description() {
        return "Позволяет работать с координатами путей";
    }

    @Override
    public List<String> adviceMessage() {
        Evaware.getInst().getNotifyManager().add(0, new WarningNotify("Ошибка в выполнения команды!", 1000));
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "way add <имя, x, y, z> - Проложить путь к WayPoint'у",
                commandPrefix + "way remove <имя> - Удалить WayPoint",
                commandPrefix + "way list - Список WayPoint'ов",
                commandPrefix + "way clear - Очистить список WayPoint'ов",
                "Пример: " + TextFormatting.RED + commandPrefix + "way add АирДроп 1000 100 1000"
        );
    }
    Vector3i vec3i;
    Vector3d vec3d;
    Vector2f vec2f;
    int distance;

    @Subscribe
    public void onUdate(EventUpdate e) {
    }

    @Subscribe
    private void onDisplay(EventRender2D e) {
        ModuleManager moduleManager = Evaware.getInst().getModuleManager();
        SelfDestruct selfDestruct = moduleManager.getSelfDestruct();

        if (selfDestruct.unhooked) {
            return;
        }
        if (waysMap.isEmpty()) {
            return;
        }
        for (String name : waysMap.keySet()) {
            vec3i = waysMap.get(name);

            vec3d = new Vector3d(
                    vec3i.getX() + 0.5,
                    vec3i.getY() + 0.5,
                    vec3i.getZ() + 0.5
            );

            vec2f = ProjectionUtility.project(vec3d.x, vec3d.y, vec3d.z);
            distance = (int) Minecraft.getInstance().player.getPositionVec().distanceTo(vec3d);
            String text = name + " (" + distance + "M)";

            float textWith = Fonts.montserrat.getWidth(text, 8);
            float fontHeight = Fonts.montserrat.getHeight(8);

            float posX = vec2f.x - textWith / 2;
            float posY = vec2f.y - fontHeight / 2;

            float padding = 2;

//            DisplayUtils.drawRectW(posX - padding, posY - padding, padding + textWith + padding, padding + fontHeight + padding, ColorUtils.rgba(20, 20, 20, 128));
            ClientFonts.icon[30].drawString(e.getMatrixStack(), "A", posX + textWith / 2 - ClientFonts.icon[30].getWidth("A") / 2, posY - 16, ColorUtility.reAlphaInt(-1, (int) (255)));
            Fonts.montserrat.drawText(e.getMatrixStack(), text, posX, posY, ColorUtility.setAlpha(-1, (int) (255)), 8);
        }
    }

    @Override
    public List<String> aliases() {
        return List.of("w");
    }
}
