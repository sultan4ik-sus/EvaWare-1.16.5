package newcode.fun.utils.language;

import newcode.fun.control.config.ConfigManager;

public class Translated {
    private static boolean ru = false;

    public static boolean isRussian() {
        return ru;
    }

    public static void setRussian(boolean value) {
        ru = value;
        ConfigManager.saveLanguageSetting(ru);
    }
}
