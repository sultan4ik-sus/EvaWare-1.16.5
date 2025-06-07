package newcode.fun.control.cmd.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.language.Translated;

@CmdInfo(name = "hclip", description = "������������� ��� ������", descriptionen = "Teleports you forward")
public class HClipCmd extends Cmd {
    @Override
    public void run(String[] args) throws Exception {
        if (args.length < 2) {
            ClientUtils.sendMessage(Translated.isRussian() ? "Specify teleportation distance" : "������� ��������� ������������");
            return;
        }

        try {
            double distance = Double.parseDouble(args[1]);
            Vector3d tp = Minecraft.getInstance().player.getLook(1F).scale(distance);
            Minecraft.getInstance().player.setPosition(
                    Minecraft.getInstance().player.getPosX() + tp.x,
                    Minecraft.getInstance().player.getPosY(),
                    Minecraft.getInstance().player.getPosZ() + tp.z
            );
            ClientUtils.sendMessage(Translated.isRussian() ? "You are teleported to " + TextFormatting.RED + distance + TextFormatting.WHITE + " blocks forward" : "�� ��������������� �� " + TextFormatting.RED + distance + TextFormatting.WHITE + " ������ ������");
        } catch (NumberFormatException e) {
            ClientUtils.sendMessage(Translated.isRussian() ? "Please enter a valid number for the distance" : "������� ���������� ����� ��� ���������");
        }
    }

    @Override
    public void error() {
    }
}
