package eva.ware.command.api;

import eva.ware.command.interfaces.*;
import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eva.ware.Evaware;
import eva.ware.modules.api.ModuleManager;
import eva.ware.modules.impl.misc.SelfDestruct;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class StandaloneCommandDispatcher implements CommandDispatcher, CommandProvider {
    private static final String DELIMITER = " ";
    final Prefix prefix;
    final ParametersFactory parametersFactory;
    final Logger logger;
    final Map<String, Command> aliasToCommandMap;


    public StandaloneCommandDispatcher(List<Command> commands, AdviceCommandFactory adviceCommandFactory, Prefix prefix, ParametersFactory parametersFactory, Logger logger) {
        this.prefix = prefix;
        this.parametersFactory = parametersFactory;
        this.logger = logger;
        aliasToCommandMap = commandsToAliasToCommandMap(commandsWithAdviceCommand(adviceCommandFactory, commands));
    }

    @Override
    public DispatchResult dispatch(String message) {
        ModuleManager moduleManager = Evaware.getInst().getModuleManager();
        SelfDestruct selfDestruct = moduleManager.getSelfDestruct();

        // Возвращает NOT_DISPATCHED если чит самоуничтожился
        if (selfDestruct.unhooked) {
            return DispatchResult.NOT_DISPATCHED;
        }

        String prefix = this.prefix.get();

        if (!message.startsWith(prefix)) {
            return DispatchResult.NOT_DISPATCHED;
        }

        String[] split = message.split(DELIMITER);
        String commandName = split[0].substring(prefix.length());
        Command command = aliasToCommandMap.get(commandName);

        try {
            String parameters = extractParametersFromMessage(message, split);
            command.execute(parametersFactory.createParameters(parameters, DELIMITER));
        } catch (Exception e) {
            handleCommandException(e, command);
        }
        return DispatchResult.DISPATCHED;
    }


    @Override
    public Command command(String alias) {
        return aliasToCommandMap.get(alias);
    }

    private Map<String, Command> commandsToAliasToCommandMap(List<Command> commands) {
        return commands.stream().flatMap(this::commandToWrappedCommandStream).collect(Collectors.toMap(FlatMapCommand::getAlias, FlatMapCommand::getCommand));
    }

    private Stream<FlatMapCommand> commandToWrappedCommandStream(Command command) {
        Stream<FlatMapCommand> wrappedCommandStream = Stream.of(new FlatMapCommand(command.name(), command));
        if (command instanceof MultiNamedCommand multiNamedCommand) {
            return Stream.concat(wrappedCommandStream, multiNamedCommand.aliases().stream().map(alias -> new FlatMapCommand(alias, command)));
        }
        return wrappedCommandStream;
    }

    private void handleCommandException(Exception e, Command command) {
        if (e instanceof CommandException) {
            logger.log(e.getMessage());
        } else {
            logger.log("Произошла ошибка во время выполнения команды!");
            String details = "Детали ошибки: ";
            String errorMessage;

            if (e instanceof NullPointerException) {
                errorMessage = "Такой команды не существует.";
            } else {
                errorMessage = e.getMessage();
            }

            logger.log(details.concat(errorMessage));
        }
        if (command instanceof CommandWithAdvice) {
            logger.log(String.format(TextFormatting.GRAY + "Введите %sadvice %s", prefix.get(), command.name()));
        }
    }

    private String extractParametersFromMessage(String message, String[] split) {
        return message.substring((split.length != 1 ? DELIMITER.length() : 0) + split[0].length());
    }

    private List<Command> commandsWithAdviceCommand(AdviceCommandFactory adviceCommandFactory, List<Command> commands) {
        List<Command> commandsWithAdvices = new ArrayList<>(commands);
        commandsWithAdvices.add(adviceCommandFactory.adviceCommand(this));
        return commandsWithAdvices;
    }

    @Value
    private static class FlatMapCommand {
        String alias;
        Command command;
    }
}
