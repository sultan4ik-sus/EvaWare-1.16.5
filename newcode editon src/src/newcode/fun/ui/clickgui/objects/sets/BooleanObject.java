package newcode.fun.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import newcode.fun.control.Manager;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.SoundUtils;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.animation.AnimationMath;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.clickgui.objects.Object;
import newcode.fun.utils.font.Fonts;
import org.joml.Vector4i;

import java.awt.*;
import java.util.Random;

public class BooleanObject extends Object {

    public ModuleObject object;
    public BooleanOption set;
    public float enabledAnimation;

    public BooleanObject(ModuleObject object, BooleanOption set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        y-=3;
        double max = !set.get() ? 0 : 8.5f;
        this.enabledAnimation = AnimationMath.fast(enabledAnimation, (float) max, 10);
        int colorfont = ColorUtils.interpolateColor(ColorUtils.rgba(255, 255, 255, 100), Color.WHITE.getRGB(), enabledAnimation / 6.5f);
        int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
        Fonts.newcode[12].drawString(stack, set.getName(), x + 24, y + 7f, colorfont);
        RenderUtils.Render2D.drawRoundedRect(x + 12,y + 3,9, 9, 2, ColorUtils.rgba(20, 21, 24, 145));
        int color2 = ColorUtils.interpolateColor(ColorUtils.setAlpha(-1, 100), firstColor2, enabledAnimation / 6.5f);
        RenderUtils.Render2D.drawRoundOutline(x + 12,y + 3,9, 9,  2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(color2, color2, color2, color2));
        int color = ColorUtils.interpolateColor(ColorUtils.rgba(42, 56, 73, 0), ColorUtils.setAlpha(-1, 200), enabledAnimation / 6.5f);
        RenderUtils.Render2D.drawRoundedRect(x + 14.5f,y + 5.5f,4,4, 2, color);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (mouseButton == 0) {
                if (isHovered(mouseX, mouseY)) {
                    set.toggle();
                    int volume = new Random().nextInt(13) + 59;
                    if (Manager.FUNCTION_MANAGER.clickGui.sounds.get()) {
                        SoundUtils.playSound("select.wav", volume, false);
                    }
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
