package newcode.fun.control.cmd;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.impl.*;
import newcode.fun.control.Manager;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.language.Translated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdManager {
    public List<Cmd> cmdList = new ArrayList<>();
    public boolean isMessage;


    public void init() {
        cmdList.addAll(Arrays.asList(
                new RctCmd(),
                new HClipCmd(),
                new VClipCmd(),
                new HelpCmd(),
                new MacroCmd(),
                new BindCmd(),
                new ConfigCmd(),
                new FriendCmd(),
                new PanicCmd(),
                new StaffCmd(),
                new GPSCmd()
        ));
    }

    public void runCommands(String message) {
        if (message.startsWith(".")) {
            for (Cmd cmd : Manager.COMMAND_MANAGER.getCommands()) {
                if (message.startsWith("." + cmd.command)) {
                    try {
                        cmd.run(message.split(" "));
                    } catch (Exception ex) {
                        cmd.error();
                        ex.printStackTrace();
                    }
                    isMessage = true;
                    return;
                }
            }
            ClientUtils.sendMessage(Translated.isRussian() ? TextFormatting.RED + "Team not found!" : TextFormatting.RED + "Команда не наейдена!");
            ClientUtils.sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "List of all commands: .help" : TextFormatting.GRAY + "Список всех команд: .help");
            isMessage = true;

        } else {
            isMessage = false;
        }
    }

    public List<Cmd> getCommands() {
        return cmdList;
    }
}
