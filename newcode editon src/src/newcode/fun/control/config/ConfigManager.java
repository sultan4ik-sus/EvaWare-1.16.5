package newcode.fun.control.config;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {

    public static final File CONFIG_DIR = new File("C:\\NewCode\\game\\NewCode\\config");
    private final File autoCfgDir = new File("C:\\NewCode\\game\\NewCode\\config\\default.cfg");
    private static final JsonParser jsonParser = new JsonParser();

    public void init() throws Exception {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
        if (!autoCfgDir.exists()) {
            autoCfgDir.createNewFile();
        }
        if (autoCfgDir.exists()) {
            // Проверим, что файл не пустой
            if (autoCfgDir.length() > 0) {
                loadConfiguration("default", true);
            } else {
                // Если файл пустой, перезаписываем его
                saveConfiguration("default");
            }
        }
    }

    public static void saveLanguageSetting(boolean isRussian) {
        try {
            File langFile = new File("C:\\NewCode\\game\\NewCode", "language.json");
            JsonObject languageConfig = new JsonObject();
            languageConfig.addProperty("isRussian", isRussian);

            writeJsonToFile(langFile, languageConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean loadLanguageSetting() {
        try {
            File langFile = new File("C:\\NewCode\\game\\NewCode", "language.json");
            if (langFile.exists()) {
                JsonElement element = readFileAsJson(langFile);
                if (element != null && element.isJsonObject()) {
                    JsonObject languageConfig = element.getAsJsonObject();
                    return languageConfig.get("isRussian").getAsBoolean();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getAllConfigurations() {
        List<String> configurations = new ArrayList<>();
        File[] files = CONFIG_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".cfg")) {
                    String configName = file.getName().substring(0, file.getName().lastIndexOf(".cfg"));
                    configurations.add(configName);
                }
            }
        }
        return configurations;
    }

    public void loadConfiguration(String configuration, boolean start) {
        Config config = findConfig(configuration);
        if (config == null) {
            return;
        }

        try {
            JsonElement element = readFileAsJson(config.getFile());
            if (element != null) {
                JsonObject object = element.getAsJsonObject();

                config.load(object, configuration, start);
            } else {
                saveConfiguration(configuration);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static JsonElement readFileAsJson(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return jsonParser.parse(content);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static void writeJsonToFile(File file, JsonElement jsonElement) {
        Thread thread = new Thread(() -> {
            try {
                String content = new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
                Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void saveConfiguration(String configuration) {
        Config config = findConfig(configuration);
        if (config == null) {
            config = new Config(configuration);
        }

        writeJsonToFile(config.getFile(), config.save());
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        if (new File(CONFIG_DIR, configName + ".cfg").exists())
            return new Config(configName);

        return null;
    }

    public void deleteConfig(String configName) {
        if (configName == null)
            return;
        Config config = findConfig(configName);
        if (config != null) {
            File file = config.getFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.out.println("Не удалось удалить конфиг " + file.getAbsolutePath());
                }
            }
        }
    }
}
