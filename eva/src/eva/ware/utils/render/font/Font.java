package eva.ware.utils.render.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import eva.ware.utils.client.IMinecraft;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.shader.ShaderUtility;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;

public class Font implements IMinecraft {

    private final String atlas;
    private final String data;

    private final MsdfFont font;

    public Font(String atlas, String data) {
        this.atlas = atlas;
        this.data = data;

        font = MsdfFont.builder().withAtlas(atlas).withData(data).build();

    }

    public void drawText(MatrixStack stack, String text, float x, float y, int color, float size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderUtility shader = ShaderUtility.textShader;

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.attach();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", 0f);
        shader.setUniform("Outline", 0); // boolean
        shader.setUniform("OutlineThickness", 0f);

        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);
        shader.setUniform("color", ColorUtility.rgba(color));

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, 0,
                x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        Tessellator.getInstance().draw();

        this.font.unbind();
        shader.detach();
    }
    // РАДИАЦИЯ ОПАСНО ОПАСНО!!!!

    public void drawText(MatrixStack stack, ITextComponent text, float x, float y, float size, int alpha) {
        float offset = 0;
        for (ITextComponent it : text.getSiblings()) {

            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();

                if (it1.getStyle().getColor() != null) {
                    drawText(stack, draw, x + offset, y, ColorUtility.setAlpha(ColorUtility.toColor(it1.getStyle().getColor().getHex()), alpha), size);
                } else
                    drawText(stack, draw, x + offset, y, ColorUtility.setAlpha(Color.GRAY.getRGB(), alpha), size);
                offset += getWidth(draw, size);
            }
            if (it.getSiblings().size() <= 1) {
                String draw = TextFormatting.getTextWithoutFormattingCodes(it.getString());

                drawText(stack, draw, x + offset, y, ColorUtility.setAlpha(it.getStyle().getColor() == null ? Color.GRAY.getRGB() : it.getStyle().getColor().getColor(), alpha), size);
                offset += getWidth(draw, size);
            }



        }
        if (text.getSiblings().isEmpty()) {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());

            drawText(stack, draw, x + offset, y, ColorUtility.setAlpha(text.getStyle().getColor() == null ? Color.GRAY.getRGB() : text.getStyle().getColor().getColor(), alpha), size);
            offset += getWidth(draw, size);
        }
    }

    public float getWidth(ITextComponent text, float size) {
        float offset = 0;
        for (ITextComponent it : text.getSiblings()) {

            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();
                offset += getWidth(draw, size);
            }
            if (it.getSiblings().size() <= 1) {
                String draw = TextFormatting.getTextWithoutFormattingCodes(it.getString());
                offset += getWidth(draw, size);
            }



        }
        if (text.getSiblings().isEmpty()) {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());
            offset += getWidth(draw, size);
        }
        return offset;
    }

    public void drawTextBuilding(MatrixStack stack, String text, float x, float y, int color, float size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderUtility shader = ShaderUtility.textShader;

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.attach();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", 0f);
        shader.setUniform("Outline", 0); // boolean
        shader.setUniform("OutlineThickness", 0f);

        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);
        shader.setUniform("color", ColorUtility.rgba(color));

        this.font.bind();
        GlStateManager.enableBlend();
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, 0, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        this.font.unbind();
        shader.detach();
    }

    public void drawCenteredText(MatrixStack stack, String text, float x, float y, int color, float size) {

        drawText(stack, text, x - getWidth(text, size) / 2f, y, color, size);
    }

    public void drawCenteredText(MatrixStack stack, ITextComponent text, float x, float y, float size) {
        drawText(stack, text, x - getWidth(text, size) / 2f, y, size, 255);
    }


    public void drawCenteredTextWithOutline(MatrixStack stack, String text, float x, float y, int color, float size) {
        drawTextWithOutline(stack, text, x - getWidth(text, size) / 2f, y, color, size, 0.05f);
    }

    public void drawCenteredTextEmpty(MatrixStack stack, String text, float x, float y, int color, float size) {
        drawEmpty(stack, text, x - getWidth(text, size) / 2f, y, size, color, 0);
    }

    public void drawCenteredTextEmptyOutline(MatrixStack stack, String text, float x, float y, int color, float size) {
        drawEmptyWithOutline(stack, text, x - getWidth(text, size) / 2f, y, size, color, 0);
    }

    public void drawCenteredText(MatrixStack stack, String text, float x, float y, int color, float size, float thickness) {
        drawText(stack, text, x - getWidth(text, size, thickness) / 2f, y, color, size, thickness);
    }

    public void drawText(MatrixStack stack, String text, float x, float y, int color, float size, float thickness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderUtility shader = ShaderUtility.textShader;

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.attach();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", thickness);
        shader.setUniform("color", ColorUtility.rgba(color));
        shader.setUniform("Outline", 0); // boolean
        shader.setUniform("OutlineThickness", 0f);

        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        Tessellator.getInstance().draw();

        this.font.unbind();
        shader.detach();
    }

    public void drawTextWithOutline(MatrixStack stack, String text, float x, float y, int color, float size, float thickness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderUtility shader = ShaderUtility.textShader;

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.attach();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", thickness);
        shader.setUniform("color", ColorUtility.rgba(color));
        shader.setUniform("Outline", 1); // boolean
        shader.setUniform("OutlineThickness", 0.2f);

        shader.setUniform("OutlineColor", 0f, 0f, 0f, 1f);

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness,
                x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        Tessellator.getInstance().draw();

        this.font.unbind();
        shader.detach();
    }

    public void init(float thickness, float smoothness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderUtility shader = ShaderUtility.textShader;

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.attach();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", smoothness);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", thickness);
        shader.setUniform("Outline", 0); // boolean
        shader.setUniform("OutlineThickness", 0f);

        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);

    }

    public void drawEmpty(MatrixStack stack, String text, float x, float y, float size, int color, float thickness) {

        ShaderUtility shader = ShaderUtility.textShader;

        shader.setUniform("color", ColorUtility.rgba(color));
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness,
                x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
    }

    public void drawEmptyWithOutline(MatrixStack stack, String text, float x, float y, float size, int color,
                                     float thickness) {

        ShaderUtility shader = ShaderUtility.textShader;
        shader.setUniform("Outline", 1); // boolean
        shader.setUniform("OutlineThickness", 0.2f);

        shader.setUniform("OutlineColor", 0f, 0f, 0f, 1f);
        shader.setUniform("color", ColorUtility.rgba(color));
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness,
                x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
    }

    public void end() {

        Tessellator.getInstance().draw();
        this.font.unbind();
        ShaderUtility shader = ShaderUtility.textShader;
        shader.detach();
    }

    public float getWidth(String text, float size) {
        return font.getWidth(text, size);
    }

    public float getWidth(String text, float size, float thickness) {
        return font.getWidth(text, size, thickness);
    }

    public float getHeight(float size) {
        return size;
    }

}
