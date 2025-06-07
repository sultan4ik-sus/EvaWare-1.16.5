package newcode.fun.utils.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jhlabs.image.GaussianFilter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.optifine.util.TextureUtils;
import org.joml.Vector4i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import newcode.fun.control.Manager;
import newcode.fun.module.impl.player.Optimization;
import newcode.fun.ui.midnight.Style;
import newcode.fun.utils.IMinecraft;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.platform.GlStateManager.disableBlend;
import static com.mojang.blaze3d.platform.GlStateManager.*;
import static com.mojang.blaze3d.systems.RenderSystem.enableBlend;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;
import static org.lwjgl.opengl.GL11.*;
import static newcode.fun.utils.render.RenderUtils.IntColor.*;
import static newcode.fun.utils.render.ShaderUtils.*;


public class RenderUtils implements IMinecraft {
    public static ShaderUtils FrameBuffer;

    private static final ShaderUtils ROUNDED_GRADIENT = ShaderUtils.create("roundedGradient");

    public static int reAlphaInt(final int color,
                                 final int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    public static void color(final int rgb) {
        GL11.glColor3f(getRed(rgb) / 255f, getGreen(rgb) / 255f, getBlue(rgb) / 255f);
    }

    public static boolean isInRegion(final int mouseX,
                                     final int mouseY,
                                     final int x,
                                     final int y,
                                     final int width,
                                     final int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static boolean isInRegion(final double mouseX,
                                     final double mouseY,
                                     final float x,
                                     final float y,
                                     final float width,
                                     final float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static boolean isInRegion(final double mouseX,
                                     final double mouseY,
                                     final int x,
                                     final int y,
                                     final int width,
                                     final int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static class IntColor {

        public static float[] rgb(final int color) {
            return new float[]{
                    (color >> 16 & 0xFF) / 255f,
                    (color >> 8 & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    (color >> 24 & 0xFF) / 255f
            };
        }

        public static int rgba(final int r,
                               final int g,
                               final int b,
                               final int a) {
            return a << 24 | r << 16 | g << 8 | b;
        }

        public static int getRed(final int hex) {
            return hex >> 16 & 255;
        }

        public static int getGreen(final int hex) {
            return hex >> 8 & 255;
        }

        public static int getBlue(final int hex) {
            return hex & 255;
        }

        public static int getAlpha(final int hex) {
            return hex >> 24 & 255;
        }
    }


    public static class Render2D {

        private static final HashMap<Integer, Integer> shadowCache = new HashMap<>();

        public static void drawTriangle(float x, float y, float width, float height, Color color) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            enableSmoothLine(1);
            GL11.glRotatef(180 + 90, 0F, 0F, 1.0F);

            GL11.glBegin(9);
            ColorUtils.setColor(color.getRGB());
            GL11.glVertex2f(x, y - 2);
            GL11.glVertex2f(x + width, y + height);
            GL11.glVertex2f(x + width, y);
            GL11.glVertex2f(x, y - 2);
            GL11.glEnd();

            GL11.glBegin(9);
            ColorUtils.setColor(color.brighter().getRGB());
            GL11.glVertex2f(x + width, y);
            GL11.glVertex2f(x + width, y + height);
            GL11.glVertex2f(x + width * 2, y - 2);
            GL11.glVertex2f(x + width, y);
            GL11.glEnd();

            GL11.glBegin(3);
            ColorUtils.setColor(color.getRGB());
            GL11.glVertex2f(x, y - 2);
            GL11.glVertex2f(x + width, y + height);
            GL11.glVertex2f(x + width, y);
            GL11.glVertex2f(x, y - 2);
            GL11.glEnd();

            GL11.glBegin(3);
            ColorUtils.setColor(color.brighter().getRGB());
            GL11.glVertex2f(x + width, y);
            GL11.glVertex2f(x + width, y + height);
            GL11.glVertex2f(x + width * 2, y - 2);
            GL11.glVertex2f(x + width, y);
            GL11.glEnd();

            disableSmoothLine();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glRotatef(-180 - 90, 0F, 0F, 1.0F);
            GL11.glPopMatrix();
        }

        public static void enableSmoothLine(float width) {
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(width);
        }

        public static void disableSmoothLine() {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        }

        public static int downloadImage(String url) {
            int texId = -1;
            int identifier = Objects.hash(url);
            if (shadowCache2.containsKey(identifier)) {
                texId = shadowCache2.get(identifier);
            } else {
                URL stringURL = null;
                try {
                    stringURL = new URL(url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                BufferedImage img = null;
                try {
                    img = ImageIO.read(stringURL);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                try {
                    texId = loadTexture(img);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                shadowCache2.put(identifier, texId);
            }
            return texId;
        }


        private static HashMap<Integer, Integer> shadowCache2 = new HashMap<Integer, Integer>();


        public static void drawShadow(float x, float y, float width, float height, int radius, int color) {
            Optimization optimization = Manager.FUNCTION_MANAGER.optimization;

            if (optimization.state && optimization.optimizeSelection.get(2)) {
                return;
            }

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(GL_GREATER, 0.01f);
            GlStateManager.disableAlphaTest();

            x -= radius;
            y -= radius;
            width = width + radius * 2;
            height = height + radius * 2;
            x -= 0.25f;
            y += 0.25f;

            int identifier = Objects.hash(width, height, radius);
            int textureId;

            if (shadowCache.containsKey(identifier)) {
                textureId = shadowCache.get(identifier);
                GlStateManager.bindTexture(textureId);
            } else {
                if (width <= 0) {
                    width = 1;
                }

                if (height <= 0) {
                    height = 1;
                }

                BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D graphics = originalImage.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
                graphics.dispose();

                GaussianFilter filter = new GaussianFilter(radius);
                BufferedImage blurredImage = filter.filter(originalImage, null);
                DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
                texture.setBlurMipmap(true, true);
                try {
                    textureId = texture.getGlTextureId();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                shadowCache.put(identifier, textureId);
            }

            float[] startColorComponents = rgb(color);

            buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
            buffer.pos(x, y, 0.0f)
                    .color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3])
                    .tex(0.0f, 0.0f)
                    .endVertex();

            buffer.pos(x, y + (float) ((int) height), 0.0f)
                    .color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3])
                    .tex(0.0f, 1.0f)
                    .endVertex();

            buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                    .color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3])
                    .tex(1.0f, 1.0f)
                    .endVertex();

            buffer.pos(x + (float) ((int) width), y, 0.0f)
                    .color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3])
                    .tex(1.0f, 0.0f)
                    .endVertex();

            tessellator.draw();
            GlStateManager.enableAlphaTest();
            GlStateManager.bindTexture(0);
            GlStateManager.disableBlend();
        }


        public static void drawShadow(float x, float y, float width, float height, int radius, int startColor, int endColor) {
            Optimization optimization = Manager.FUNCTION_MANAGER.optimization;

            if (optimization.state && optimization.optimizeSelection.get(2)) {
                return;
            }

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(GL_GREATER, 0.01f);

            x -= radius;
            y -= radius;
            width = width + radius * 2;
            height = height + radius * 2;
            x -= 0.25f;
            y += 0.25f;

            int identifier = Objects.hash(width, height, radius);
            int textureId;

            if (shadowCache.containsKey(identifier)) {
                textureId = shadowCache.get(identifier);
                GlStateManager.bindTexture(textureId);
            } else {
                if (width <= 0) {
                    width = 1;
                }

                if (height <= 0) {
                    height = 1;
                }

                BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D graphics = originalImage.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
                graphics.dispose();

                GaussianFilter filter = new GaussianFilter(radius);
                BufferedImage blurredImage = filter.filter(originalImage, null);
                DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
                texture.setBlurMipmap(true, true);
                try {
                    textureId = texture.getGlTextureId();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                shadowCache.put(identifier, textureId);
            }

            float[] startColorComponents = rgb(startColor);
            float[] endColorComponents = rgb(endColor);

            buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
            buffer.pos(x, y, 0.0f)
                    .color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3])
                    .tex(0.0f, 0.0f)
                    .endVertex();

            buffer.pos(x, y + (float) ((int) height), 0.0f)
                    .color(startColorComponents[0], startColorComponents[1], startColorComponents[2], startColorComponents[3])
                    .tex(0.0f, 1.0f)
                    .endVertex();

            buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                    .color(endColorComponents[0], endColorComponents[1], endColorComponents[2], endColorComponents[3])
                    .tex(1.0f, 1.0f)
                    .endVertex();

            buffer.pos(x + (float) ((int) width), y, 0.0f)
                    .color(endColorComponents[0], endColorComponents[1], endColorComponents[2], endColorComponents[3])
                    .tex(1.0f, 0.0f)
                    .endVertex();

            tessellator.draw();

            GlStateManager.bindTexture(0);
            GlStateManager.disableBlend();
        }

        public static void drawShadow(float x, float y, float width, float height, int radius, int bottomLeft, int topLeft, int bottomRight, int topRight) {
            GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA.param,
                    DestFactor.ONE_MINUS_SRC_ALPHA.param, SourceFactor.ONE.param,
                    DestFactor.ZERO.param);
            GlStateManager.shadeModel(7425);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(GL_GREATER, 0.01f);

            x -= radius;
            y -= radius;
            width = width + radius * 2;
            height = height + radius * 2;
            x -= 0.25f;
            y += 0.25f;

            // Проверка на минимальные значения width и height
            if (width < 1) {
                width = 1;
            }

            if (height < 1) {
                height = 1;
            }

            int identifier = Objects.hash(width, height, radius);
            int textureId;

            if (shadowCache.containsKey(identifier)) {
                textureId = shadowCache.get(identifier);
                GlStateManager.bindTexture(textureId);
            } else {
                BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D graphics = originalImage.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
                graphics.dispose();

                // Применение Gaussian фильтра
                GaussianFilter filter = new GaussianFilter(radius);
                BufferedImage blurredImage = filter.filter(originalImage, null);
                DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
                texture.setBlurMipmap(true, true);
                try {
                    textureId = texture.getGlTextureId();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                shadowCache.put(identifier, textureId);
            }

            float[] bottomLefts = rgb(bottomLeft);
            float[] topLefts = rgb(topLeft);
            float[] bottomRights = rgb(bottomRight);
            float[] topRights = rgb(topRight);

            buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
            buffer.pos(x, y, 0.0f)
                    .color(bottomLefts[0], bottomLefts[1], bottomLefts[2], bottomLefts[3])
                    .tex(0.0f, 0.0f)
                    .endVertex();

            buffer.pos(x, y + (float) ((int) height), 0.0f)
                    .color(topLefts[0], topLefts[1], topLefts[2], topLefts[3])
                    .tex(0.0f, 1.0f)
                    .endVertex();

            buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                    .color(topRights[0], topRights[1], topRights[2], topRights[3])
                    .tex(1.0f, 1.0f)
                    .endVertex();

            buffer.pos(x + (float) ((int) width), y, 0.0f)
                    .color(bottomRights[0], bottomRights[1], bottomRights[2], bottomRights[3])
                    .tex(1.0f, 0.0f)
                    .endVertex();

            tessellator.draw();
            GlStateManager.shadeModel(7424);
            GlStateManager.bindTexture(0);
            GlStateManager.disableBlend();
        }



        public static void drawCircle(float x, float y, float start, float end, float radius, float width, boolean filled, Style s) {

            float i;
            float endOffset;
            if (start > end) {
                endOffset = end;
                end = start;
                start = endOffset;
            }
            GlStateManager.enableBlend();
            RenderSystem.disableAlphaTest();
            GL11.glDisable(GL_TEXTURE_2D);
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.shadeModel(7425);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (i = end; i >= start; i--) {
                ColorUtils.setColor(s.getColor((int) (i * 1)));
                float cos = (float) (MathHelper.cos((float) (i * Math.PI / 180)) * radius);
                float sin = (float) (MathHelper.sin((float) (i * Math.PI / 180)) * radius);
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            if (filled) {
                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                for (i = end; i >= start; i--) {
                    ColorUtils.setColor(s.getColor((int) (i * 1)));
                    float cos = (float) MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                    float sin = (float) MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                    GL11.glVertex2f(x + cos, y + sin);
                }
                GL11.glEnd();
            }

            RenderSystem.enableAlphaTest();
            RenderSystem.shadeModel(7424);
            GL11.glEnable(GL_TEXTURE_2D);
            GlStateManager.disableBlend();
        }

        public static void drawCircle(float x, float y, float start, float end, float radius, float width, boolean filled, int color) {

            float i;
            float endOffset;
            if (start > end) {
                endOffset = end;
                end = start;
                start = endOffset;
            }

            GlStateManager.enableBlend();
            GL11.glDisable(GL_TEXTURE_2D);
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (i = end; i >= start; i--) {
                ColorUtils.setColor(color);
                float cos = (float) (MathHelper.cos((float) (i * Math.PI / 180)) * radius);
                float sin = (float) (MathHelper.sin((float) (i * Math.PI / 180)) * radius);
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);

            if (filled) {
                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                for (i = end; i >= start; i--) {
                    ColorUtils.setColor(color);
                    float cos = (float) MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                    float sin = (float) MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                    GL11.glVertex2f(x + cos, y + sin);
                }
                GL11.glEnd();
            }

            GL11.glEnable(GL_TEXTURE_2D);
            GlStateManager.disableBlend();
        }

        public static void drawRoundedRect2(float x, float y, float width, float height, float radius, int color) {
            GlStateManager.enableBlend();
            GL11.glDisable(GL_TEXTURE_2D);
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);

            // Set the color for the rounded rect
            ColorUtils.setColor(color);

            // Draw the center part (the square body)
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(x + radius, y); // top-left corner
            GL11.glVertex2f(x + width - radius, y); // top-right corner
            GL11.glVertex2f(x + width - radius, y + height); // bottom-right corner
            GL11.glVertex2f(x + radius, y + height); // bottom-left corner
            GL11.glEnd();

            // Draw the four rounded corners (quarter circles)
            drawQuarterCircle(x + radius, y + radius, radius, 180, 270, color); // top-left corner
            drawQuarterCircle(x + width - radius, y + radius, radius, 270, 360, color); // top-right corner
            drawQuarterCircle(x + width - radius, y + height - radius, radius, 0, 90, color); // bottom-right corner
            drawQuarterCircle(x + radius, y + height - radius, radius, 90, 180, color); // bottom-left corner

            GL11.glEnable(GL_TEXTURE_2D);
            GlStateManager.disableBlend();
        }

        private static void drawQuarterCircle(float x, float y, float radius, float startAngle, float endAngle, int color) {
            float i;
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            for (i = startAngle; i <= endAngle; i++) {
                ColorUtils.setColor(color);
                float cos = (float) MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                float sin = (float) MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
        }

        public static int loadTexture(BufferedImage image) throws Exception {
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            buffer.flip();

            int textureID = GlStateManager.genTexture();
            RenderSystem.bindTexture(textureID);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
            GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
            RenderSystem.bindTexture(0);
            return textureID;
        }

        public static void drawFace(float d, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight, AbstractClientPlayerEntity target) {
            try {
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                ResourceLocation skin = target.getLocationSkin();
                mc.getTextureManager().bindTexture(skin);
                float hurtPercent = getHurtPercent(target);
                GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
                AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glPopMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void drawFace(float x, float y, float width, float height) {
            try {
                AbstractGui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, width, height, 64, 64);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static float getRenderHurtTime(LivingEntity hurt) {
            return hurt.hurtTime - (hurt.hurtTime != 0 ? mc.timer.renderPartialTicks : 0);
        }

        public static float getHurtPercent(LivingEntity hurt) {
            return getRenderHurtTime(hurt) / 10f;
        }

        public static void drawRect(float x, float y, float width, float height, int color) {
            drawMcRect(x, y, x + width, y + height, color);
        }

        public static void drawRoundCircle(float x,
                                           float y,
                                           float radius,
                                           int color) {
            drawRoundCircle(x - (radius / 2), y - (radius / 2), (radius), 255.0f, 255.0f, 255.0f);
        }
        public static void drawRoundCircle(float x, float y, float radius, float red, float green, float blue) {
            glColor3f(red, green, blue);  // Устанавливаем цвет обводки

            // Устанавливаем толщину линии (например, 1.0 для стандартной толщины)
            glLineWidth(1.0f);  // Устанавливаем толщину линии (по умолчанию она обычно равна 1.0)

            glBegin(GL_LINE_LOOP);  // Начинаем рисование линии по окружности
            for (int i = 0; i < 360; i++) {
                double angle = org.joml.Math.toRadians(i);
                float x1 = (float) (x + org.joml.Math.cos(angle) * radius);
                float y1 = (float) (y + org.joml.Math.sin(angle) * radius);
                glVertex2f(x1, y1);  // Рисуем вершину круга
            }
            glEnd();  // Завершаем рисование линии по окружности
        }


        public static void drawRoundCircle(float x,
                                           float y,
                                           float radius,
                                           int bottomLeft, int topLeft, int bottomRight, int topRight) {
            drawGradientRound(x - (radius / 2), y - (radius / 2), radius, radius, (radius / 2), bottomLeft,topLeft,bottomRight,topRight);
        }


        public static void drawMcRect(double left, double top, double right, double bottom, int color) {
            if (left < right) {
                double i = left;
                left = right;
                right = i;
            }

            if (top < bottom) {
                double j = top;
                top = bottom;
                bottom = j;
            }

            float f3 = (float) (color >> 24 & 255) / 255.0F;
            float f = (float) (color >> 16 & 255) / 255.0F;
            float f1 = (float) (color >> 8 & 255) / 255.0F;
            float f2 = (float) (color & 255) / 255.0F;
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
            bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
            bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
            bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

        public static void drawLine(double x, double y, double z, double w, int color) {
            float f3 = (float) (color >> 24 & 255) / 255.0F;
            float f = (float) (color >> 16 & 255) / 255.0F;
            float f1 = (float) (color >> 8 & 255) / 255.0F;
            float f2 = (float) (color & 255) / 255.0F;
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glLineWidth(1);
            bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, y, 0.0F).color(f, f1, f2, f3).endVertex();
            bufferbuilder.pos(z, w, 0.0F).color(f, f1, f2, f3).endVertex();
            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            GL11.glDisable(GL_LINE_SMOOTH);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

        public static void drawMCHorizontal(double x, double y, double width, double height, int start, int end) {

            float f = (float) (start >> 24 & 255) / 255.0F;
            float f1 = (float) (start >> 16 & 255) / 255.0F;
            float f2 = (float) (start >> 8 & 255) / 255.0F;
            float f3 = (float) (start & 255) / 255.0F;
            float f4 = (float) (end >> 24 & 255) / 255.0F;
            float f5 = (float) (end >> 16 & 255) / 255.0F;
            float f6 = (float) (end >> 8 & 255) / 255.0F;
            float f7 = (float) (end & 255) / 255.0F;

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();

            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }

        public static void drawMCHorizontalBuilding(double x, double y, double width, double height, int start, int end) {

            float f = (float) (start >> 24 & 255) / 255.0F;
            float f1 = (float) (start >> 16 & 255) / 255.0F;
            float f2 = (float) (start >> 8 & 255) / 255.0F;
            float f3 = (float) (start & 255) / 255.0F;
            float f4 = (float) (end >> 24 & 255) / 255.0F;
            float f5 = (float) (end >> 16 & 255) / 255.0F;
            float f6 = (float) (end >> 8 & 255) / 255.0F;
            float f7 = (float) (end & 255) / 255.0F;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();

        }


        public static void drawHorizontal(float x, float y, float width, float height, int start, int end) {
            width += x;
            height += y;

            float f = (float) (start >> 24 & 255) / 255.0F;
            float f1 = (float) (start >> 16 & 255) / 255.0F;
            float f2 = (float) (start >> 8 & 255) / 255.0F;
            float f3 = (float) (start & 255) / 255.0F;
            float f4 = (float) (end >> 24 & 255) / 255.0F;
            float f5 = (float) (end >> 16 & 255) / 255.0F;
            float f6 = (float) (end >> 8 & 255) / 255.0F;
            float f7 = (float) (end & 255) / 255.0F;

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();

            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }

        public static void drawVertical(float x, float y, float width, float height, int start, int end) {
            width += x;
            height += y;

            float f = (float) (start >> 24 & 255) / 255.0F;
            float f1 = (float) (start >> 16 & 255) / 255.0F;
            float f2 = (float) (start >> 8 & 255) / 255.0F;
            float f3 = (float) (start & 255) / 255.0F;
            float f4 = (float) (end >> 24 & 255) / 255.0F;
            float f5 = (float) (end >> 16 & 255) / 255.0F;
            float f6 = (float) (end >> 8 & 255) / 255.0F;
            float f7 = (float) (end & 255) / 255.0F;

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(x, y, 0f).color(f5, f6, f7, f4).endVertex();

            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();

        }

        public static void drawMCVertical(double x, double y, double width, double height, int start, int end) {
            float f = (float) (start >> 24 & 255) / 255.0F;
            float f1 = (float) (start >> 16 & 255) / 255.0F;
            float f2 = (float) (start >> 8 & 255) / 255.0F;
            float f3 = (float) (start & 255) / 255.0F;
            float f4 = (float) (end >> 24 & 255) / 255.0F;
            float f5 = (float) (end >> 16 & 255) / 255.0F;
            float f6 = (float) (end >> 8 & 255) / 255.0F;
            float f7 = (float) (end & 255) / 255.0F;

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(x, y, 0f).color(f5, f6, f7, f4).endVertex();

            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();

        }

        public static void drawMCVerticalBuilding(double x, double y, double width, double height, int start, int end) {

            float f = (float) (start >> 24 & 255) / 255.0F;
            float f1 = (float) (start >> 16 & 255) / 255.0F;
            float f2 = (float) (start >> 8 & 255) / 255.0F;
            float f3 = (float) (start & 255) / 255.0F;
            float f4 = (float) (end >> 24 & 255) / 255.0F;
            float f5 = (float) (end >> 16 & 255) / 255.0F;
            float f6 = (float) (end >> 8 & 255) / 255.0F;
            float f7 = (float) (end & 255) / 255.0F;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(x, y, 0f).color(f5, f6, f7, f4).endVertex();
        }

        public static void drawVerticalBuilding(double x, double y, double width, double height, int start, int end) {

            width += x;
            height += y;

            float f = (float) (start >> 24 & 255) / 255.0F;
            float f1 = (float) (start >> 16 & 255) / 255.0F;
            float f2 = (float) (start >> 8 & 255) / 255.0F;
            float f3 = (float) (start & 255) / 255.0F;
            float f4 = (float) (end >> 24 & 255) / 255.0F;
            float f5 = (float) (end >> 16 & 255) / 255.0F;
            float f6 = (float) (end >> 8 & 255) / 255.0F;
            float f7 = (float) (end & 255) / 255.0F;


            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, height, 0f).color(f1, f2, f3, f).endVertex();
            bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
            bufferbuilder.pos(x, y, 0f).color(f5, f6, f7, f4).endVertex();

        }

        public static void drawTexture(final float x, final float y, final float width, final float height, final float radius, final float alpha) {
            pushMatrix();
            enableBlend();
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.TEXTURE_ROUND_SHADER.attach();

            ShaderUtils.TEXTURE_ROUND_SHADER.setUniform("rectSize", (float) (width * 2), (float) (height * 2));
            ShaderUtils.TEXTURE_ROUND_SHADER.setUniform("radius", radius * 2);
            ShaderUtils.TEXTURE_ROUND_SHADER.setUniform("alpha", alpha);

            quadsBegin(x, y, width, height, 7);


            ShaderUtils.TEXTURE_ROUND_SHADER.detach();
            popMatrix();
        }

        public static void quadsBegin(float x, float y, float width, float height, int glQuads) {
            buffer.begin(glQuads, POSITION_TEX);
            {
                buffer.pos(x, y, 0).tex(0, 0).endVertex();
                buffer.pos(x, y + height, 0).tex(0, 1).endVertex();
                buffer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
                buffer.pos(x + width, y, 0).tex(1, 0).endVertex();
            }
            tessellator.draw();
        }

        public static void quadsBeginC(float x, float y, float width, float height, int glQuads, Vector4i color) {
            buffer.begin(glQuads, POSITION_TEX_COLOR);
            {

                buffer.pos(x, y, 0).tex(0, 0).color(color.get(0)).endVertex();
                buffer.pos(x, y + height, 0).tex(0, 1).color(color.get(1)).endVertex();
                buffer.pos(x + width, y + height, 0).tex(1, 1).color(color.get(2)).endVertex();
                buffer.pos(x + width, y, 0).tex(1, 0).color(color.get(3)).endVertex();
            }
            tessellator.draw();
        }


        public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
            pushMatrix();
            enableBlend();
            ShaderUtils.ROUND_SHADER.attach();

            ShaderUtils.setupRoundedRectUniforms(x, y, width, height, radius, ShaderUtils.ROUND_SHADER);

            ShaderUtils.ROUND_SHADER.setUniform("blur", 0);
            ShaderUtils.ROUND_SHADER.setUniform("color", getRed(color) / 255f,
                    getGreen(color) / 255f,
                    getBlue(color) / 255f,
                    getAlpha(color) / 255f);

            ShaderUtils.ROUND_SHADER.drawQuads(x, y, width, height);

            ShaderUtils.ROUND_SHADER.detach();
            disableBlend();
            popMatrix();
        }
        public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color1, int color2) {
            pushMatrix();
            enableBlend();
            ShaderUtils.ROUND_SHADER.attach();

            // Pass the rectangle properties and the radius to the shader
            ShaderUtils.setupRoundedRectUniforms(x, y, width, height, radius, ShaderUtils.ROUND_SHADER);

            // Set uniform for color1 and color2 (convert the colors from int to normalized float)
            ShaderUtils.ROUND_SHADER.setUniform("color1", getRed(color1) / 255f,
                    getGreen(color1) / 255f,
                    getBlue(color1) / 255f,
                    getAlpha(color1) / 255f);

            ShaderUtils.ROUND_SHADER.setUniform("color2", getRed(color2) / 255f,
                    getGreen(color2) / 255f,
                    getBlue(color2) / 255f,
                    getAlpha(color2) / 255f);

            // Optional: you can add a blur effect if needed
            ShaderUtils.ROUND_SHADER.setUniform("blur", 0);

            // Draw the rectangle with the shader
            ShaderUtils.ROUND_SHADER.drawQuads(x, y, width, height);

            ShaderUtils.ROUND_SHADER.detach();
            disableBlend();
            popMatrix();
        }

        public static void drawRoundOutline(float x, float y, float width, float height, float radius, float outlineThickness, int color, Vector4i outlineColor) {
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.ROUND_SHADER_OUTLINE.attach();

            MainWindow sr = mc.getMainWindow();
            ShaderUtils.setupRoundedRectUniforms(x, y, width, height, radius, ShaderUtils.ROUND_SHADER_OUTLINE);

            float[] clr = RenderUtils.IntColor.rgb(color);
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("outlineThickness", (float) (outlineThickness * 2));
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("color", clr[0], clr[1], clr[2],clr[3]);

            for (int i = 0; i < 4;i++) {
                float[] col = RenderUtils.IntColor.rgb(outlineColor.get(i));
                ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("outlineColor" + (i + 1), col[0], col[1], col[2],col[3]);
            }

            ShaderUtils.ROUND_SHADER_OUTLINE.drawQuads(x - (2 + outlineThickness), y - (2 + outlineThickness), width + (4 + outlineThickness * 2), height + (4 + outlineThickness * 2));
            ShaderUtils.ROUND_SHADER_OUTLINE.detach();
            GlStateManager.disableBlend();
        }

        public static void drawRoundOut(float x, float y, float width, float height, float radius, float outlineThickness, int color, Vector4i outlineColor) {
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.ROUND_SHADER_OUTLINE.attach();

            MainWindow sr = mc.getMainWindow();
            ShaderUtils.setupRoundedRectUniforms(x, y, width, height, radius, ShaderUtils.ROUND_SHADER_OUTLINE);

            float[] clr = RenderUtils.IntColor.rgb(color);
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("outlineThickness", outlineThickness);  // Adjusted here
            ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("color", clr[0], clr[1], clr[2], clr[3]);

            for (int i = 0; i < 4; i++) {
                float[] col = RenderUtils.IntColor.rgb(outlineColor.get(i));
                ShaderUtils.ROUND_SHADER_OUTLINE.setUniform("outlineColor" + (i + 1), col[0], col[1], col[2], col[3]);
            }

            ShaderUtils.ROUND_SHADER_OUTLINE.drawQuads(x - (2 + outlineThickness), y - (2 + outlineThickness), width + (4 + outlineThickness * 2), height + (4 + outlineThickness * 2));
            ShaderUtils.ROUND_SHADER_OUTLINE.detach();
            GlStateManager.disableBlend();
        }

        public static void drawRoundedCorner(float x, float y, float width, float height, Vector4f vector4f) {
            pushMatrix();
            enableBlend();
            CORNER_ROUND_SHADER_TEXTURE.attach();

            CORNER_ROUND_SHADER_TEXTURE.setUniform("size", (float) (width * 2), (float) (height * 2));
            CORNER_ROUND_SHADER_TEXTURE.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

            CORNER_ROUND_SHADER_TEXTURE.setUniform("smoothness", 0.f, 1.5f);
            CORNER_ROUND_SHADER_TEXTURE.setUniformf("alpha",1);

            CORNER_ROUND_SHADER_TEXTURE.drawQuads(x, y, width, height);

            CORNER_ROUND_SHADER_TEXTURE.detach();
            disableBlend();
            popMatrix();
        }



        public static void drawRoundedCorner(float x, float y, float width, float height, float radius, int color) {
            pushMatrix();
            enableBlend();
            ShaderUtils.CORNER_ROUND_SHADER.attach();

            ShaderUtils.CORNER_ROUND_SHADER.setUniform("size", (float) (width * 2), (float) (height * 2));
            ShaderUtils.CORNER_ROUND_SHADER.setUniform("round", radius * 2, radius * 2, radius * 2, radius * 2);

            ShaderUtils.CORNER_ROUND_SHADER.setUniform("smoothness", 0.f, 1.5f);
            ShaderUtils.CORNER_ROUND_SHADER.setUniform("color",
                    getRed(color) / 255f,
                    getGreen(color) / 255f,
                    getBlue(color) / 255f,
                    IntColor.getAlpha(color) / 255f);

            ShaderUtils.CORNER_ROUND_SHADER.drawQuads(x, y, width, height);

            ShaderUtils.CORNER_ROUND_SHADER.detach();
            disableBlend();
            popMatrix();
        }

        public static void drawRoundedCorner(float x, float y, float width, float height, Vector4f vector4f, int color) {
            pushMatrix();
            enableBlend();
            ShaderUtils.CORNER_ROUND_SHADER.attach();

            ShaderUtils.CORNER_ROUND_SHADER.setUniform("size", (float) (width * 2), (float) (height * 2));
            ShaderUtils.CORNER_ROUND_SHADER.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

            ShaderUtils.CORNER_ROUND_SHADER.setUniform("smoothness", 0.f, 1.5f);
            ShaderUtils.CORNER_ROUND_SHADER.setUniform("color",
                    getRed(color) / 255f,
                    getGreen(color) / 255f,
                    getBlue(color) / 255f,
                    IntColor.getAlpha(color) / 255f);

            ShaderUtils.CORNER_ROUND_SHADER.drawQuads(x, y, width, height);

            ShaderUtils.CORNER_ROUND_SHADER.detach();
            disableBlend();
            popMatrix();
        }

        private static ShaderUtils rounded = new ShaderUtils("cornerGradient");

        public static void drawRoundedCorner(float x, float y, float width, float height, Vector4f vector4f, Vector4i color) {
            pushMatrix();
            enableBlend();
            rounded.attach();

            rounded.setUniform("size", (float) (width * 2), (float) (height * 2));
            rounded.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

            rounded.setUniform("smoothness", 0.f, 1.5f);

            for (int i = 0; i < 4;i++) {
                float[] col = RenderUtils.IntColor.rgb(color.get(i));
                rounded.setUniform("color" + (i + 1), col[0], col[1], col[2],col[3]);
            }

            rounded.drawQuads(x, y, width, height);

            rounded.detach();
            disableBlend();
            popMatrix();
        }




        public static void drawImageAlpha(ResourceLocation resourceLocation, float x, float y, float width, float height, Vector4i color) {
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            mc.getTextureManager().bindTexture(resourceLocation);
            buffer.begin(7, POSITION_TEX_COLOR);
            {
                buffer.pos(x, y, 0).tex(0, 1 - 0.01f).lightmap(0, 240).color(color.x).endVertex();
                buffer.pos(x, y + height, 0).tex(1, 1 - 0.01f).lightmap(0, 240).color(color.y).endVertex();
                buffer.pos(x + width, y + height, 0).tex(1, 0).lightmap(0, 240).color(color.z).endVertex();
                buffer.pos(x + width, y, 0).tex(0, 0).lightmap(0, 240).color(color.w).endVertex();

            }
            tessellator.draw();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            RenderSystem.depthMask(true);
            RenderSystem.popMatrix();
        }

        public static void drawImageAlph(ResourceLocation resourceLocation, float x, float y, float width, float height, Vector4i color) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x, (float) y, 0.0F);
            GlStateManager.rotatef(-90, 0.0F, 0.0F, 1.0F); // Используем сохраненный угол
            GlStateManager.translatef((float) -x, (float) -y, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 1);

            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            mc.getTextureManager().bindTexture(resourceLocation);
            buffer.begin(7, POSITION_TEX_COLOR);
            {
                buffer.pos(x, y, 0).tex(0, 1 - 0.01f).lightmap(0, 240).color(color.x).endVertex();
                buffer.pos(x, y + height, 0).tex(1, 1 - 0.01f).lightmap(0, 240).color(color.y).endVertex();
                buffer.pos(x + width, y + height, 0).tex(1, 0).lightmap(0, 240).color(color.z).endVertex();
                buffer.pos(x + width, y, 0).tex(0, 0).lightmap(0, 240).color(color.w).endVertex();

            }
            tessellator.draw();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            RenderSystem.depthMask(true);
            RenderSystem.popMatrix();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        public static void drawGradientRound(float x, float y, float width, float height, float radius, int bottomLeft, int topLeft, int bottomRight, int topRight) {
            RenderSystem.color4f(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.GRADIENT_ROUND_SHADER.attach();
            ShaderUtils.setupRoundedRectUniforms(x, y, width, height, radius, ShaderUtils.GRADIENT_ROUND_SHADER);

            ShaderUtils.GRADIENT_ROUND_SHADER.setUniform("color1", rgb(bottomLeft));
            ShaderUtils.GRADIENT_ROUND_SHADER.setUniform("color2", rgb(topLeft));
            ShaderUtils.GRADIENT_ROUND_SHADER.setUniform("color3", rgb(bottomRight));
            ShaderUtils.GRADIENT_ROUND_SHADER.setUniform("color4", rgb(topRight));

            ShaderUtils.GRADIENT_ROUND_SHADER.drawQuads(x -1,y -1,width + 2,height + 2);
            ShaderUtils.GRADIENT_ROUND_SHADER.detach();
            RenderSystem.disableBlend();
        }


        public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height, int color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            ColorUtils.setColor(color);
            mc.getTextureManager().bindTexture(resourceLocation);
            AbstractGui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }

        public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height, Vector4i color) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            mc.getTextureManager().bindTexture(resourceLocation);
            quadsBeginC(x,y,width,height, 7, color);
            RenderSystem.shadeModel(7424);
            RenderSystem.color4f(1, 1, 1, 1);
            RenderSystem.popMatrix();

        }

        public static void setColor(int color) {
            setColor(color, (float) (color >> 24 & 255) / 255.0F);
        }

        public static void setColor(int color, float alpha) {
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            RenderSystem.color4f(r, g, b, alpha);
        }

        public static void drawRoundFace(float x, float y, float width, float height, float radius, float alpha, AbstractClientPlayerEntity target) {
            try {
                ResourceLocation skin = target.getLocationSkin();
                mc.getTextureManager().bindTexture(skin);
                RenderSystem.pushMatrix();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(770, 771);
                ShaderUtils.FACE_ROUND.attach();
                ShaderUtils.FACE_ROUND.setUniform("location", x * 2, mw.getHeight() - height * 2 - y * 2);
                ShaderUtils.FACE_ROUND.setUniform("size", width * 2, height * 2);
                ShaderUtils.FACE_ROUND.setUniform("texture", 0);
                ShaderUtils.FACE_ROUND.setUniform("radius", radius * 2);
                ShaderUtils.FACE_ROUND.setUniform("alpha", alpha);
                ShaderUtils.FACE_ROUND.setUniform("u", (1f / 64) * 8);
                ShaderUtils.FACE_ROUND.setUniform("v", (1f / 64) * 8);
                ShaderUtils.FACE_ROUND.setUniform("w", 1f / 8);
                ShaderUtils.FACE_ROUND.setUniform("h", 1f / 8);
                quadsBegin(x, y, width, height, 7);
                ShaderUtils.FACE_ROUND.detach();
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void drawFace(float x, float y, float width, float height, AbstractClientPlayerEntity target) {
            try {
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                ResourceLocation skin = target.getLocationSkin();
                mc.getTextureManager().bindTexture(skin);
                float hurtPercent = getHurtPercent(target);
                GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
                AbstractGui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, width, height, 64, 64);
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void drawRectOutlineBuildingGradient(double x, double y, double width, double height, double size, int colors) {
            drawMCHorizontalBuilding(x + size, y, width - size, y + size, colors, colors);
            drawMCVerticalBuilding(x, y, x + size, height, colors, colors);

            drawMCVerticalBuilding(width - size, y, width, height, colors, colors);
            drawMCHorizontalBuilding(x + size, height - size, width - size, height, colors, colors);
        }

        private static final ShaderUtils CUSTOM_ROUNDED_GRADIENT = ShaderUtils.create("customRoundedGradient");


        public static void drawCustomGradientRoundedRect(MatrixStack stack, float x, float y, float width, float height, float radius1, float radius2, float radius3, float radius4, int bottomLeft, int topLeft, int bottomRight, int topRight) {
            MainWindow mw = mc.getMainWindow();
            CUSTOM_ROUNDED_GRADIENT.attach();
            CUSTOM_ROUNDED_GRADIENT.setUniformf("location", x * 2, (mw.getScaledHeight() - (height * 2)) - (y * 2));
            CUSTOM_ROUNDED_GRADIENT.setUniformf("rectSize", width * 2, height * 2);
            CUSTOM_ROUNDED_GRADIENT.setUniformf("radius", radius1, radius2, radius3, radius4);
            CUSTOM_ROUNDED_GRADIENT.setUniformf("color1", ColorUtils.rgba(bottomLeft));
            CUSTOM_ROUNDED_GRADIENT.setUniformf("color2", ColorUtils.rgba(topLeft));
            CUSTOM_ROUNDED_GRADIENT.setUniformf("color3", ColorUtils.rgba(bottomRight));
            CUSTOM_ROUNDED_GRADIENT.setUniformf("color4", ColorUtils.rgba(topRight));
            processDraw(() -> quadsBegin(x, y, width, height, 7));
            CUSTOM_ROUNDED_GRADIENT.detach();
        }

        public static void drawGradientRoundedRect(MatrixStack stack, float x, float y, float width, float height, float radius, int bottomLeft, int topLeft, int bottomRight, int topRight) {
            ROUNDED_GRADIENT.attach();
            ShaderUtils.setupRoundedRectUniforms(x, y, width, height, radius, ROUNDED_GRADIENT);
            ROUNDED_GRADIENT.setUniformf("color1", ColorUtils.rgba(bottomLeft));
            ROUNDED_GRADIENT.setUniformf("color2", ColorUtils.rgba(topLeft));
            ROUNDED_GRADIENT.setUniformf("color3", ColorUtils.rgba(bottomRight));
            ROUNDED_GRADIENT.setUniformf("color4", ColorUtils.rgba(topRight));
            processDraw(() -> quadsBegin(x, y, width, height, 7));
            ROUNDED_GRADIENT.detach();
        }

        public static void drawGradientRectCustom(MatrixStack stack, float x, float y, float width, float height, int color1, int color2, int color3, int color4) {
            float[] color1f = ColorUtils.rgba(color1);
            float[] color2f = ColorUtils.rgba(color2);
            float[] color3f = ColorUtils.rgba(color3);
            float[] color4f = ColorUtils.rgba(color4);
            Matrix4f matrix4f = stack.getLast().getMatrix();

            processDraw(() -> {
                GlStateManager.shadeModel(GL_SMOOTH);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(matrix4f, x, y + height, 0).color(color2f[0], color2f[1], color2f[2], color2f[3]).endVertex();
                buffer.pos(matrix4f, x + width, y + height, 0).color(color4f[0], color4f[1], color4f[2], color4f[3]).endVertex();
                buffer.pos(matrix4f, x + width, y, 0).color(color3f[0], color3f[1], color3f[2], color3f[3]).endVertex();
                buffer.pos(matrix4f, x, y, 0).color(color1f[0], color1f[1], color1f[2], color1f[3]).endVertex();
                tessellator.draw();
                GlStateManager.shadeModel(GL_FLAT);
            });
        }

        public static void drawGradientRectCustom(MatrixStack stack, float x, float y, float width, float height, int color1, int color2) {
            float[] color1f = ColorUtils.rgba(color2);
            float[] color2f = ColorUtils.rgba(color1);
            Matrix4f matrix4f = stack.getLast().getMatrix();

            processDraw(() -> {
                GlStateManager.shadeModel(GL_SMOOTH);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(matrix4f, x, y + height, 0).color(color1f[0], color1f[1], color1f[2], color1f[3]).endVertex();
                buffer.pos(matrix4f, x + width, y + height, 0).color(color1f[0], color1f[1], color1f[2], color1f[3]).endVertex();
                buffer.pos(matrix4f, x + width, y, 0).color(color2f[0], color2f[1], color2f[2], color2f[3]).endVertex();
                buffer.pos(matrix4f, x, y, 0).color(color2f[0], color2f[1], color2f[2], color2f[3]).endVertex();
                tessellator.draw();
                GlStateManager.shadeModel(GL_FLAT);
            });
        }
    }

    public static void processDraw(Runnable runnable) {
        GlStateManager.clearCurrentColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        runnable.run();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.clearCurrentColor();
    }

    public static class Render3D {

        public static void drawBlockBox(BlockPos blockPos, int color) {
            drawBox(new AxisAlignedBB(blockPos).offset(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z), color);
        }
        public static void drawBlockBoxSkull(BlockPos blockPos, int color) {
            drawBoxSkull(new AxisAlignedBB(blockPos).offset(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z), color);
        }
        public static void drawBoxVectorElytra(AxisAlignedBB bb, int color) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1);

            float[] rgb = IntColor.rgb(color);
            GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();

            double minX = bb.minX;
            double minY = bb.minY;
            double minZ = bb.minZ;
            double maxX = minX + 0.5;
            double maxY = minY + 0.5;
            double maxZ = minZ + 0.5;

            vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(minX, minY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, minY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, minY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(minX, minY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(minX, minY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();

            // Draw top face
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(minX, maxY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, maxY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, maxY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(minX, maxY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(minX, maxY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();

            vertexbuffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(minX, minY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(minX, maxY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, minY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, maxY, minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, minY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(maxX, maxY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(minX, minY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(minX, maxY, maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL_LINE_SMOOTH);
            GL11.glPopMatrix();
        }
        public static void drawBox(AxisAlignedBB bb, int color) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL_DEPTH_TEST);
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glLineWidth(1);
            float[] rgb = IntColor.rgb(color);
            GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();
            vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();
            GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL_DEPTH_TEST);
            GL11.glDisable(GL_LINE_SMOOTH);
            GL11.glPopMatrix();

        }
        public static void drawBoxSkull(AxisAlignedBB bb, int color) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1);
            float[] rgb = IntColor.rgb(color);
            GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);

            double skullWidth = 0.5;  // Ширина головы
            double skullHeight = 0.5; // Высота головы
            double skullDepth = 0.5;  // Глубина головы

            // Центрируем голову относительно исходного AABB
            double centerX = (bb.minX + bb.maxX) / 2.0;
            double centerY = (bb.minY + bb.maxY) / 2.0 - 0.25;
            double centerZ = (bb.minZ + bb.maxZ) / 2.0;

            AxisAlignedBB skullBB = new AxisAlignedBB(
                    centerX - skullWidth / 2, centerY - skullHeight / 2, centerZ - skullDepth / 2,
                    centerX + skullWidth / 2, centerY + skullHeight / 2, centerZ + skullDepth / 2
            );

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();

            // Рендер нижней грани
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(skullBB.minX, skullBB.minY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.minY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.minY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.minX, skullBB.minY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.minX, skullBB.minY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();

            // Рендер верхней грани
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(skullBB.minX, skullBB.maxY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.maxY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.maxY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.minX, skullBB.maxY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.minX, skullBB.maxY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();

            // Рендер вертикальных рёбер
            vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(skullBB.minX, skullBB.minY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.minX, skullBB.maxY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.minY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.maxY, skullBB.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.minY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.maxX, skullBB.maxY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.minX, skullBB.minY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            vertexbuffer.pos(skullBB.minX, skullBB.maxY, skullBB.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
            tessellator.draw();

            GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL_LINE_SMOOTH);
            GL11.glPopMatrix();
        }
    }
    public static void drawMcRect(double left,
                                  double top,
                                  double right,
                                  double bottom,
                                  int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawLine(double x,
                                double y,
                                double z,
                                double w,
                                int color) {


        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        GL11.glEnable(GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1.5f);
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(z, w, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        GL11.glDisable(GL_LINE_SMOOTH);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }




    public static void drawMcRectBuilding(double left,
                                          double top,
                                          double right,
                                          double bottom,
                                          int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        //bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
        //bufferbuilder.finishDrawing();
        //WorldVertexBufferUploader.draw(bufferbuilder);
    }

    public static void drawRectBuilding(double left,
                                        double top,
                                        double right,
                                        double bottom,
                                        int color) {
        right += left;
        bottom += top;

        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        //bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
        //bufferbuilder.finishDrawing();
        //WorldVertexBufferUploader.draw(bufferbuilder);
    }


    public static void drawRectOutlineBuilding(double x, double y, double width, double height, double size, int color) {
        drawMcRectBuilding(x + size, y, width - size, y + size, color);
        drawMcRectBuilding(x, y, x + size, height, color);
        drawMcRectBuilding(width - size, y, width, height, color);
        drawMcRectBuilding(x + size, height - size, width - size, height, color);
    }
    public static void drawRectOutlineBuildingGradient(double x, double y, double width, double height, double size, Vector4i colors) {
        drawMCHorizontalBuilding(x + size, y, width - size, y + size, colors.x, colors.z);
        drawMCVerticalBuilding(x, y, x + size, height, colors.z, colors.x);

        drawMCVerticalBuilding(width - size, y, width, height, colors.x, colors.z);
        drawMCHorizontalBuilding(x + size, height - size, width - size, height, colors.z, colors.x);
    }
    public static void drawMCVerticalBuilding(double x,
                                              double y,
                                              double width,
                                              double height,
                                              int start,
                                              int end) {

        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

//            RenderSystem.disableTexture();
//            RenderSystem.enableBlend();
//            RenderSystem.disableAlphaTest();
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        //bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f5, f6, f7, f4).endVertex();

        //tessellator.draw();
//            RenderSystem.shadeModel(7424);
//            RenderSystem.disableBlend();
//            RenderSystem.enableAlphaTest();
//            RenderSystem.enableTexture();

    }
    public static void drawMCHorizontal(double x,
                                        double y,
                                        double width,
                                        double height,
                                        int start,
                                        int end) {


        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();

        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void drawMCHorizontalBuilding(double x,
                                                double y,
                                                double width,
                                                double height,
                                                int start,
                                                int end) {


        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        //bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();

        //tessellator.draw();

    }


    public static void drawHorizontal(float x,
                                      float y,
                                      float width,
                                      float height,
                                      int start,
                                      int end) {

        width += x;
        height += y;

        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();

        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }
    public static class SmartScissor {
        private static class State implements Cloneable {
            public boolean enabled;
            public int transX;
            public int transY;
            public int x;
            public int y;
            public int width;
            public int height;

            @Override
            public State clone() {
                try {
                    return (State) super.clone();
                } catch (CloneNotSupportedException e) {
                    throw new AssertionError(e);
                }
            }
        }

        private static State state = new State();

        private static final List<State> stateStack = Lists.newArrayList();

        public static void push() {
            stateStack.add(state.clone());
            GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        }

        public static void pop() {
            state = stateStack.remove(stateStack.size() - 1);
            GL11.glPopAttrib();
        }

        public static void unset() {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            state.enabled = false;
        }

        public static void setFromComponentCoordinates(double x, double y, double width, double height) {
            int scaleFactor = 2;

            int screenX = (int) (x * scaleFactor);
            int screenY = (int) (y * scaleFactor);
            int screenWidth = (int) (width * scaleFactor);
            int screenHeight = (int) (height * scaleFactor);
            screenY = mc.getMainWindow().getHeight() - screenY - screenHeight;
            set(screenX, screenY, screenWidth, screenHeight);
        }

        public static void set(int x, int y, int width, int height) {
            Rectangle screen = new Rectangle(0, 0, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight());
            Rectangle current;
            if (state.enabled) {
                current = new Rectangle(state.x, state.y, state.width, state.height);
            } else {
                current = screen;
            }
            Rectangle target = new Rectangle(x + state.transX, y + state.transY, width, height);
            Rectangle result = current.intersection(target);
            result = result.intersection(screen);
            if (result.width < 0) result.width = 0;
            if (result.height < 0) result.height = 0;
            state.enabled = true;
            state.x = result.x;
            state.y = result.y;
            state.width = result.width;
            state.height = result.height;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(result.x, result.y, result.width, result.height);
        }

        public static void translate(int x, int y) {
            state.transX = x;
            state.transY = y;
        }
    }
}
