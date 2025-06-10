package eva.ware.command.api;

import eva.ware.command.interfaces.Logger;

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("message = " + message);
    }
}
