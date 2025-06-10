package eva.ware.command.api;

import eva.ware.command.interfaces.AdviceCommandFactory;
import eva.ware.command.interfaces.CommandProvider;
import eva.ware.command.interfaces.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommandFactoryImpl implements AdviceCommandFactory {

    final Logger logger;
    @Override
    public AdviceCommand adviceCommand(CommandProvider commandProvider) {
        return new AdviceCommand(commandProvider, logger);
    }
}
