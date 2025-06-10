// DropDown.java

package eva.ware.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.Evaware;
import eva.ware.modules.api.Category;
import eva.ware.modules.impl.visual.ClickGui;
import eva.ware.manager.Theme;
import eva.ware.ui.clickgui.components.ModuleComponent;
import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.client.IMinecraft;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.math.Vector2i;
import eva.ware.utils.render.Cursors;
import eva.ware.utils.render.other.GLUtility;
import eva.ware.utils.render.other.GifUtility;
import eva.ware.utils.render.other.KawaseBlur;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class ClickGuiScreen extends Screen implements IMinecraft {
    private static final CompactAnimation yGuiAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 650L);
    private static final CompactAnimation xGuiAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 650L);

    @Getter
    private static final Animation globalAnim = new Animation();
    @Getter
    private static final Animation imageAnimation = new Animation();
    @Getter
    private static final Animation gradientAnimation = new Animation();
    private static final CompactAnimation scaleAnimation = new CompactAnimation(Easing.EASE_IN_QUAD, 200);
    private static final CompactAnimation psChanAnimation = new CompactAnimation(Easing.LINEAR, 700);
    private static final CompactAnimation psChanOverlayAnimation = new CompactAnimation(Easing.LINEAR, 1400);
    public static float scale = 1F;

    private final List<Panel> panels = new ArrayList<>();
    @Setter
    @Getter
    private ModuleComponent expandedModule = null;
    private float updownPanel = 40;
    private float movePanel = 0;
    public SearchField searchField;
    private boolean exit = false, open = false;

    private final TimerUtility psChatYAnimTimer = new TimerUtility();
    private final TimerUtility psChatOverlayAnimTimer = new TimerUtility();

    public ClickGuiScreen(ITextComponent titleIn) {
        super(titleIn);
        Category[] categories = Category.values();
        for (Category category : categories) {
            panels.add(new Panel(category));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        globalAnim.animate(1, 2f, Easings.EXPO_OUT);
        gradientAnimation.animate(1, 0.7f, Easings.EXPO_OUT);
        imageAnimation.animate(1, 0.5, Easings.BACK_OUT);

        exit = false;
        open = true;

        searchField = new SearchField(3, ClientUtility.calc(mc.getMainWindow().getScaledHeight()) - 19, 70, 16, "Поиск");
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        SoundPlayer.playSound("guiyes.wav", .02);

        super.init();
    }

    @Override
    public void closeScreen() {
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        super.closeScreen();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        SoundPlayer.playSound("guiscroll.wav");

        if (ClientUtility.ctrlIsDown()) {
            movePanel += (float) (delta * 5);
        } else {
            updownPanel -= (float) (delta * 20);
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Stream.of(globalAnim, imageAnimation, gradientAnimation).forEach(Animation::update);

        scaleAnimation.run(exit ? 1.5 : 1);
        scaleAnimation.setDuration(exit ? 500 : 150);
        scaleAnimation.setEasing(exit ? Easing.EASE_IN_QUAD : Easing.EASE_OUT_QUAD);

        boolean allow = !(globalAnim.getValue() > 0.4);

        if (Stream.of(globalAnim, imageAnimation, gradientAnimation).allMatch(anim -> anim.getValue() <= 0.1 && anim.isDone())) {
            closeScreen();
        }

        float off = 10.0F;
        float width = (float) panels.size() * 115;
        updateScaleBasedOnScreenWidth();
        int windowWidth = ClientUtility.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtility.calc(mc.getMainWindow().getScaledHeight());
        Vector2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);
        Vector2i fix = ClientUtility.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        ClickGui clickGui = Evaware.getInst().getModuleManager().getClickGui();

        mc.gameRenderer.setupOverlayRendering(2);

        if (ClickGui.background.getValue()) {
            RenderUtility.drawContrast(1 - (float) (gradientAnimation.getValue() / 3f) * 0.7f);
            RenderUtility.drawWhite((float) gradientAnimation.getValue() * 0.7f);
        }

        if (ClickGui.blur.getValue()) {
            KawaseBlur.blur.updateBlur((ClickGui.blurPower.getValue() - 1), (ClickGui.blurPower.getValue().intValue()));
            KawaseBlur.blur.BLURRED.draw();
        }

        if (ClickGui.gradient.getValue()) {
            RenderUtility.drawRectHorizontalW(0, 0 - scaled().y / 4, Minecraft.getInstance().getMainWindow().getScaledWidth(), Minecraft.getInstance().getMainWindow().getScaledHeight() + scaled().y / 3, ColorUtility.setAlpha(Theme.mainRectColor, (int) ((255 * gradientAnimation.getValue()) * getGlobalAnim().getValue())), ColorUtility.rgba(0,0,0,0));
        }

        if (ClickGui.images.getValue()) {
            GifUtility gifUtility = new GifUtility();
            String image = clickGui.imageType.getValue().toLowerCase();
            String path = "eva/images/gui/";
            String psChanOverlay = "eva/images/gui/pschan/ps_overlay.png";
            int totalFrames = 0;
            int frameDelay = 0;
            boolean fromZero = false;

            long durY = 700;
            long durOverlayAlpha = 1400;//go

            if (ClickGui.imageType.is("Miku")) {
                totalFrames = 9;
                frameDelay = 40;
            } else if (ClickGui.imageType.is("Novoura")) {
                totalFrames = 4;
                frameDelay = 80;
            }

            if (ClickGui.imageType.is("PSChan")) {
                path = "eva/images/gui/pschan/";
                image = "ps_base";
            }

            if (Arrays.asList("Miku", "Novoura").contains(ClickGui.imageType.getValue())) {
                int i = gifUtility.getFrame(totalFrames, frameDelay, fromZero);
                path = "eva/images/gif/" + ClickGui.imageType.getValue().toLowerCase() + "/frame_" + i;
                image = "";
            }

            if (psChanAnimation.getValue() != 10 && !psChatYAnimTimer.isReached(durY)) {
                psChanAnimation.run(10);
            } if (psChanAnimation.getValue() != 0 && psChatYAnimTimer.isReached(durY)) {
                psChanAnimation.run(0);
            } if (psChatYAnimTimer.isReached(durY * 2)) {
                psChatYAnimTimer.reset();
            }

            if (psChanOverlayAnimation.getValue() != 255 && !psChatOverlayAnimTimer.isReached(durOverlayAlpha)) {
                psChanOverlayAnimation.run(255);
            } if (psChanOverlayAnimation.getValue() != 0 && psChatOverlayAnimTimer.isReached(durOverlayAlpha * 2)) {
                psChanOverlayAnimation.run(0);
            } if (psChatOverlayAnimTimer.isReached(durOverlayAlpha * 3)) {
                psChatOverlayAnimTimer.reset();
            }

            float offset = (float) (ClickGui.imageType.is("PSChan") ? psChanAnimation.getValue() : 0);
            float size = (float) ((512f / 2f) - 100 + 100 * imageAnimation.getValue());
            float x1 = (windowWidth - size);
            float x2 = (windowWidth);
            float y1 = (windowHeight - size);
            float y2 = (windowHeight);

            RenderUtility.drawImage(new ResourceLocation(path + image + ".png"), x1, y1 + offset, x2 - x1, y2 - y1, ColorUtility.reAlphaInt(-1, (int) ((255 * (imageAnimation.getValue())) * getGlobalAnim().getValue())));

            if (ClickGui.imageType.is("PSChan")) {
                RenderUtility.drawImage(new ResourceLocation(psChanOverlay), x1, y1 + offset, x2 - x1, y2 - y1, ColorUtility.reAlphaInt(-1, (int) ((psChanOverlayAnimation.getValue() * (imageAnimation.getValue())) * getGlobalAnim().getValue())));
            }
        }

        GLUtility.scaleStart(mc.getMainWindow().getScaledWidth() / 2F, mc.getMainWindow().getScaledHeight() / 2f, (float) scaleAnimation.getValue());

        for (Panel panel : panels) {
            xGuiAnimation.run(movePanel);
            yGuiAnimation.run(!allow ? (windowHeight / 2.0F - 110.0F - updownPanel) : (panel.getY() - (exit ? 0 : 10)));
            panel.setY((float) yGuiAnimation.getValue());
            panel.setX((float) (((windowWidth / 2f) - (width / 2f) + panel.getCategory().ordinal() * (115 + off / 2) - off / 1.5) - xGuiAnimation.getValue()));

            panel.render(matrixStack, (float) mouseX, (float) mouseY);
        }

        GLUtility.scaleEnd();

        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        mc.gameRenderer.setupOverlayRendering();
    }

    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public boolean searchCheck(String text) {
        return isSearching() && !text.replaceAll(" ", "").toLowerCase().contains(getSearchText().replaceAll(" ", "").toLowerCase());
    }

    private void updateScaleBasedOnScreenWidth() {
        float totalPanelWidth = (float) panels.size() * 115;
        float screenWidth = (float) mc.getMainWindow().getScaledWidth();
        if (totalPanelWidth >= screenWidth) {
            scale = screenWidth / totalPanelWidth;
            scale = MathHelper.clamp(scale, 0.5F, 1.0F);
        } else {
            scale = 1.0F;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ruleBind = false;

        for (Panel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }

        if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        for (Panel panel : panels) {
            if (panel.binding) {
                ruleBind = true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !exit && !searchField.isTyping() && !ruleBind) {
            globalAnim.animate(0.0, 0.4, Easings.EXPO_OUT);
            gradientAnimation.animate(0.0, 0.35, Easings.EXPO_OUT);
            imageAnimation.animate(0.0, 0.3, Easings.BACK_OUT);

            SoundPlayer.playSound("guino.wav", .03);
            exit = true;
            open = false;
            return false;
        } else {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) return false;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private Vector2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();
        float adjustedMouseX = ((float) mouseX - (float) windowWidth / 2.0F) / scale + (float) windowWidth / 2.0F;
        float adjustedMouseY = ((float) mouseY - (float) windowHeight / 2.0F) / scale + (float) windowHeight / 2.0F;
        return new Vector2i((int) adjustedMouseX, (int) adjustedMouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientUtility.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();
        if (searchField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (Panel panel : panels) {
            panel.mouseClick((float) mouseX, (float) mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientUtility.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = (double) fix.getX();
        mouseY = (double) fix.getY();

        for (Panel panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

}
