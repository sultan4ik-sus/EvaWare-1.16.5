package eva.ware.command.api;

import lombok.Value;

@Value
public class CommandException extends RuntimeException {
    String message;
}
