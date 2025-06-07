package newcode.fun.control.cmd.impl;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.cmd.macro.Macro;
import newcode.fun.control.Manager;
import newcode.fun.utils.math.KeyMappings;

@CmdInfo(name = "macro", description = "���� ����������� ���������� ������� ��� ������� ������", descriptionen = "Allows you to send a command when a button is pressed")
public class MacroCmd extends Cmd {

    @Override
    public void run(String[] args) throws Exception {
        if (args.length > 1) {


            switch (args[1]) {
                case "add" -> {
                    String buttonName = args[2].toUpperCase();
                    Integer keycode = null;

                    try {
                        keycode = KeyMappings.keyMap.get(buttonName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (keycode != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 3; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }
                        Manager.MACRO_MANAGER.addMacros(new Macro(sb.toString(), keycode));
                        sendMessage(TextFormatting.GREEN + "�������� ������ ��� ������" + TextFormatting.RED + " \""
                                + args[2].toUpperCase() + TextFormatting.RED + "\" " + TextFormatting.WHITE + "� �������� "
                                + TextFormatting.RED + sb);
                    } else {
                        sendMessage("�� ������� ������ � ��������� " + buttonName);
                    }
                }
                case "clear" -> {

                    if (Manager.MACRO_MANAGER.getMacros().isEmpty()) {
                        sendMessage(TextFormatting.RED + "������ �������� ����");
                    } else {
                        sendMessage(TextFormatting.GREEN + "������ �������� " + TextFormatting.WHITE + "������� ������");
                        Manager.MACRO_MANAGER.getMacros().clear();
                        Manager.MACRO_MANAGER.updateFile();
                    }
                }
                case "remove" -> {
                    String buttonName = args[2].toUpperCase();
                    Integer keycode = null;

                    try {
                        keycode = KeyMappings.keyMap.get(buttonName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (keycode != null) {
                        sendMessage(TextFormatting.GREEN + "������ " + TextFormatting.WHITE + "��� ������ � ������ "
                                + TextFormatting.RED + "\"" + args[2] + "\"");

                        Manager.MACRO_MANAGER.deleteMacro(keycode);
                    } else {
                        sendMessage("�� ������� ������ � ��������� " + buttonName);

                    }
                }
                case "list" -> {
                    if (Manager.MACRO_MANAGER.getMacros().isEmpty()) {
                        sendMessage("������ �������� ����");
                    } else {
                        sendMessage(TextFormatting.GREEN + "������ ��������: ");
                        Manager.MACRO_MANAGER.getMacros()
                                .forEach(macro -> sendMessage(TextFormatting.WHITE + "�������: " + TextFormatting.RED
                                        + macro.getMessage() + TextFormatting.WHITE + ", ������: " + TextFormatting.RED
                                        + macro.getKey()));
                    }
                }
            }
        } else {
            error();
        }
    }

    @Override
    public void error() {
        sendMessage(TextFormatting.GRAY + "������ � �������������" + TextFormatting.WHITE + ":");
        sendMessage(TextFormatting.WHITE + "." + "macro add " + TextFormatting.GRAY + "<"
                + TextFormatting.RED + "key" + TextFormatting.GRAY + ">" + TextFormatting.GRAY + " message");
        sendMessage(TextFormatting.WHITE + "." + "macro remove " + TextFormatting.GRAY + "<"
                + TextFormatting.RED + "key" + TextFormatting.GRAY + ">");
        sendMessage(TextFormatting.WHITE + "." + "macro list");
        sendMessage(TextFormatting.WHITE + "." + "macro clear");
    }
}
