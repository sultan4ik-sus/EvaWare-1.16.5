package newcode.fun.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;
import static newcode.fun.utils.IMinecraft.mc;


/**
 * @author cedo
 * @since 05/13/2022
 */
public class NoiseShader {

    private static final ShaderUtils gaussianBlur = new ShaderUtils("noise");

    private static Framebuffer framebuffer = new Framebuffer(1, 1, false, false);

    private static void setupUniforms(float time) {
        gaussianBlur.setUniform("u_texture", 0);
        gaussianBlur.setUniformf("u_value", time);
    }

    public static void start(){
        StencilUtils.initStencilToWrite();
    }
    public static void end(float time) {
        StencilUtils.readStencilBuffer(1);

        framebuffer = ShaderUtils.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(time);

        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtils.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(time);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBlur.detach();

        StencilUtils.uninitStencilBuffer();
        GlStateManager.color4f(-1,-1,1,-1);
        GlStateManager.bindTexture(0);

    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }


}