package eva.ware.modules.settings.impl;


import java.util.function.Supplier;

import eva.ware.modules.settings.api.Setting;

public class CheckBoxSetting extends Setting<Boolean> {

    public CheckBoxSetting(String name, Boolean defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public CheckBoxSetting visibleIf(Supplier<Boolean> bool) {
        return (CheckBoxSetting) super.visibleIf(bool);
    }

}