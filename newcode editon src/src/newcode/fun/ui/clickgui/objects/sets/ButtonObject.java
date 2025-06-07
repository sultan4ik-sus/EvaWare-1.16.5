package newcode.fun.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import newcode.fun.module.settings.imp.ButtonSetting;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.clickgui.objects.Object;
import newcode.fun.utils.font.Fonts;

import java.awt.*;

public class ButtonObject extends Object {

    public ButtonSetting set;
    public ModuleObject object;

    public ButtonObject(ModuleObject object, ButtonSetting set) {
        this.object = object;
        this.set = set;
        setting = set;

    }
    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);

        float wwidth = Math.max(10, Fonts.newcode[13].getWidth(set.getName()) + 4);
        RenderUtils.Render2D.drawGradientRound(x + width - wwidth - 27,y + 2, wwidth,8, 1f, new Color(21, 21, 21, 205).getRGB(), new Color(21, 21, 21, 205).getRGB(), new Color(21, 21, 21, 205).getRGB(), new Color(21, 21, 21, 205).getRGB());

        Fonts.newcode[13].drawCenteredString(stack, set.getName(), x + width - wwidth - 27  + wwidth / 2f, y + height / 1.5f - Fonts.newcode[13].getFontHeight() / 2f + 2, Color.WHITE.getRGB());

    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (isHovered(mouseX,mouseY)) {
                float wwidth = Math.max(10, Fonts.newcode[13].getWidth(set.getName()) + 4);
                if (RenderUtils.isInRegion(mouseX,mouseY,x + width - wwidth - 27,y + 2, wwidth,10)) {
                    set.getRun().run();
                }
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
