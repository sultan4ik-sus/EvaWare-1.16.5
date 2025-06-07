package org.luaj.vm2.lib.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import newcode.fun.utils.render.BloomHelper;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.GaussianBlur;
import newcode.fun.utils.render.RenderUtils;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;
import newcode.fun.control.Manager;
import newcode.fun.utils.font.common.Lang;
import newcode.fun.utils.font.styled.StyledFont;
import newcode.fun.utils.font.styled.StyledFontRenderer;

import static newcode.fun.utils.render.RenderUtils.Render2D.drawFace;

public class DisplayLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("rectangle", new rectangle());
        library.set("rounded", new rounded());
        library.set("face", new face());
        library.set("glow", new glow());
        library.set("circle", new circle());
        library.set("drawText", new drawtext());
        library.set("drawCenterText", new drawcentertext());
        library.set("getTextWidth", new textwidth());
        library.set("getWidth", new width());
        library.set("getHeight", new height());
        library.set("drawImage", new FiveArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5) {
                RenderSystem.enableBlend();
                GlStateManager.bindTexture(RenderUtils.Render2D.downloadImage(arg1.toString()));
                RenderUtils.Render2D.quadsBegin(arg2.tofloat(), arg3.tofloat(),arg4.tofloat(),arg5.tofloat(), 7);
                GlStateManager.bindTexture(0);
                return LuaValue.valueOf(0);
            }
        });
        library.set("rgb", new rgb());
        library.set("bloom", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                BloomHelper.registerRenderCall(() -> {
                    arg.checkfunction().call();
                });
                return LuaValue.valueOf(0);
            }
        });
        library.set("blur", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg,LuaValue arg1) {

                GaussianBlur.startBlur();
                arg.checkfunction().call();
                GaussianBlur.endBlur(arg1.tofloat(), 1);

                return LuaValue.valueOf(0);
            }
        });
        library.set("createFont", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                StyledFont font = new StyledFont(arg1.toString(), arg2.toint(), 0.0f, 0.0f, 0.0f, false, Lang.ENG_RU, true);
                return DisplayLib.userdataOf(font);
            }
        });
        env.set("display", library);
        return library;
    }

    static class rgb extends FourArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4) {
            return LuaValue.valueOf(ColorUtils.rgba(arg1.toint(), arg2.toint(), arg3.toint(), arg4.toint()));
        }
    }

    static class rectangle extends FiveArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5) {
            RenderUtils.Render2D.drawRect(arg1.tofloat(), arg2.tofloat(), arg3.tofloat(), arg4.tofloat(), arg5.toint());
            return LuaValue.valueOf(0);
        }
    }

    static class face extends FourArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4) {
            LivingEntity target = Manager.FUNCTION_MANAGER.auraFunction.getTarget();
            if (target instanceof PlayerEntity)
                drawFace(arg1.tofloat(), arg2.tofloat(), 8F, 8F, 8F, 8F, arg3.tofloat(), arg4.tofloat(), 64F, 64F, (AbstractClientPlayerEntity) target);
            return LuaValue.valueOf(0);
        }
    }

    static class glow extends SixArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5, LuaValue arg6) {
            RenderUtils.Render2D.drawShadow(arg1.tofloat(), arg2.tofloat(), arg3.tofloat(), arg4.tofloat(), arg5.toint(), arg6.toint());
            return LuaValue.valueOf(0);
        }
    }

    static class rounded extends SixArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5, LuaValue arg6) {
            RenderUtils.Render2D.drawRoundedRect(arg1.tofloat(), arg2.tofloat(), arg3.tofloat(), arg4.tofloat(), arg5.tofloat(), arg6.toint());
            return LuaValue.valueOf(0);
        }
    }

    static class circle extends SevenArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5, LuaValue arg6, LuaValue arg7) {
            RenderUtils.Render2D.drawCircle(arg1.tofloat(), arg2.tofloat(), arg3.tofloat(), arg4.tofloat(), arg5.tofloat(), arg7.tofloat(), false, arg6.toint());
            return LuaValue.valueOf(0);
        }
    }

    static class width extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(mc.getMainWindow().scaledWidth());
        }
    }

    static class height extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(mc.getMainWindow().scaledHeight());
        }
    }


    static class drawtext extends SixArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5, LuaValue arg6) {
            StyledFontRenderer.drawString((MatrixStack) arg1.touserdata(), (StyledFont) arg2.touserdata(), arg3.toString(), arg4.tofloat(), arg5.tofloat(), arg6.toint());
            return LuaValue.valueOf(0);
        }
    }

    static class drawcentertext extends SixArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3, LuaValue arg4, LuaValue arg5, LuaValue arg6) {
            StyledFontRenderer.drawCenteredString((MatrixStack) arg1.touserdata(), (StyledFont) arg2.touserdata(), arg3.toString(), arg4.tofloat(), arg5.tofloat(), arg6.toint());
            return LuaValue.valueOf(0);
        }
    }

    static class textwidth extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {

            StyledFont font = (StyledFont) arg1.touserdata();

            return LuaValue.valueOf(font.getWidth(arg2.toString()));
        }
    }

}
