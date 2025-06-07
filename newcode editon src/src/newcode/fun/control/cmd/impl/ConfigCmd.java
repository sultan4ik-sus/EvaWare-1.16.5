package newcode.fun.control.cmd.impl;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.config.ConfigManager;
import newcode.fun.control.Manager;
import newcode.fun.utils.language.Translated;

import java.io.IOException;


@CmdInfo(name = "cfg", description = "Через эту команду можно управлять конфигами", descriptionen = "Through this command you can manage configs")
public class ConfigCmd extends Cmd {

    @Override
    public void run(String[] args) throws Exception {
        if (args.length > 1) {
            ConfigManager configManager = Manager.CONFIG_MANAGER;
            switch (args[1]) {
                case "save" -> {
                    String configName = args[2];
                    configManager.saveConfiguration(configName);
                    sendMessage(Translated.isRussian() ? "Configuration " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " successfully saved." : "Конфигурация " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " успешно сохранена.");
                }
                case "load" -> {
                    String configName = args[2];
                    configManager.loadConfiguration(configName, false);
                }
                case "remove" -> {
                    String configName = args[2];
                    try {
                        configManager.deleteConfig(configName);
                        sendMessage(Translated.isRussian() ? "Configuration " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " successfully deleted." : "Конфигурация " + TextFormatting.GRAY + args[2] + TextFormatting.RESET + " успешно удалена.");
                    } catch (Exception e) {
                        sendMessage(Translated.isRussian() ? "Failed to delete configuration named " + configName + " maybe it's just not there." : "Не удалось удалить конфигурацию с именем " + configName + " возможно её просто нету.");
                    }
                }
                case "list" -> {
                    if (configManager.getAllConfigurations().isEmpty()) {
                        sendMessage(Translated.isRussian() ? "The list of configs is empty." : "Список конфигов пуст.");
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
        sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "Error in use" + TextFormatting.WHITE + ":" : TextFormatting.GRAY + "Ошибка в использовании" + TextFormatting.WHITE + ":");
        sendMessage(TextFormatting.WHITE + "." + "cfg load " + TextFormatting.GRAY + "<name>");
        sendMessage(TextFormatting.WHITE + "." + "cfg save " + TextFormatting.GRAY + "<name>");
        sendMessage(TextFormatting.WHITE + "." + "cfg remove " + TextFormatting.GRAY + "<name>");
        sendMessage(TextFormatting.WHITE + "." + "cfg list" + TextFormatting.GRAY
                + (Translated.isRussian() ? " - show list of configs" : " - показать список конфигов"));
        sendMessage(TextFormatting.WHITE + "." + "cfg dir" + TextFormatting.GRAY
                + (Translated.isRussian() ? " - open the config folder" : " - открыть папку с конфигами"));
    }
}
