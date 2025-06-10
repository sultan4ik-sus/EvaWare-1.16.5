package eva.ware.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.manager.Theme;
import eva.ware.ui.clickgui.ClickGuiScreen;
import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.render.Cursors;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.font.ClientFonts;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import eva.ware.ui.clickgui.components.builder.Component;

/**
 * BooleanComponent
 */
public class BooleanComponent extends Component {

    private final CheckBoxSetting setting;

    public BooleanComponent(CheckBoxSetting setting) {
        this.setting = setting;
        setHeight(16);
        animation = animation.animate(setting.getValue() ? 1 : 0, 0.1, Easings.CIRC_OUT);
    }

    private Animation animation = new Animation();
    private float width, height;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        // TODO Auto-generated method stub
        super.render(stack, mouseX, mouseY);
        animation.update();
        ClientFonts.tenacity[16].drawString(stack, setting.getName(), getX() + 20f, getY() + 4, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        width = 12;
        height = 7;
        int color = ColorUtility.interpolate(Theme.rectColor, Theme.darkTextColor, 1 - (float) animation.getValue());
        RenderUtility.drawRoundedRect(getX() + 6, getY() - 1.5f + getHeight() / 2f - height / 2f, width, height, 3f, ColorUtility.setAlpha(Theme.textColor, (int) (40 * ClickGuiScreen.getGlobalAnim().getValue())));
        RenderUtility.drawCircle((float) (getX() + 6 + 4 + (4 * animation.getValue())), getY() - 1.5f + getHeight() / 2f - height / 2f + 3.5f, 5f, ColorUtility.setAlpha(color, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        RenderUtility.drawShadowCircle((float) (getX() + 6 + 4 + (4 * animation.getValue())), getY() - 1.5f + getHeight() / 2f - height / 2f + 3.5f, 7f, ColorUtility.setAlpha(color, (int) ((128 * animation.getValue()) * ClickGuiScreen.getGlobalAnim().getValue())));

        if (isHovered(mouseX, mouseY)) {
            if (MathUtility.isHovered(mouseX, mouseY, getX() + 6, getY() - 1.5f + getHeight() / 2f - height / 2f, width, height)) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (mouse == 0 && MathUtility.isHovered(mouseX, mouseY, getX() + 6, getY() - 1.5f + getHeight() / 2f - height / 2f, width, height)) {
            setting.setValue(!setting.getValue());
            animation = animation.animate(setting.getValue() ? 1 : 0, 0.1, Easings.CIRC_OUT);
            SoundPlayer.playSound(setting.getValue() ? "buttonyes.wav" : "buttonno.wav");
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}