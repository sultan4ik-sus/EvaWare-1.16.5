package newcode.fun.control.cmd.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.language.Translated;

@CmdInfo(name = "vclip", description = "������������� ��� �����", descriptionen = "Teleports you up")
public class VClipCmd extends Cmd {

    @Override
    public void run(String[] args) throws Exception {
        if (args.length < 2) {
            ClientUtils.sendMessage(Translated.isRussian() ? "Specify teleportation height" : "������� ������ ������������");
            return;
        }

        try {
            double offset = Double.parseDouble(args[1]);
            Minecraft.getInstance().player.setPosition(mc.player.getPosX(), mc.player.getPosY() + Double.parseDouble(args[1]), mc.player.getPosZ());
            ClientUtils.sendMessage(Translated.isRussian() ? "You are teleported to " + TextFormatting.RED + offset + TextFormatting.WHITE + " blocks" : "�� ��������������� �� " + TextFormatting.RED + offset + TextFormatting.WHITE + " ������");
        } catch (NumberFormatException e) {
            ClientUtils.sendMessage(Translated.isRussian() ? "Please enter a valid number for height" : "������� ���������� ����� ��� ������");
        }
    }

    @Override
    public void error() {
    }
}
