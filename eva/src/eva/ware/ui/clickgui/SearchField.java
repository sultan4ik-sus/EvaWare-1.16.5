package eva.ware.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.other.Scissor;
import eva.ware.utils.render.engine2d.RectUtility;
import eva.ware.utils.text.font.ClientFonts;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

@Setter
@Getter
public class SearchField {
    private int x, y, width, height;
    private String text;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;

    public SearchField(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
        this.text = "";
        this.isFocused = false;
        this.typing = false;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        float animation = (float) (1 * ClickGuiScreen.getGradientAnimation().getValue());
        float posAnimation = 20 - 20 * animation;
        String textToDraw = text.isEmpty() && !typing ? placeholder : text;
        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "";
        int color = ColorUtility.rgba(20, 20, 20, (int) (180 * animation));
        RectUtility.getInstance().drawRoundedRectShadowed(matrixStack, x - posAnimation, y, x + width - posAnimation, y + height, 2, 5, color, color, color, color, true, false, true, true);
        RectUtility.getInstance().drawRoundedRectShadowed(matrixStack, x - posAnimation, y, x + width - posAnimation, y + height, 2, 5, color, color, color, color, true, true, false, true);

        Scissor.push();
        Scissor.setFromComponentCoordinates(x - posAnimation, y, width + posAnimation, height);
        ClientFonts.tenacity[15].drawString(matrixStack, textToDraw + cursor, x + 5 - posAnimation, y + (height - 8f) / 2 + 1.5f, ColorUtility.reAlphaInt(ColorUtility.rgb(200, 200, 200), (int) (255 * animation)));
        Scissor.unset();
        Scissor.pop();
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (typing) {
            text += codePoint;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientUtility.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            text = "";
        }
        if (typing && keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            typing = false;
        }
        if (ClientUtility.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
            text += ClientUtility.pasteString();
        }
        if (ClientUtility.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_F) {
            typing = true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!MathUtility.isHovered((float) mouseX, (float) mouseY, x, y, width, height)){
            isFocused = false;
        }
        isFocused = MathUtility.isHovered((float) mouseX, (float) mouseY, x, y, width, height);
        typing = isFocused;
        return isFocused;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }
    public void setFocused(boolean focused) { isFocused = focused; }
}
