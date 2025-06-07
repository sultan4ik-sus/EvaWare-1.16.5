package newcode.fun.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import newcode.fun.control.Manager;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.math.MathUtil;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.animation.AnimationMath;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.clickgui.objects.Object;
import newcode.fun.utils.font.Fonts;
import org.joml.Vector2i;

import java.awt.*;

import static org.joml.Math.lerp;

public class SliderObject extends Object {

    public ModuleObject object;
    public SliderSetting set;
    public boolean sliding;

    public float animatedVal;
    public float animatedThumbX;
    private float slider = 0;

    public SliderObject(ModuleObject object, SliderSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
        animatedThumbX = x + 10 + 3 + ((set.getValue().floatValue() - set.getMin()) / (set.getMax() - set.getMin())) * (width - 20); // Инициализация
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        int firstColor = ColorUtils.getColorStyle(0);
        int secondColor = ColorUtils.getColorStyle(90);
        Vector2i colorVec = new Vector2i(firstColor, secondColor);
        y -= 0;

        if (sliding) {
            float value = (float) ((mouseX - (x + 10 + 3)) / (width - 30) * (set.getMax() - set.getMin()) + set.getMin());
            value = (float) MathUtil.round(value, set.getIncrement());
            set.setValue(value);
        }

        int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());

        float sliderWidth = ((set.getValue().floatValue() - set.getMin()) / (set.getMax() - set.getMin())) * (width - 27) + 3f; // Измените -20 на -40
        animatedVal = AnimationMath.fast(animatedVal, sliderWidth, 11);

        float targetThumbX = x + 10 + 3 + animatedVal;
        animatedThumbX = AnimationMath.fast(animatedThumbX, targetThumbX, 11);

        RenderUtils.Render2D.drawRoundedRect(x + 10 + 3, y + height / 2f + 3, width - 34 + 9, 1.3f, 0.5f, ColorUtils.rgba(128, 133, 152, 128)); // Измените -24 на -34
        RenderUtils.Render2D.drawRoundedRect(x + 10 + 3, y + height / 2f + 3, animatedVal - 5 + 2, 1.3f, 0.5f, firstColor2);

        RenderUtils.Render2D.drawRoundedRect(x + 10 + 3 + animatedVal - 5 + 1, y + height / 2f + 2.2f, 3, 3f, 1, -1);

        float slidert = Float.parseFloat(String.valueOf(set.getValue().floatValue()));
        slider = lerp(slider, slidert, 0.001f);

        Fonts.newcode[11].drawString(stack, set.getName(), x + 12f, y + height / 2f - 4, Color.WHITE.getRGB()); // Сдвинуть текст на 3 пикселя вправо
        Fonts.newcode[11].drawString(stack, String.valueOf(slidert), x + width - 10.6f - Fonts.newcode[11].getWidth(String.valueOf(set.getValue().floatValue())), y + height / 2f - 4, Color.WHITE.getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (isHovered(mouseX, mouseY)) {
                sliding = true;
            }
        }
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {

    }

    @Override
    public void exit() {
        super.exit();
        sliding = false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        sliding = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
