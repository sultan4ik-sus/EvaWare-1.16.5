package eva.ware.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import eva.ware.manager.Theme;
import eva.ware.ui.clickgui.ClickGuiScreen;
import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.render.Cursors;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.render.engine2d.RenderUtility;
import net.minecraft.client.Minecraft;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import eva.ware.ui.clickgui.components.builder.Component;

public class MultiBoxComponent extends Component {

    final ModeListSetting setting;

    float width = 0;
    float heightPadding = 0;
    float spacing = 3;

    private final Map<CheckBoxSetting, Animation> animations = new HashMap<>();

    public MultiBoxComponent(ModeListSetting setting) {
        this.setting = setting;
        setHeight(22);
        for (CheckBoxSetting checkBoxSetting : setting.getValue()) {
            animations.put(checkBoxSetting, new Animation());
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 2, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), 5.5f, 0.05f);
        float offset = 0;
        float heightoff = 0;
        boolean anyHovered = false;
        for (CheckBoxSetting text : setting.getValue()) {
            Animation animation = animations.get(text);
            animation.update();
            animation.animate(text.getValue() ? 1 : 0, 1, Easings.EXPO_OUT);
            
            int interpolateColor = ColorUtility.interpolateColor(Theme.mainRectColor, ColorUtility.rgba(25, 25, 25, 180), (float) animation.getValue());
            int color = ColorUtility.setAlpha(interpolateColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));
            
            int textColorInterpolate = ColorUtility.interpolateColor(Theme.textColor, Theme.darkTextColor, (float) animation.getValue());
            int finalTextColor = ColorUtility.setAlpha(textColorInterpolate, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));
            
            float textWidth = Fonts.montserrat.getWidth(text.getName(), 5.5f, 0.05f) + 2;
            float textHeight = Fonts.montserrat.getHeight(5.5f) + 1;
            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing;
            }
            if (MathUtility.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10f + heightoff, textWidth, textHeight)) {
                anyHovered = true;
            }

            RenderUtility.drawShadow(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);
            RenderUtility.drawRoundedRect(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);

            Fonts.montserrat.drawText(stack, text.getName(), getX() + 8 + offset, getY() + 11.5f + heightoff, finalTextColor, 5.5f, !text.getValue() ? 0.05f : 0.07f);
            
            offset += textWidth + spacing;
        }
        if (isHovered(mouseX, mouseY)) {
            if (anyHovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
        width = getWidth() - 15;
        setHeight(22 + heightoff);
        heightPadding = heightoff;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float offset = 0;
        float heightoff = 0;
        for (CheckBoxSetting text : setting.getValue()) {
            float textWidth = Fonts.montserrat.getWidth(text.getName(), 5.5f, 0.05f) + 2;
            float textHeight = Fonts.montserrat.getHeight(5.5f) + 1;
            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing;
            }
            if (mouse == 0 && MathUtility.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10 + heightoff, Fonts.montserrat.getWidth(text.getName(), 5.5f, 0.05f), Fonts.montserrat.getHeight(5.5f) + 1)) {
                text.setValue(!text.getValue());
                SoundPlayer.playSound("guichangemode.wav");
            }
            offset += textWidth + spacing;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}
