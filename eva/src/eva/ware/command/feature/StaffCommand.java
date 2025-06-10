package eva.ware.command.feature;

import eva.ware.Evaware;
import eva.ware.command.interfaces.*;
import eva.ware.ui.notify.impl.WarningNotify;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import eva.ware.command.api.CommandException;
import eva.ware.manager.StaffManager;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElseThrow();
        switch (commandType) {
            case "add" -> addStaffToList(parameters, logger);
            case "remove" -> removeStaffFromList(parameters, logger);
            case "clear" -> clearStaffList(logger);
            case "list" -> getStaffList(logger);
            default -> throw new CommandException(TextFormatting.RED
                    + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear, list");
        }
    }

    private void addStaffToList(Parameters parameters, Logger logger) {
        String staffName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя модератора для добавления/удаления."));

        if (staffName.equalsIgnoreCase(Minecraft.getInstance().player.getName().getString())) {
            logger.log(TextFormatting.RED + "Вы не можете добавить себя в список модераторов, как бы вам не хотелось");
            return;

        }

        if (StaffManager.isStaff(staffName)) {
            logger.log(TextFormatting.RED + "Этот игрок уже находится в списке.");
            return;
        }

        StaffManager.add(staffName);
        logger.log(TextFormatting.GRAY + "Вы успешно добавили " + TextFormatting.GRAY + staffName + TextFormatting.GRAY + " в список модераторов!");
    }

    private void removeStaffFromList(final Parameters parameters, Logger logger) {
        String staff = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя модератора для добавления/удаления."));
        if (StaffManager.isStaff(staff)) {
            StaffManager.remove(staff);
            logger.log(TextFormatting.GRAY + "Вы успешно удалили " + TextFormatting.GRAY + staff
                    + TextFormatting.GRAY + " из списка!");
            return;
        }
        logger.log(TextFormatting.RED + staff + " не найден в списке друзей");
    }

    private void getStaffList(Logger logger) {
        if (StaffManager.getStaffs().isEmpty()) {
            logger.log(TextFormatting.RED + "Список модераторов пустой.");
            return;
        }

        logger.log(TextFormatting.GRAY + "Список модераторов:");
        for (String friend : StaffManager.getStaffs()) {
            logger.log(TextFormatting.GRAY + friend);
        }
    }

    private void clearStaffList(Logger logger) {
        if (StaffManager.getStaffs().isEmpty()) {
            logger.log(TextFormatting.RED + "Список модераторов пустой.");
            return;
        }
        StaffManager.clear();
        logger.log(TextFormatting.GRAY + "Список модераторов очищен.");
    }

    @Override
    public String name() {
        return "staff";
    }

    @Override
    public String description() {
        return "Позволяет управлять списком с никами модерации";
    }

    @Override
    public List<String> adviceMessage() {
        Evaware.getInst().getNotifyManager().add(0, new WarningNotify("Ошибка в выполнения команды!", 1000));
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "staff add <name> - Добавить ник в список",
                commandPrefix + "staff remove <name> - Удалить ник из списка",
                commandPrefix + "staff list - Получить список ников модерации",
                commandPrefix + "staff clear - Очистить список ников модерации",
                "Пример: " + TextFormatting.RED + commandPrefix + "staff add Twoya_mama"
        );
    }
}