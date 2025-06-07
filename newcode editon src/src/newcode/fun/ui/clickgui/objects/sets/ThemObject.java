package newcode.fun.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import newcode.fun.control.Manager;
import newcode.fun.module.settings.imp.ThemSetting;
import newcode.fun.ui.midnight.Style;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.clickgui.objects.Object;
import newcode.fun.utils.font.Fonts;
import org.joml.Vector2i;
import org.joml.Vector4i;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static newcode.fun.utils.IMinecraft.mc;

public class ThemObject extends Object {
    public ThemSetting set;
    public ModuleObject object;

    public ThemObject(ModuleObject object, ThemSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }
    private void closeGui() {
        if (mc.currentScreen != null) {
            mc.displayGuiScreen(null);
        }
    }
    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        Vector2i colorVec = new Vector2i(ColorUtils.getColorStyle(0), ColorUtils.getColorStyle(90));
        int offset = 0;
        float offsetY = 0;
        float size = 0;

        // Calculate the total width for the modes
        for (String mode : set.modes) {
            float preOffset = size + 10;
            if (preOffset > width - 20) {
                break;
            }
            size += 10;
        }

        height -= 1;
        Fonts.newcode[13].drawString(stack, set.getName(), x + 12, y + height / 2f - 5.5f, ColorUtils.rgba(240, 240, 240, 255));

        height += 19;
        offset = 0;
        offsetY = 0;
        int i = 0;

        for (String mode : set.modes) {
            float preOff = offset + 10;
            if (preOff > size) {
                offset = 0;
                offsetY += 10f;
            }

            int color = ColorUtils.rgba(255, 255, 255, 255);  // Default color

            // Сопоставление стилей с цветами
            Map<String, String> styleColors = new HashMap<>();
            styleColors.put("1", "#a5baed");
            styleColors.put("2", "#15c9de");
            styleColors.put("3", "#6a83dd");
            styleColors.put("4", "#7444cd");
            styleColors.put("5", "#FFC8C8");
            styleColors.put("6", "#FFE8C1");
            styleColors.put("7", "#c9c1f8");
            styleColors.put("8", "#4ee4b4");
            styleColors.put("9", "#cf125e");
            styleColors.put("10", "#ffffff");
            styleColors.put("11", "#bf5d5d");
            styleColors.put("12", "#434566");
            styleColors.put("13", "astolfo");
            styleColors.put("14", "rgba(255, 255, 255, 255)");

            if (styleColors.containsKey(mode)) {
                for (Style style : Manager.STYLE_MANAGER.styles) {
                    if (style.name.equals(mode)) {
                        if (mode.equals("13")) {
                            color = ColorUtils.astolfo(10, 1, 0.5F, 1.0F, 1.0F);
                        } else if (mode.equals("14")) {
                            color = ColorUtils.rgba(255, 255, 255, 255);
                        } else {
                            color = StyleManager.HexColor.toColor(styleColors.get(mode));
                        }
                    }
                }
            }


            if (set.getIndex() == i) {
                int finalOffset = offset;
                float finalOffsetY = offsetY;

                RenderUtils.Render2D.drawRoundedRect(x + 13f + finalOffset, y + 9f + finalOffsetY, 9.5f, 9.5f, 2f, color);
                RenderUtils.Render2D.drawRoundOutline(x + 13f + finalOffset, y + 9f + finalOffsetY, 9.5f, 9.5f, 2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(120, 120, 120, 192).getRGB(), new Color(120, 120, 120, 192).getRGB(), new Color(120, 120, 120, 192).getRGB(), new Color(120, 120, 120, 192).getRGB()));
            } else {
                RenderUtils.Render2D.drawRoundedRect(x + 13f + offset, y + 9f + offsetY, 9.5f, 9.5f, 2f, color);
                RenderUtils.Render2D.drawRoundOutline(x + 13f + offset, y + 9f + offsetY, 9.5f, 9.5f, 2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB()));
            }

            offset += 10;
            i++;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float offset = 0;
        float offsetY = 0;
        int i = 0;
        float size = 0;
        if (object.module.expanded) {
            for (String mode : set.modes) {
                float preOffset = size + 10;
                if (preOffset > width - 20) {
                    break;
                }
                size += 10;
            }
        }

        if (object.module.expanded) {
            for (String mode : set.modes) {
                float preOff = offset + 10;
                if (preOff > size) {
                    offset = 0;
                    offsetY += 10;
                }

                if (RenderUtils.isInRegion(mouseX, mouseY, x + 13f + offset, y + 9f + offsetY, 9.5f, 9.5f)) {
                    set.setIndex(i);
                    closeGui();
                }

                offset += 10;
                i++;
            }
        }
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
}
