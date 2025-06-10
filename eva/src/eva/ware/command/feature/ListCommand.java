package eva.ware.command.feature;

import eva.ware.command.interfaces.Command;
import eva.ware.command.interfaces.Logger;
import eva.ware.command.interfaces.MultiNamedCommand;
import eva.ware.command.interfaces.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListCommand implements Command, MultiNamedCommand {

    final List<Command> commands;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        logger.log(TextFormatting.RED + "advice" + TextFormatting.WHITE + " | " + TextFormatting.GRAY + "Помогает узнать как использовать команду");
        for (Command command : commands) {
            if (command == this) {
                continue;
            }

            String alias = ""; // потом доделаю

            logger.log(TextFormatting.RED + command.name() + TextFormatting.WHITE + " | " + TextFormatting.GRAY + command.description());
        }
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "Выдает список всех команд";
    }

    @Override
    public List<String> aliases() {
        return List.of("", "help");
    }
}
