package newcode.fun.control.cmd.impl;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.Manager;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.language.Translated;

@CmdInfo(name = "help", description = "Список команд чита", descriptionen = "List of cheat commands")
public class HelpCmd extends Cmd {
    @Override
    public void run(String[] args) throws Exception {
        for (Cmd cmd : Manager.COMMAND_MANAGER.getCommands()) {
            if (cmd instanceof HelpCmd) continue;
            ClientUtils.sendMessage(TextFormatting.WHITE + cmd.description + TextFormatting.GRAY +  " " + TextFormatting.RED + cmd.command);
        }
    }

    @Override
    public void error() {

    }
}
