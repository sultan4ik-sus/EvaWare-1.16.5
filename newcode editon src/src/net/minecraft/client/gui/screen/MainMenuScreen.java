package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import newcode.fun.module.TypeList;
import newcode.fun.ui.clickgui.Panel;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.language.Translated;
import newcode.fun.utils.render.*;
import newcode.fun.control.Manager;
import newcode.fun.utils.font.Fonts;
import org.joml.Vector4i;

import static newcode.fun.utils.IMinecraft.mc;
import static newcode.fun.utils.IMinecraft.sr;

public class MainMenuScreen extends Screen {
    public static final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY_TEXTURES = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private final boolean showTitleWronglySpelled;
    private static final ResourceLocation MINECRAFT_TITLE_TEXTURES = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_TITLE_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private Screen realmsNotification;
    private final boolean showFadeInAnimation;

    public MainMenuScreen() {
        this(false);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        int buttonWidth = (int) ((int) (353 / 2f));
        int buttonHeight = (int) ((int) (68 / 2f));
        int off = (int) (buttonHeight + 5);

        this.addButton(new Button(
                (int) ((int) (mc.getMainWindow().scaledWidth() / 2 - buttonWidth / 2f) + 25 + 7f + 12),
                mc.getMainWindow().scaledHeight() / 2 - 57 + 10 + 3 + 1 + 6,
                buttonWidth - 50 - 14 - 24,
                buttonHeight - 4 - 10 - 2,
                new StringTextComponent("Singleplayer"),
                p_onPress_1_ -> { mc.displayGuiScreen(new WorldSelectionScreen(this)); },
                'q'
        ));

        this.addButton(new Button(
                (int) (mc.getMainWindow().scaledWidth() / 2 - buttonWidth / 2f) + 25 + 7 + 12,
                mc.getMainWindow().scaledHeight() / 2 - 66 + off + 3 + 1 + 6,
                buttonWidth - 50 - 14 - 24,
                buttonHeight - 4 - 10 - 2,
                new StringTextComponent("Multiplayer"),
                p_onPress_1_ -> { mc.displayGuiScreen(new MultiplayerScreen(this)); },
                'r'
        ));

        this.addButton(new Button(
                (int) (mc.getMainWindow().scaledWidth() / 2 - buttonWidth / 2f) + 25 + 7 + 12,
                mc.getMainWindow().scaledHeight() / 2 - 75 + off * 2 - 4 + 3 + 1,
                buttonWidth - 50 - 14 - 24,
                buttonHeight - 4 - 10 - 2,
                new StringTextComponent("Account Manager"),
                p_onPress_1_ -> { mc.displayGuiScreen(Manager.ALT); },
                's'
        ));

        this.addButton(new Button(
                (int) (mc.getMainWindow().scaledWidth() / 2 - buttonWidth / 2f) + 25 + 7 + 12,
                mc.getMainWindow().scaledHeight() / 2 - 75 + off * 2 - 4 + 3 + 1 + 20,
                buttonHeight - 4 - 10 - 2 + 1,
                buttonHeight - 4 - 10 - 2 + 1,
                new StringTextComponent(""),
                p_onPress_1_ -> { mc.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings)); },
                't'
        ));

        this.addButton(new Button(
                (int) (mc.getMainWindow().scaledWidth() / 2 - buttonWidth / 2f) + 25 + 7 + 11 + 70,
                mc.getMainWindow().scaledHeight() / 2 - 75 + off * 2 - 4 + 3 + 1 + 20,
                buttonHeight - 4 - 10 - 2 + 1,
                buttonHeight - 4 - 10 - 2 + 1,
                new StringTextComponent(""),
                p_onPress_1_ -> { mc.shutdownMinecraftApplet(); },
                'u'
        ));
    }

    private boolean areRealmsNotificationsEnabled() {
        return this.minecraft.gameSettings.realmsNotifications && this.realmsNotification != null;
    }

    public MainMenuScreen(boolean fadeIn) {
        super(new TranslationTextComponent("narrator.screen.title"));
        this.showFadeInAnimation = fadeIn;
        this.showTitleWronglySpelled = (double) (new Random()).nextFloat() < 1.0E-4D;
    }

    public void tick() {
        if (this.areRealmsNotificationsEnabled()) {
            this.realmsNotification.tick();
        }
    }

    public static CompletableFuture<Void> loadAsync(TextureManager texMngr, Executor backgroundExecutor) {
        return CompletableFuture.allOf(texMngr.loadAsync(MINECRAFT_TITLE_TEXTURES, backgroundExecutor), texMngr.loadAsync(MINECRAFT_TITLE_EDITION, backgroundExecutor), texMngr.loadAsync(PANORAMA_OVERLAY_TEXTURES, backgroundExecutor), PANORAMA_RESOURCES.loadAsync(texMngr, backgroundExecutor));
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
    int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        if (RenderUtils.isInRegion(mouseX, mouseY, mc.getMainWindow().scaledWidth() - 25, mc.getMainWindow().scaledHeight() - 25, 20, 20)) {
            Translated.setRussian(!Translated.isRussian());
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        mc.gameRenderer.setupOverlayRendering(2);

        RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/mainscreen/background.png"), 0, 0, mc.getMainWindow().scaledWidth(), mc.getMainWindow().scaledHeight(), -1);

        RenderUtils.Render2D.drawImageAlpha(new ResourceLocation("newcode/images/all/mainscreen/background2.png"), 0, 0, mc.getMainWindow().scaledWidth(), mc.getMainWindow().scaledHeight(), new Vector4i(ColorUtils.setAlpha(firstColor2, 155), ColorUtils.setAlpha(firstColor2, 155), ColorUtils.setAlpha(firstColor2, 155), ColorUtils.setAlpha(firstColor2, 155)));

        RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/logo/logo.png"), mc.getMainWindow().scaledWidth() / 2 - 17f, mc.getMainWindow().scaledHeight() / 2 - 75, 35, 35, ColorUtils.setAlpha(firstColor2, 95));

        Fonts.newcode[14].drawCenteredString(matrixStack, "v 1.04", mc.getMainWindow().scaledWidth() / 2, mc.getMainWindow().scaledHeight() / 2 + 60f, new Color(58, 58, 58, 90).getRGB());

        String languageLogoPath = Translated.isRussian() ? "newcode/images/all/language/en.png" : "newcode/images/all/language/ru.png";

        mc.getTextureManager().bindTexture(new ResourceLocation(languageLogoPath));
        RenderUtils.Render2D.drawTexture(mc.getMainWindow().scaledWidth() - 25, mc.getMainWindow().scaledHeight() - 25, 20, 20, 4, 1);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        mc.gameRenderer.setupOverlayRendering();
    }

    public void onClose() {
        if (this.realmsNotification != null) {
            this.realmsNotification.onClose();
        }
    }

    public static class Button extends AbstractButton {

        public static final net.minecraft.client.gui.widget.button.Button.ITooltip field_238486_s_ = (button, matrixStack, mouseX, mouseY) ->
        {
        };
        protected final net.minecraft.client.gui.widget.button.Button.IPressable onPress;
        protected final net.minecraft.client.gui.widget.button.Button.ITooltip onTooltip;
        private char iconChar;

        public Button(int x, int y, int width, int height, ITextComponent title, net.minecraft.client.gui.widget.button.Button.IPressable pressedAction, char iconChar) {
            this(x, y, width, height, title, pressedAction, field_238486_s_);
            this.iconChar = iconChar;
        }

        public Button(int x, int y, int width, int height, ITextComponent title, net.minecraft.client.gui.widget.button.Button.IPressable pressedAction, net.minecraft.client.gui.widget.button.Button.ITooltip onTooltip) {
            super(x, y, width, height, title);
            this.onPress = pressedAction;
            this.onTooltip = onTooltip;
            this.iconChar = ' ';
        }


        public float animation;
        int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
        float titleWidth = Fonts.newcode[14].getWidth(this.getMessage().getString());
        private float hoverProgress = 0f;
        private final float animationSpeed = 0.05f;

        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            GaussianBlur.startBlur();
            RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 5f, new Color(16, 16, 16, 25).getRGB());
            GaussianBlur.endBlur(6, 1);

            boolean isHovered = isMouseOverButton(mouseX, mouseY, (int) (x), y, width, height);

            if (isHovered) {
                hoverProgress = Math.min(hoverProgress + animationSpeed, 1f);
            } else {
                hoverProgress = Math.max(hoverProgress - animationSpeed, 0f);
            }

            RenderUtils.Render2D.drawRoundOutline(x, y, width, height, 5f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    ColorUtils.interpolateColor(new Color(34, 34, 34, 179).getRGB(), ColorUtils.setAlpha(firstColor2, 135), hoverProgress),
                    ColorUtils.interpolateColor(new Color(34, 34, 34, 179).getRGB(), ColorUtils.setAlpha(firstColor2, 135), hoverProgress),
                    ColorUtils.interpolateColor(new Color(34, 34, 34, 179).getRGB(), ColorUtils.setAlpha(firstColor2, 135), hoverProgress),
                    ColorUtils.interpolateColor(new Color(34, 34, 34, 179).getRGB(), ColorUtils.setAlpha(firstColor2, 135), hoverProgress)
            ));

            RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 5f, ColorUtils.interpolateColor(new Color(16, 16, 16, 25).getRGB(), new Color(30, 30, 30, 77).getRGB(), hoverProgress));

            RenderUtils.Render2D.drawShadow(x + 0.1f, y + 5 + 4f, 0.5f, 5, 0, ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 255), hoverProgress), ColorUtils.interpolateColor(new Color(69, 69, 69, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 160), hoverProgress), ColorUtils.interpolateColor(new Color(69, 69, 69, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress));
            RenderUtils.Render2D.drawShadow(x + 0.1f, y + 4f, 0.5f, 5, 0, ColorUtils.interpolateColor(new Color(78, 78, 78, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 255), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 255), hoverProgress));
            RenderUtils.Render2D.drawShadow(x + 0.1f + width - 1, y + 5 + 4f, 0.5f, 5, 0, ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 255), hoverProgress), ColorUtils.interpolateColor(new Color(69, 69, 69, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 160), hoverProgress), ColorUtils.interpolateColor(new Color(69, 69, 69, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress));
            RenderUtils.Render2D.drawShadow(x + 0.1f + width - 1, y + 4f, 0.5f, 5, 0, ColorUtils.interpolateColor(new Color(78, 78, 78, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 255), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 0), hoverProgress), ColorUtils.interpolateColor(new Color(78, 78, 78, 153).getRGB(), ColorUtils.setAlpha(firstColor2, 255), hoverProgress));

            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"),x, y, width, height, ColorUtils.interpolateColor(new Color(16, 16, 16, 0).getRGB(), ColorUtils.setAlpha(firstColor2, 35), hoverProgress));

            Fonts.newcode[14].drawCenteredString(matrixStack, this.getMessage().getString(), this.x + this.width / 2 + 6, this.y + (this.height - 8) / 2 + 3f,
                    ColorUtils.interpolateColor(new Color(120, 120, 120, 255).getRGB(), new Color(225, 225, 225, 255).getRGB(), hoverProgress)
            );
            Fonts.icon[16].drawCenteredString(matrixStack, String.valueOf(iconChar), this.x + this.width / 2 + 1f - titleWidth / 2, this.y + (this.height - 8) / 2 + 2 + 1.5f,
                    ColorUtils.interpolateColor(new Color(120, 120, 120, 255).getRGB(), firstColor2, hoverProgress)
            );
        }


        private boolean isMouseOverButton(int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
            return mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;
        }

        public void onPress() {
            this.onPress.onPress(this);
        }
    }
}
