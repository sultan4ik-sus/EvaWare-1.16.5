package eva.ware.command.interfaces;

public interface Command {
    void execute(Parameters parameters);

    String name();

    String description();
}
