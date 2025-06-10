package eva.ware.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.manager.Theme;
import eva.ware.ui.clickgui.ClickGuiScreen;
import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.render.Cursors;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.render.engine2d.RenderUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import eva.ware.ui.clickgui.components.builder.Component;

/**
 * SliderComponent
 */
public class SliderComponent extends Component {

    private final SliderSetting setting;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        this.setHeight(18);
    }
    private float newValue, lastValue;
    private float anim;
    private boolean drag;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 4.5f / 2f + 1, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), 5.5f, 0.05f);
        Fonts.montserrat.drawText(stack, String.valueOf(setting.getValue()), getX() + getWidth() - 5 - Fonts.montserrat.getWidth( String.valueOf(setting.getValue()), 5.5f), getY() + 4.5f / 2f + 1, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), 5.5f, 0.05f);

        RenderUtility.drawRoundedRect(getX() + 5, getY() + 11, getWidth() - 10, 2, 0.6f, ColorUtility.setAlpha(Theme.darkMainRectColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        anim = MathUtility.fast(anim, (getWidth() - 10) * (setting.getValue() - setting.min) / (setting.max - setting.min), 20);
        float sliderWidth = anim;
        RenderUtility.drawRoundedRect(getX() + 5, getY() + 11, sliderWidth, 2, 0.6f, ColorUtility.setAlpha(Theme.rectColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        RenderUtility.drawCircle(getX() + 5 + sliderWidth, getY() + 12, 5, ColorUtility.setAlpha(Theme.rectColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        RenderUtility.drawShadowCircle(getX() + 5 + sliderWidth, getY() + 12, 6, ColorUtility.setAlpha(Theme.rectColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        if (drag) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(),
                    GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
            float newValue = (float) MathHelper.clamp(
                    MathUtility.round((mouseX - getX() - 5) / (getWidth() - 10) * (setting.max - setting.min) + setting.min,
                            setting.increment), setting.min, setting.max);
            if (newValue != lastValue) {
                setting.setValue(newValue);
                lastValue = newValue;
                SoundPlayer.playSound("guislidermove.wav");
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (MathUtility.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 3)) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.RESIZEH);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        if (MathUtility.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 3)) {
            drag = true;
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}