package eva.ware.utils.render.shader.impl;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.opengl.GL30;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.IRenderCall;

import eva.ware.utils.client.IMinecraft;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.shader.ShaderUtility;
import net.minecraft.client.shader.Framebuffer;

public class Outline implements IMinecraft {

    private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
    private static final Framebuffer inFrameBuffer = new Framebuffer(1, 1, true, false);
    private static final Framebuffer outFrameBuffer = new Framebuffer(1, 1, true, false);

    public static void registerRenderCall(IRenderCall rc) {
        renderQueue.add(rc);
    }

    public static void draw(int radius, int color) {
        if (renderQueue.isEmpty())
            return;

        setupBuffer(inFrameBuffer);
        setupBuffer(outFrameBuffer);

        inFrameBuffer.bindFramebuffer(true);

        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute();
        }

        outFrameBuffer.bindFramebuffer(true);

        ShaderUtility.outline.attach();
        ShaderUtility.outline.setUniformf("size", radius);
        ShaderUtility.outline.setUniform("textureIn", 0);
        ShaderUtility.outline.setUniform("textureToCheck", 20);
        ShaderUtility.outline.setUniformf("texelSize", 1.0F / (float) mc.getMainWindow().getWidth(),
                1.0F / (float) mc.getMainWindow().getHeight());
        ShaderUtility.outline.setUniformf("direction", 1.0F, 0.0F);
        float[] col = ColorUtility.rgba(color);
        ShaderUtility.outline.setUniformf("color", col[0], col[1], col[2]);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);

        inFrameBuffer.bindFramebufferTexture();
        ShaderUtility.drawQuads();

        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        ShaderUtility.outline.setUniformf("direction", 0.0F, 1.0F);

        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        inFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        ShaderUtility.drawQuads();

        ShaderUtility.outline.detach();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    public static Framebuffer setupBuffer(Framebuffer frameBuffer) {
        if (frameBuffer.framebufferWidth != mc.getMainWindow().getWidth()
                || frameBuffer.framebufferHeight != mc.getMainWindow().getHeight())
            frameBuffer.resize(Math.max(1, mc.getMainWindow().getWidth()), Math.max(1, mc.getMainWindow().getHeight()),
                    false);
        else
            frameBuffer.framebufferClear(false);
        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);

        return frameBuffer;
    }

}