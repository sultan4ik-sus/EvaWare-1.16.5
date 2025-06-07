package org.luaj.vm2.lib.custom;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import newcode.fun.utils.render.ColorUtils;

public class ColorLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("get", new get());
        env.set("color", library);
        return library;
    }

    static class get extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg1) {
            return LuaValue.valueOf(ColorUtils.getColorStyle(arg1.toint()));
        }
    }

}
