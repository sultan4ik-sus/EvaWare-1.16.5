package eva.ware.modules.settings.impl;


import java.util.function.Supplier;

import eva.ware.modules.settings.api.Setting;

public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }
    @Override
    public ColorSetting visibleIf(Supplier<Boolean> bool) {
        return (ColorSetting) super.visibleIf(bool);
    }
}