package eva.ware.modules.settings.impl;


import java.util.function.Supplier;

import eva.ware.modules.settings.api.Setting;

public class ModeSetting extends Setting<String> {

    public String[] strings;

    public ModeSetting(String name, String defaultVal, String... strings) {
        super(name, defaultVal);
        this.strings = strings;
    }

    public int getIndex() {
        int index = 0;
        for (String val : strings) {
            if (val.equalsIgnoreCase(getValue())) {
                return index;
            }
            index++;
        }
        return 0;
    }

    public boolean is(String s) {
        return getValue().equalsIgnoreCase(s);
    }
    @Override
    public ModeSetting visibleIf(Supplier<Boolean> bool) {
        return (ModeSetting) super.visibleIf(bool);
    }

}