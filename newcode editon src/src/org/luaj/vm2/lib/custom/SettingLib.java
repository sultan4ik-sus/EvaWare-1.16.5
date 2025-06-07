package org.luaj.vm2.lib.custom;

import newcode.fun.module.api.Module;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

import java.util.ArrayList;
import java.util.List;

public class SettingLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("addBool", new addBoolean());
        library.set("addNumber", new addSlider());
        library.set("addMode", new addMode());
        library.set("get", new get());
        env.set("setting", library);
        return library;
    }

    static class addBoolean extends ThreeArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {

            Module module = (Module) arg1.checkuserdata();

            String name = arg2.toString();
            String nameen = arg3.toString();
            boolean value = arg3.toboolean();

            module.addSettings(new BooleanOption(name, nameen, value));

            return LuaValue.valueOf(0);
        }
    }

    static class addSlider extends SixArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5, LuaValue arg6) {
            Module module = (Module) arg1.checkuserdata();
            module.addSettings(new SliderSetting(arg2.toString(), arg3.tofloat(), arg4.tofloat(),arg5.tofloat(), arg6.tofloat()));
            return LuaValue.valueOf(0);
        }

    }

    static class addMode extends FourArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4) {
            Module module = (Module) arg1.checkuserdata();
            System.out.println(arg4);
            LuaTable table = (LuaTable) arg4;
            List<String> val = new ArrayList<>();
            int ind = 1;
            while (true) {
                LuaValue value = table.get(ind);
                if (value != NIL) {
                    val.add(value.toString());
                } else {
                    break;
                }
                ind++;
            }

            module.addSettings(new ModeSetting(arg2.toString(), arg3.toString(), val.toArray(String[]::new)));
            return LuaValue.valueOf(0);
        }

    }

    static class get extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            Module module = (Module) arg1.checkuserdata();
            for (Setting setting : module.settingList) {
                if (setting.getName().equalsIgnoreCase(arg2.toString())) {
                    if (setting instanceof BooleanOption b) {
                        return LuaValue.valueOf(b.get());
                    }
                    if (setting instanceof SliderSetting b) {
                        return LuaValue.valueOf(b.getValue().floatValue());
                    }
                    if (setting instanceof ModeSetting b) {
                        return LuaValue.valueOf(b.get());
                    }
                }
            }
            return LuaValue.valueOf(0);
        }

    }

}
