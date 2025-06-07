package org.luaj.vm2.lib.custom;

import newcode.fun.module.api.Module;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import newcode.fun.control.Manager;

public class UtilsLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("setState", new setstate());
        library.set("getName", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(Manager.USER_PROFILE.getName());
            }
        });

        library.set("getUid", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(Manager.USER_PROFILE.getUid());
            }
        });
        library.set("currentTimeMillis", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(System.currentTimeMillis());
            }
        });
        env.set("utils", library);
        return library;
    }

    static class setstate extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            Module module = (Module) arg1.checkuserdata();
            module.setState(arg2.toboolean());
            return arg2;
        }
    }

}
