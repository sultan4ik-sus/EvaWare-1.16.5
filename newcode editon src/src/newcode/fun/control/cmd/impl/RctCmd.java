package newcode.fun.control.cmd.impl;

import net.minecraft.scoreboard.ScoreObjective;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;

@CmdInfo(name = "rct", description = "Реконектится на анахрий FunTime", descriptionen = "Reconnects to FunTime anachry")
public class RctCmd extends Cmd {

    @Override
    public void run(String[] args) throws Exception {
        String anca = "";
        for (ScoreObjective team : mc.world.getScoreboard().getScoreObjectives()) {
            String an = team.getDisplayName().getString();
            if (an.contains("Анархия-")) {
                anca = an.split("Анархия-")[1];
                mc.player.sendChatMessage("/hub");
                break;
            }
        }
        String finalAnca = anca;
        new Thread(() -> {
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mc.player.sendChatMessage("/an" + finalAnca);
        }).start();

    }

    @Override
    public void error() {
    }

}