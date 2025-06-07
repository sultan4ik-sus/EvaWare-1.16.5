package newcode.fun.module.settings.imp;

import lombok.Getter;
import lombok.Setter;
import newcode.fun.module.settings.Setting;

import java.util.Arrays;
import java.util.function.Supplier;

@Getter
public class ThemSetting extends Setting {
    @Setter
    private int index;
    public String[] modes;


    public ThemSetting(String name, String current, String... modes) {
        super(name);
        this.modes = modes;
        this.index = Arrays.asList(modes).indexOf(current);
    }

    public boolean is(String mode) {
        return get().equals(mode);
    }

    public String get() {
        try {
            if (index < 0 || index >= modes.length) {
                return modes[0];
            }
            return modes[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "ERROR";
        }
    }

    public void set(String mode) {
        this.index = Arrays.asList(modes).indexOf(mode);
    }

    public void set(int mode) {
        this.index = mode;
    }

    public ThemSetting setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.THEME_SETTING;
    }
}
