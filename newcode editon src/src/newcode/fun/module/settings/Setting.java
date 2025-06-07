package newcode.fun.module.settings;

import lombok.Getter;

import java.awt.*;
import java.util.function.Supplier;

@Getter
public abstract class Setting<Value> {
    private final String name;
    public Supplier<Boolean> visible = () -> true;
    public Color color = Color.WHITE;
    Value defaultVal;

    String settingName;

    public abstract SettingType getType();

    public Setting(String name) {
        this.name = name;
    }

    public void set(Value value) {
        defaultVal = value;
    }
    public Boolean visible() {
        return visible.get();
    }

    public enum SettingType {
        BOOLEAN_OPTION,
        SLIDER_SETTING,
        MODE_SETTING,
        COLOR_SETTING,
        MULTI_BOX_SETTING,
        THEME_SETTING,
        BIND_SETTING,
        BUTTON_SETTING,
        TEXT_SETTING
    }
}