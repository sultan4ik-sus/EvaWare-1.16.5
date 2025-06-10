package eva.ware.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.manager.Theme;
import eva.ware.modules.settings.impl.StringSetting;
import eva.ware.ui.clickgui.ClickGuiScreen;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.render.Cursors;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.render.engine2d.RenderUtility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.glfw.GLFW;
import eva.ware.ui.clickgui.components.builder.Component;

import java.util.ArrayList;
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class StringComponent extends Component {

    final StringSetting setting;
    boolean typing;
    String text = "";

    private static final int X_OFFSET = 5;
    private static final int Y_OFFSET = 10;
    private static final int WIDTH_OFFSET = -9;
    private static final int TEXT_Y_OFFSET = -7;

    public StringComponent(StringSetting setting) {
        this.setting = setting;
        this.setHeight(24);
    }

    boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        text = setting.getValue();
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(text)) {
            text = text.replaceAll("[a-zA-Z]", "");
        }
        float x = calculateX();
        float y = calculateY();
        float width = calculateWidth();
        String settingName = setting.getName();
        String settingDesc = setting.getDescription();
        String textToDraw = setting.getValue();

        if (!typing && setting.getValue().isEmpty()) {
            textToDraw = settingDesc;
        }
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(textToDraw)) {
            textToDraw = textToDraw.replaceAll("[a-zA-Z]", "");
        }

        float height = calculateHeight(textToDraw, width - 1);
        drawSettingName(stack, settingName, x, y);
        drawBackground(x, y, width, height);
        drawTextWithLineBreaks(stack, textToDraw + (typing && text.length() < 59 && System.currentTimeMillis() % 1000 > 500 ? "_" : ""), x + 1, y + Fonts.montserrat.getHeight(6) / 2, width - 1);

        if (isHovered(mouseX, mouseY)) {
            if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.IBEAM);
                    hovered = true;
                }
            } else {
                if (hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    hovered = false;
                }
            }
        }
        setHeight(height + 12);
    }

    private void drawTextWithLineBreaks(MatrixStack stack, String text, float x, float y, float maxWidth) {

        String[] lines = text.split("\n");
        float currentY = y;

        for (String line : lines) {
            List<String> wrappedLines = wrapText(line, 6, maxWidth);
            for (String wrappedLine : wrappedLines) {
                Fonts.montserrat.drawText(stack, wrappedLine, x, currentY, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), 6);
                currentY += Fonts.montserrat.getHeight(6);
            }
        }
    }

    private List<String> wrapText(String text, float size, float maxWidth) {

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (Fonts.montserrat.getWidth(word, size) <= maxWidth) {
                if (Fonts.montserrat.getWidth(currentLine.toString() + word, size) <= maxWidth) {
                    currentLine.append(word).append(" ");
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word).append(" ");
                }
            } else {
                if (!currentLine.toString().isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                currentLine = breakAndAddWord(word, currentLine, size, maxWidth, lines);
            }
        }

        if (!currentLine.toString().isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private StringBuilder breakAndAddWord(String word, StringBuilder currentLine, float size, float maxWidth, List<String> lines) {
        int wordLength = word.length();
        for (int i = 0; i < wordLength; i++) {
            char c = word.charAt(i);
            String nextPart = currentLine.toString() + c;
            if (Fonts.montserrat.getWidth(nextPart, size) <= maxWidth) {
                currentLine.append(c);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(String.valueOf(c));
            }
        }
        return currentLine;
    }


    private float calculateX() {
        return getX() + X_OFFSET;
    }

    private float calculateY() {
        return getY() + Y_OFFSET;
    }

    private float calculateWidth() {
        return getWidth() + WIDTH_OFFSET;
    }

    private float calculateHeight(String text, float maxWidth) {
        List<String> wrappedLines = wrapText(text, 6, maxWidth);
        int numberOfLines = wrappedLines.size();
        float lineHeight = Fonts.montserrat.getHeight(6);
        float spacingBetweenLines = 1.5f;
        float initialHeight = 5;

        return initialHeight + (numberOfLines * lineHeight) + ((numberOfLines - 1));
    }


    private void drawSettingName(MatrixStack stack, String settingName, float x, float y) {
        Fonts.montserrat.drawText(stack, settingName, x, y + TEXT_Y_OFFSET, ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), 6);
    }

    private void drawBackground(float x, float y, float width, float height) {
        RenderUtility.drawShadow(x, y, width, height, 10, ColorUtility.rgba(25, 25, 25, (int) (45 * ClickGuiScreen.getGlobalAnim().getValue())));
        RenderUtility.drawRoundedRect(x, y, width, height, 2, ColorUtility.rgba(25, 25, 25, (int) (45 * ClickGuiScreen.getGlobalAnim().getValue())));
    }


    @Override
    public void charTyped(char codePoint, int modifiers) {
        boolean ctrlDown = GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

        if (setting.isOnlyNumber() && !NumberUtils.isNumber(String.valueOf(codePoint))) {
            return;
        }
        if (typing && text.length() < 60) {
            text += codePoint;
            setting.setValue(text);

        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (typing) {
            if (Screen.isPaste(key)) {
                pasteFromClipboard();
            }

            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                deleteLastCharacter();
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                typing = false;
            }
        }
        super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY)) {
            typing = !typing;
        } else {
            typing = false;
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    private boolean isControlDown() {
        return GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private void pasteFromClipboard() {
        try {
            text += GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
            setting.setValue(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteLastCharacter() {
        if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            setting.setValue(text);
        }
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}
