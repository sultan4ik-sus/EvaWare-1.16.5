package newcode.fun.module.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.shader.Framebuffer;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.utils.render.*;

@Annotation(name = "CustomHand", type = TypeList.Render)
public class CustomHand extends Module {

    public static Framebuffer handBuffer = new Framebuffer(1,1, true,false);

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender e) {
            if (e.isRender2D() && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                OutlineUtils.registerRenderCall(() -> {
                    handBuffer.bindFramebufferTexture();
                    ShaderUtils.drawQuads();
                });
                BloomHelper.registerRenderCall(() -> {
                    handBuffer.bindFramebufferTexture();
                    ShaderUtils.drawQuads();
                });
                BloomHelper.drawC(10, 1f, true, ColorUtils.getColorStyle(0), 3);
                OutlineUtils.draw(1, ColorUtils.getColorStyle(0));

                GaussianBlur.startBlur();
                handBuffer.bindFramebufferTexture();
                ShaderUtils.drawQuads();
                GaussianBlur.endBlur(10,3);


                GlStateManager.popMatrix();
            }
        }
        return false;
    }
}
