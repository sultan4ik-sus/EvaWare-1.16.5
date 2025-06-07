package newcode.fun.module.settings.imp;

import lombok.Getter;
import lombok.Setter;
import newcode.fun.module.settings.Setting;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class ModeSetting extends Setting {
    @Setter
    private int index;
    public String[] modes;

    private Consumer<String> onChangeListener; // Колбэк для изменения значения

    public ModeSetting(String name, String current, String... modes) {
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
        int oldIndex = this.index;
        this.index = Arrays.asList(modes).indexOf(mode);
        if (oldIndex != this.index && onChangeListener != null) {
            onChangeListener.accept(get());
        }
    }

    public void set(int mode) {
        int oldIndex = this.index;
        this.index = mode;
        if (oldIndex != this.index && onChangeListener != null) {
            onChangeListener.accept(get());
        }
    }

    public ModeSetting setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    public ModeSetting onChange(Consumer<String> listener) {
        this.onChangeListener = listener;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.MODE_SETTING;
    }
}
