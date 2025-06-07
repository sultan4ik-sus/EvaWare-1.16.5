package newcode.fun.module.api;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.Manager;
import newcode.fun.module.TypeList;
import newcode.fun.module.impl.player.ClientSounds;
import newcode.fun.module.settings.Configurable;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.*;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.SoundUtils;


public abstract class Module extends Configurable implements IMinecraft {
    private final Annotation info = this.getClass().getAnnotation(Annotation.class);
    public TypeList category;
    public String name, desc;
    public int bind;

    @Getter
    public boolean state;

    public Module() {
        initializeProperties();
    }

    public Module(String name, TypeList category) {
        this.name = name;
        this.category = category;
        state = false;
        bind = 0;
        init();
    }

    public void init() {
    }

    private void initializeProperties() {
        name = info.name();
        category = info.type();
        state = false;
        bind = info.key();
        desc = info.desc();
    }

    public String getDescription() {
        Annotation annotation = getClass().getAnnotation(Annotation.class);
        if (annotation != null) {
            return annotation.desc();
        }
        return "Описание модуля отсутствует";
    }

    public void setStateNotUsing(boolean enabled) {
        state = enabled;
    }

    public void setState(boolean enabled) {
        if (mc.player == null || mc.world == null) return;

        if (!enabled) {
            onDisable();
        } else {
            onEnable();
        }

        state = enabled;
    }

    public void toggle() {
        if (mc.player == null || mc.world == null) {
            return;
        }
        this.state = !state;
        if (!state)
            onDisable();
        else
            onEnable();
        Manager.NOTIFICATION_MANAGER.add(name + " " + (state ? TextFormatting.WHITE + "enabled!" : TextFormatting.WHITE + "disabled!"), "Function Debug",3);


        ClientSounds clientSounds = Manager.FUNCTION_MANAGER.clientSounds;

        if (clientSounds.state) {
            if (!state) {
                SoundUtils.playSound("Function_OFF.wav", Manager.FUNCTION_MANAGER.clientSounds.voulme.getValue().floatValue(), false);
            } else {
                SoundUtils.playSound("Function_ON.wav", Manager.FUNCTION_MANAGER.clientSounds.voulme.getValue().floatValue(), false);
            }
        }
    }

    public boolean isState() {
        return this.state;
    }

    protected void onDisable() {
    }

    protected void onEnable() {
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("bind", bind);
        object.addProperty("state", state);

        for (Setting setting : getSettingList()) {
            String name = setting.getName();
            switch (setting.getType()) {
                case BOOLEAN_OPTION -> object.addProperty(name, ((BooleanOption) setting).get());
                case SLIDER_SETTING -> object.addProperty(name, ((SliderSetting) setting).getValue().floatValue());
                case MODE_SETTING -> object.addProperty(name, ((ModeSetting) setting).getIndex());
                case THEME_SETTING -> object.addProperty(name, ((ThemSetting) setting).getIndex());
                case COLOR_SETTING -> object.addProperty(name, ((ColorSetting) setting).get());
                case MULTI_BOX_SETTING -> {
                    ((MultiBoxSetting) setting).options.forEach(option -> object.addProperty(option.getName(), option.get()));
                }
                case BIND_SETTING -> object.addProperty(name, ((BindSetting) setting).getKey());
                case TEXT_SETTING -> object.addProperty(name, ((TextSetting) setting).text);
            }
        }
        return object;
    }


    public void load(JsonObject object, boolean start) {
        if (object != null) {
            if (object.has("bind")) bind = object.get("bind").getAsInt();
            if (object.has("state")) {
                if (start) setStateNotUsing(object.get("state").getAsBoolean());
                else setState(object.get("state").getAsBoolean());
            }

            for (Setting setting : getSettingList()) {
                String name = setting.getName();
                if (!object.has(name) && !(setting instanceof MultiBoxSetting)) {
                    continue;
                }

                switch (setting.getType()) {

                    case BOOLEAN_OPTION -> ((BooleanOption) setting).set(object.get(name).getAsBoolean());
                    case SLIDER_SETTING -> ((SliderSetting) setting).setValue((float) object.get(name).getAsDouble());
                    case MODE_SETTING -> ((ModeSetting) setting).setIndex(object.get(name).getAsInt());
                    case THEME_SETTING -> ((ThemSetting) setting).setIndex(object.get(name).getAsInt());
                    case BIND_SETTING -> ((BindSetting) setting).setKey(object.get(name).getAsInt());
                    case COLOR_SETTING -> ((ColorSetting) setting).setValue(object.get(name).getAsInt());
                    case MULTI_BOX_SETTING -> {
                        ((MultiBoxSetting) setting).options.forEach(option -> option.set(object.get(option.getName()) != null && object.get(option.getName()).getAsBoolean()));
                    }
                    case TEXT_SETTING -> ((TextSetting) setting).text = object.get(name).getAsString();
                }
            }
        }
    }

    public abstract boolean onEvent(Event event);
}