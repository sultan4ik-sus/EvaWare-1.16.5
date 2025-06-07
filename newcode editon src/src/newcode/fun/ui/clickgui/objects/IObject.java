package newcode.fun.ui.clickgui.objects;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IObject {

    void draw(MatrixStack stack, int mouseX, int mouseY);
    void exit();
    void mouseClicked(int mouseX, int mouseY, int mouseButton);
    void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY);

    void mouseReleased(int mouseX, int mouseY, int mouseButton);

    void keyTyped(int keyCode, int scanCode, int modifiers);
    default void render(MatrixStack stack, float mouseX, float mouseY) {
    }

    default void mouseClick(float mouseX, float mouseY, int mouse) {
    }

    default void mouseRelease(float mouseX, float mouseY, int mouse) {
    }
    default void keyPressed(int key, int scanCode, int modifiers) {
    }
    void charTyped(char codePoint, int modifiers);
}
