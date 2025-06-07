package newcode.fun.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import newcode.fun.control.Manager;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.utils.SoundUtils;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.clickgui.objects.Object;
import newcode.fun.utils.font.Fonts;
import org.joml.Vector2i;

import java.util.Random;

public class ModeObject extends Object {
    public ModeSetting set;
    public ModuleObject object;

    public ModeObject(ModuleObject object, ModeSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        int firstColor = ColorUtils.getColorStyle(0);
        int secondColor = ColorUtils.getColorStyle(90);
        Vector2i colorVec = new Vector2i(ColorUtils.getColorStyle(0), ColorUtils.getColorStyle(90));
        int offset = 0;
        float offsetY = 0;
        int lines = 1;
        float size = 0;
        for (String mode : set.modes) {

            float preOffset = size + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOffset > width - 20) {
                break;
            }
            size += Fonts.msSemiBold[11].getWidth(mode) + 3;
        }

        for (String mode : set.modes) {
            float preOffset = offset + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOffset > size) {
                lines++;
                offset = 0;
            }
            offset += Fonts.msSemiBold[11].getWidth(mode) + 3;
        }

        height += 5;
        Fonts.newcode[13].drawString(stack, set.getName(), x + 12, y + height / 2f - 5.5f, ColorUtils.rgba(240, 240, 240, 255));

        RenderUtils.Render2D.drawRoundedRect(x + 10.9f, y + 9, 73.6F, 10.3f * lines, 1.5f, ColorUtils.rgba(20, 20, 20, 165));
        height += 11 * (lines - 1);
        offset = 0;
        offsetY = 0;
        int i = 0;
        for (String mode : set.modes) {

            float preOff = offset + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOff > size) {
                offset = 0;
                offsetY += 10;
            }
            if (set.getIndex() == i) {
                int finalOffset = offset;
                float finalOffsetY = offsetY;

                Fonts.msSemiBold[11].drawString(stack, mode, x + 14 + finalOffset, y + 14f + finalOffsetY, ColorUtils.rgba(119, 121, 134, 255));
                Fonts.msSemiBold[11].drawString(stack, mode, x + 14 + offset, y + 14f + offsetY, -1);
            } else
                Fonts.msSemiBold[11].drawString(stack, mode, x + 14 + offset, y + 14f + offsetY, ColorUtils.rgba(163, 163, 163, 255));
            offset += Fonts.msSemiBold[11].getWidth(mode) + 3;
            i++;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float offset = -4;
        float offsetY = 0;
        int i = 0;
        float size = 0;
        if (object.module.expanded) {
        for (String mode : set.modes) {

            float preOffset = size + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOffset > width - 20) {
                break;
            }
            size += Fonts.msSemiBold[11].getWidth(mode) + 3;
        }
        }
            if (object.module.expanded) {
        for (String mode : set.modes) {
            float preOff = offset + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOff > size) {
                offset = -4;
                offsetY += 11;
            }
            if (RenderUtils.isInRegion(mouseX, mouseY, x + 15 + offset, y + 12f + offsetY, Fonts.newcode[11].getWidth(mode), Fonts.newcode[11].getFontHeight() / 2f + 3)) {
                set.setIndex(i);
                int volume = new Random().nextInt(13) + 59;
                if (Manager.FUNCTION_MANAGER.clickGui.sounds.get()) {
                    SoundUtils.playSound("select.wav", volume, false);
                }
            }

            offset += Fonts.msSemiBold[11].getWidth(mode) + 3;
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
