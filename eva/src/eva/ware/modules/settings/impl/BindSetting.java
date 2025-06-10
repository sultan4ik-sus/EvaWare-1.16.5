package eva.ware.modules.settings.impl;

import java.util.function.Supplier;

import eva.ware.modules.settings.api.Setting;

public class BindSetting extends Setting<Integer> {
    public BindSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public BindSetting visibleIf(Supplier<Boolean> bool) {
        return (BindSetting) super.visibleIf(bool);
    }
}
