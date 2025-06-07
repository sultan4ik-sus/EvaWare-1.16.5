package newcode.fun.control.cmd.impl;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.config.ConfigManager;
import newcode.fun.control.Manager;
import newcode.fun.utils.language.Translated;

import java.io.IOException;


@CmdInfo(name = "cfg", description = "����� ��� ������� ����� ��������� ���������", descriptionen = "Through this command you can manage configs")
public class ConfigCmd extends Cmd {

    @Override
    public void run(String[] args) throws Exception {
        if (args.length > 1) {
            ConfigManager configManager = Manager.CONFIG_MANAGER;
            switch (args[1]) {
                case "save" -> {
                    String configName = args[2];
                    configManager.saveConfiguration(configName);
                    sendMessage(Translated.isRussian() ? "Configuration " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " successfully saved." : "������������ " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " ������� ���������.");
                }
                case "load" -> {
                    String configName = args[2];
                    configManager.loadConfiguration(configName, false);
                }
                case "remove" -> {
                    String configName = args[2];
                    try {
                        configManager.deleteConfig(configName);
                        sendMessage(Translated.isRussian() ? "Configuration " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " successfully deleted." : "������������ " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " ������� �������.");
                    } catch (Exception e) {
                        sendMessage(Translated.isRussian() ? "Failed to delete configuration named " + configName + " maybe it's just not there." : "�� ������� ������� ������������ � ������ " + configName + " �������� � ������ ����.");
                    }
                }
                case "list" -> {
                    if (configManager.getAllConfigurations().isEmpty()) {
                        sendMessage(Translated.isRussian() ? "The list of configs is empty." : "������ �������� ����.");
                        return;
                    }
                    for (String s : configManager.getAllConfigurations()) {
                        sendMessage(s.replace(".cfg", ""));
                    }
                }
                case "dir" -> {
                    try {
                        Runtime.getRuntime().exec("explorer " + ConfigManager.CONFIG_DIR.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            error();
        }
    }

    @Override
    public void error() {
        sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "Error in use" + TextFormatting.WHITE + ":" : TextFormatting.GRAY + "������ � �������������" + TextFormatting.WHITE + ":");
        sendMessage(TextFormatting.WHITE + "." + "cfg load " + TextFormatting.GRAY + "<name>");
        sendMessage(TextFormatting.WHITE + "." + "cfg save " + TextFormatting.GRAY + "<name>");
        sendMessage(TextFormatting.WHITE + "." + "cfg remove " + TextFormatting.GRAY + "<name>");
        sendMessage(TextFormatting.WHITE + "." + "cfg list" + TextFormatting.GRAY
                + (Translated.isRussian() ? " - show list of configs" : " - �������� ������ ��������"));
        sendMessage(TextFormatting.WHITE + "." + "cfg dir" + TextFormatting.GRAY
                + (Translated.isRussian() ? " - open the config folder" : " - ������� ����� � ���������"));
    }
}
