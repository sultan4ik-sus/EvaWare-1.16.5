package eva.ware.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.modules.settings.impl.ModeSetting;
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

import org.lwjgl.glfw.GLFW;
import eva.ware.ui.clickgui.components.builder.Component;

import java.util.HashMap;
import java.util.Map;

public class ModeComponent extends Component {

    final ModeSetting setting;

    float width = 0;
    float heightplus = 0;
    float spacing = 5;

    private final Map<String, Animation> animations = new HashMap<>();

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
        setHeight(22);
        for (String option : setting.strings) {
            animations.put(option, new Animation());
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        spacing = 5;
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 2, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), 5.5f, 0.05f);

        float offset = 0;
        float heightoff = 0;
        boolean anyHovered = false;

        for (String text : setting.strings) {
            Animation animation = animations.get(text);
            animation.update();
            animation.animate(text.equals(setting.getValue()) ? 1 : 0, 1, Easings.EXPO_OUT);
            float textWidth = Fonts.montserrat.getWidth(text, 5.5f, 0.05f) + 3;
            float textHeight = Fonts.montserrat.getHeight(5.5f) + 1;

            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing / 2;
            }
            if (MathUtility.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 11.5f + heightoff, textWidth, textHeight)) {
                anyHovered = true;
            }

            int interpolateColor = ColorUtility.interpolateColor(Theme.mainRectColor, ColorUtility.rgba(25, 25, 25, 180), (float) animation.getValue());
            int color = ColorUtility.setAlpha(interpolateColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));

            RenderUtility.drawShadow(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);
            RenderUtility.drawRoundedRect(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);

            Fonts.montserrat.drawText(stack, text, getX() + 8 + offset, getY() + 11.5f + heightoff, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), 5.5f, text.equals(setting.getValue()) ? 0.07f : 0.05f);

            offset += textWidth + spacing / 2;
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
        heightplus = heightoff;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float offset = 0;
        float heightoff = 0;
        for (String text : setting.strings) {
            float textWidth = Fonts.montserrat.getWidth(text, 5.5f, 0.05f) + 3;
            float textHeight = Fonts.montserrat.getHeight(5.5f) + 1;

            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing / 2;
            }
            if (mouse == 0 && !text.equals(setting.getValue()) && MathUtility.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10 + heightoff, Fonts.montserrat.getWidth(text, 5.5f, 0.05f), Fonts.montserrat.getHeight(5.5f) + 1)) {
                setting.setValue(text);
                SoundPlayer.playSound("guichangemode.wav");
            }
            offset += textWidth + spacing / 2;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}