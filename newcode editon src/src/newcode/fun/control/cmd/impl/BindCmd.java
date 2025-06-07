package newcode.fun.control.cmd.impl;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.module.api.Module;
import newcode.fun.utils.language.Translated;
import org.lwjgl.glfw.GLFW;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.Manager;
import newcode.fun.utils.math.KeyMappings;


@CmdInfo(name = "bind", description = "Позволяет забиндить модуль на определенную клавишу", descriptionen = "Allows you to bind a module to a specific key")
public class BindCmd extends Cmd {

    @Override
    public void run(String[] args) throws Exception {
        try {
            if (args.length >= 2) {
                switch (args[1].toLowerCase()) {
                    case "list" -> listBoundKeys();
                    case "clear" -> clearAllBindings();
                    case "add" -> {
                        if (args.length >= 4) {
                            addKeyBinding(args[2], args[3]);
                        } else {
                            error();
                        }
                    }
                    case "remove" -> {
                        if (args.length >= 2) {
                            removeKeyBinding(args[2]);
                        } else {
                            error();
                        }
                    }
                    default -> error();
                }
            } else {
                error();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для вывода списка модулей с привязанными клавишами
     */
    private void listBoundKeys() {
        sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "List of all modules with key bindings:" : TextFormatting.GRAY + "Список всех модулей с привязанными клавишами:");
        for (Module f : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (f.bind == 0) continue;
            sendMessage(f.name + " [" + TextFormatting.GRAY + (GLFW.glfwGetKeyName(f.bind, -1) == null ? "" : GLFW.glfwGetKeyName(f.bind, -1)) + TextFormatting.RESET + "]");
        }
    }

    /**
     * Метод для очистки всех привязок клавиш
     */
    private void clearAllBindings() {
        for (Module f : Manager.FUNCTION_MANAGER.getFunctions()) {
            f.bind = 0;
        }
        sendMessage(Translated.isRussian() ? TextFormatting.GREEN + "All keys have been unlinked from modules" : TextFormatting.GREEN + "Все клавиши были отвязаны от модулей");
    }

    /**
     * Метод для добавления привязки клавиши к модулю
     *
     * @param moduleName имя модуля
     * @param keyName    название клавиши
     */
    private void addKeyBinding(String moduleName, String keyName) {
        Integer key = null;

        try {
            key = KeyMappings.keyMap.get(keyName.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Module module = Manager.FUNCTION_MANAGER.get(moduleName);
        if (key != null) {
            if (module != null) {
                module.bind = key;
                sendMessage(Translated.isRussian() ? "Key" + TextFormatting.GRAY + keyName + TextFormatting.WHITE + " was bound to the module " + TextFormatting.RED + module.name : "Клавиша" + TextFormatting.GRAY + keyName + TextFormatting.WHITE + " была привязана к модулю " + TextFormatting.RED + module.name);
            } else {
                sendMessage(Translated.isRussian() ? "Module " + moduleName + " was not found" : "Модуль " + moduleName + " не был найден");
            }
        } else {
            sendMessage(Translated.isRussian() ? "Key " + keyName + " was not found!" : "Клавиша " + keyName + " не была найдена!");
        }
    }

    /**
     * Метод для удаления привязки клавиши
     *
     * @param moduleName имя модуля
     */
    private void removeKeyBinding(String moduleName) {
        for (Module f : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (f.name.equalsIgnoreCase(moduleName)) {
                f.bind = 0;
                sendMessage(Translated.isRussian() ? "Key " + TextFormatting.RESET + " was unlinked from the module " + TextFormatting.GRAY + f.name : "Клавиша " + TextFormatting.RESET + " была отвязана от модуля " + TextFormatting.GRAY + f.name);
            }
        }
    }

    /**
     * Метод для обработки ошибки неверного синтаксиса команды
     */
    @Override
    public void error() {
        sendMessage(Translated.isRussian() ? TextFormatting.WHITE + "Invalid command syntax. " + TextFormatting.GRAY + "Use:" : TextFormatting.WHITE + "Неверный синтаксис команды. " + TextFormatting.GRAY + "Используйте:");
        sendMessage(TextFormatting.WHITE + ".bind add " + TextFormatting.DARK_GRAY + "<name> <key>");
        sendMessage(TextFormatting.WHITE + ".bind remove " + TextFormatting.DARK_GRAY + "<name>");
        sendMessage(TextFormatting.WHITE + ".bind list");
        sendMessage(TextFormatting.WHITE + ".bind clear");
    }
}