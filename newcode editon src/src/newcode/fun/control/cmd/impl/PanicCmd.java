package newcode.fun.control.cmd.impl;

import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.Manager;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.language.Translated;

@CmdInfo(name = "panic", description = "Выключает все функции чита", descriptionen = "Disables all cheat functions")

public class PanicCmd extends Cmd {
    @Override
    public void run(String[] args) throws Exception {
        if (args.length == 1) {
            Manager.FUNCTION_MANAGER.getFunctions().stream().filter(function -> function.state).forEach(function -> function.setState(false));
            ClientUtils.sendMessage(Translated.isRussian() ? "Turned off all modules!" : "Выключил все модули!");
        } else error();
    }

    @Override
    public void error() {

    }
}
