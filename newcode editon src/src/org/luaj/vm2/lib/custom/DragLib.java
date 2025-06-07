package org.luaj.vm2.lib.custom;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.FourArgFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import newcode.fun.NewCode;
import newcode.fun.module.api.Module;
import newcode.fun.control.drag.Dragging;

public class DragLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("create", new create());
        library.set("setSize", new setSize());
        library.set("getPosition", new getPosition());
        env.set("drag", library);
        return library;
    }

    static class create extends FourArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4) {
            Module module = (Module) arg1.checkuserdata();
            return DragLib.userdataOf(NewCode.createDrag(module, arg2.toString(), arg3.tofloat(), arg4.tofloat()));
        }
    }

    static class setSize extends ThreeArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
            Dragging function = (Dragging) arg1.checkuserdata();
            function.setWidth(arg2.tofloat());

            function.setHeight(arg3.tofloat());
            return LuaValue.valueOf(0);
        }
    }

    static class getPosition extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg1) {
            Dragging function = (Dragging) arg1.checkuserdata();
            return LuaValue.listOf(new LuaValue[]{
                    LuaValue.valueOf(function.getX()),
                    LuaValue.valueOf(function.getY())
            });
        }
    }

}
