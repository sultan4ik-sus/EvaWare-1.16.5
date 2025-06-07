package newcode.fun.control.cmd;

import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.language.Translated;

public abstract class Cmd implements IMinecraft {
    public final String command;
    public final String description;

    public Cmd() {
        command = this.getClass().getAnnotation(CmdInfo.class).name();
        if (Translated.isRussian()) {
            description = this.getClass().getAnnotation(CmdInfo.class).descriptionen();
        } else {
            description = this.getClass().getAnnotation(CmdInfo.class).description();
        }
    }

    public abstract void run(String[] args) throws Exception;
    public abstract void error();

    public void sendMessage(String message) {
        ClientUtils.sendMessage(message);
    }
}
