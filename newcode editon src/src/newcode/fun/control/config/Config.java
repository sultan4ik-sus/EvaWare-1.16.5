package newcode.fun.control.config;

import com.google.gson.JsonObject;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.Manager;
import newcode.fun.ui.midnight.Style;
import newcode.fun.utils.ClientUtils;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class Config {

    private final File file;
    public String author;

    public Config(String name) {
        this.file = new File(ConfigManager.CONFIG_DIR, name + ".cfg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();

        JsonObject modulesObject = new JsonObject();
        Manager.FUNCTION_MANAGER.getFunctions().forEach(module -> modulesObject.add(module.name, module.save()));
        jsonObject.add("Features", modulesObject);

        JsonObject stylesObject = new JsonObject();
        for (Style style : Manager.STYLE_MANAGER.styles) {
            String colors = Arrays.stream(style.colors).mapToObj(String::valueOf).collect(Collectors.joining(","));
            JsonObject styleObject = new JsonObject();
            styleObject.addProperty("color", colors);
            styleObject.addProperty("selected", Manager.STYLE_MANAGER.getCurrentStyle() == style);
            stylesObject.add(style.name, styleObject);
        }
        jsonObject.add("styles", stylesObject);

        JsonObject otherObject = new JsonObject();
        if (!otherObject.has("author"))
            otherObject.addProperty("author", Manager.USER_PROFILE.getName());
        if (!otherObject.has("time"))
            otherObject.addProperty("time", System.currentTimeMillis());

        jsonObject.add("Others", otherObject);
        return jsonObject;
    }

    public void load(JsonObject object, String configuration, boolean start) {
        if (object.has("Features")) {
            JsonObject modulesObject = object.getAsJsonObject("Features");
            Manager.FUNCTION_MANAGER.getFunctions().forEach(module -> {
                if (!start && module.isState()) {
                    module.setState(false);
                }
                module.load(modulesObject.getAsJsonObject(module.name), start);
            });
        }

        try {
            if (object.has("styles")) {
                JsonObject stylesObject = object.getAsJsonObject("styles");
                for (Style style : Manager.STYLE_MANAGER.styles) {
                    if (stylesObject.has(style.name)) {
                        JsonObject styleObject = stylesObject.getAsJsonObject(style.name);
                        String colors = styleObject.get("color").getAsString();
                        boolean selected = styleObject.get("selected").getAsBoolean();

                        if (selected) {
                            Manager.STYLE_MANAGER.setCurrentStyle(style);
                        }

                        String[] colorArray = colors.split(",");
                        style.colors = Arrays.stream(colorArray).mapToInt(Integer::parseInt).toArray();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ClientUtils.sendMessage("������������ " + TextFormatting.GRAY + configuration + TextFormatting.RESET + " ���������.");
    }

    public File getFile() {
        return file;
    }
}