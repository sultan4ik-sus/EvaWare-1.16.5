package eva.ware.modules.settings.impl;


import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import eva.ware.modules.settings.api.Setting;

public class ModeListSetting extends Setting<List<CheckBoxSetting>> {
    public ModeListSetting(String name, CheckBoxSetting... strings) {
        super(name, Arrays.asList(strings));
    }

    public CheckBoxSetting is(String settingName) {
        return getValue().stream().filter(booleanSetting -> booleanSetting.getName().equalsIgnoreCase(settingName)).findFirst().orElse(null);
    }

    public CheckBoxSetting get(int index) {
        return getValue().get(index);
    }

    @Override
    public ModeListSetting visibleIf(Supplier<Boolean> bool) {
        return (ModeListSetting) super.visibleIf(bool);
    }
}