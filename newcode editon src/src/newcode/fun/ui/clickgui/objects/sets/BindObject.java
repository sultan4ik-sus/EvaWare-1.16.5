package newcode.fun.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import newcode.fun.module.settings.imp.BindSetting;
import newcode.fun.utils.SoundUtils;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.clickgui.objects.Object;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.font.Fonts;

import java.util.Random;

public class BindObject extends Object {

    public BindSetting set;
    public ModuleObject object;
    boolean bind;

    public boolean isBinding;

    private static final int MOUSE_BUTTON_3 = 3;
    private static final int MOUSE_BUTTON_4 = 4;

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        height -= 3;

        String bindString = bind ? "..." : (set.getKey() == 0 ? "NONE" : ClientUtils.getKey(set.getKey()));

        if (bindString == null) {
            bindString = "";
        }

        bindString = bindString.replace("MOUSE", "M");
        bindString = bindString.replace("LEFT", "L");
        bindString = bindString.replace("RIGHT", "R");
        bindString = bindString.replace("CONTROL", "C");
        bindString = bindString.replace("SHIFT", "S");
        bindString = bindString.replace("_", "");


        String shortBindString = bindString.substring(0, Math.min(bindString.length(), 4));

        float width = Fonts.newcode[14].getWidth(shortBindString) + 4;

        RenderUtils.Render2D.drawRoundedRect(x + 12, y + 1, width, 10, 2, ColorUtils.rgba(20, 21, 24, 175));
        Fonts.newcode[12].drawCenteredString(matrixStack, shortBindString, x + 12 + (width / 2), y + 5, -1);
        Fonts.newcode[12].drawString(matrixStack, set.getName(), x + 12 + width + 3, y + 5, -1);
    }


    public BindObject(ModuleObject object, BindSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (RenderUtils.isInRegion(mouseX, mouseY, x + 12, y + 2, width + 6, 13)) {
                if (bind && (mouseButton > 1 || mouseButton == MOUSE_BUTTON_3 || mouseButton == MOUSE_BUTTON_4)) {
                    set.setKey(-100 + mouseButton);
                    bind = false;

                }
                if (isHovered(mouseX, mouseY) && mouseButton == 0) {
                    bind = true;
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
        if (bind) {
            if (keyCode == 261 || keyCode == 259) {
                set.setKey(0);
                bind = false;
                return;
            }
            set.setKey(keyCode);
            bind = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}