package eva.ware.command.interfaces;

import eva.ware.command.api.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
