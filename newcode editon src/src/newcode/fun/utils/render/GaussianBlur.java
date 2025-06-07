package newcode.fun.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;
import static newcode.fun.utils.IMinecraft.mc;


/**
 * @author cedo
 * @since 05/13/2022
 */
public class GaussianBlur  {

    private static final ShaderUtils gaussianBlur = new ShaderUtils("blur");

    private static Framebuffer framebuffer = new Framebuffer(1, 1, false, false);

    private static void setupUniforms(float dir1, float dir2, float radius) {
        gaussianBlur.setUniform("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0F / (float) mc.getMainWindow().getWidth(), 1.0F / (float) mc.getMainWindow().getHeight());
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static void startBlur(){
        StencilUtils.initStencilToWrite();
    }
    public static void endBlur(float radius, float compression) {
        StencilUtils.readStencilBuffer(1);

        framebuffer = ShaderUtils.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);

        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtils.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        gaussianBlur.setUniformf("direction", 0, compression);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBlur.detach();

        StencilUtils.uninitStencilBuffer();
        GlStateManager.color4f(-1,-1,1,-1);
        GlStateManager.bindTexture(0);

    }

    public static void blur(float radius, float compression) {
        framebuffer = ShaderUtils.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);

        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtils.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(0, compression, radius);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBlur.detach();

        GlStateManager.color4f(-1,-1,1,-1);
        GlStateManager.bindTexture(0);

    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }


}