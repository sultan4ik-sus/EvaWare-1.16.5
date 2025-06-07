package newcode.fun.ui.alt;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import newcode.fun.control.Manager;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.render.*;
import newcode.fun.utils.render.animation.AnimationMath;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Vector4i;
import org.lwjgl.glfw.GLFW;
import newcode.fun.utils.font.Fonts;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static newcode.fun.utils.IMinecraft.mc;

public class AltManager extends Screen {

    public AltManager() {
        super(new StringTextComponent(""));

    }

    public ArrayList<Account> accounts = new ArrayList<>();


    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            altName += Minecraft.getInstance().keyboardListener.getClipboardString();
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!altName.isEmpty())
                altName = altName.substring(0, altName.length() - 1);
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!altName.isEmpty())
                accounts.add(new Account(altName));
            typing = false;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            altName = "";
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        altName += !typing ? "" : Character.toString(codePoint);
        return super.charTyped(codePoint, modifiers);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        if (RenderUtils.isInRegion(mouseX, mouseY, (int) ((mc.getMainWindow().scaledWidth() / 2f) - 93f - 20), (int) (mc.getMainWindow().scaledHeight() / 2 + 24f), 16, 16)) {
            AltConfig.updateFile();
            accounts.add(new Account(RandomStringUtils.randomAlphabetic(9)));
        }
        if (RenderUtils.isInRegion(mouseX, mouseY, (int) ((mc.getMainWindow().scaledWidth() / 2f) - 141f), (int) (mc.getMainWindow().scaledHeight() / 2 + 50.5f), 20, 20)) {
            IMinecraft.mc.displayGuiScreen(new MainMenuScreen());
        }
        if (RenderUtils.isInRegion(mouseX, mouseY, (mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 132.5f - 20, 14f)) {
            typing = !typing;
        }
        float altX = (IMinecraft.mc.getMainWindow().scaledWidth() / 2f) - 79.5f - 85 + 3;
        float iter = scrollAn;

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);

            float panWidth = -6.5f;
            int accountIndex = i % 3;

            float xOffset = 0, yOffset = 0;

            if (accountIndex == 0) {
                xOffset = 0;
                yOffset = 0;
            } else if (accountIndex == 1) {
                xOffset = 95.5f;
                yOffset = -11;
            } else if (accountIndex == 2) {
                xOffset = 191.0f;
                yOffset = -22;
            }

            float acX = altX + 120 - 1 + xOffset;
            float acY = IMinecraft.mc.getMainWindow().scaledHeight() / 2 + yOffset - 92.5f + 26 + (iter * (panWidth + 17f)) + 23.5f - 8;

            if (RenderUtils.isInRegion(mouseX, mouseY, acX, acY + 1.5f, 93, 28.5f)) {
                if (button == 0) {
                    IMinecraft.mc.session = new Session(account.accountName, "", "", "mojang");
                } else if (button == 1) {
                    accounts.remove(i);
                    AltConfig.updateFile();
                }
                return true;
            }

            iter++;
        }

        if (button == 0) {
            if (isMouseOverButton((int) mouseX, (int) mouseY, (int) (int) 5, 5, 47, 47)) {
                Runtime rt = Runtime.getRuntime();
                try {
                    rt.exec("rundll32 url.dll,FileProtocolHandler https://login.live.com/login.srf?wa=wsignin1.0&rpsnv=152&ct=1716147678&rver=7.3.6960.0&wp=MBI_SSL&wreply=https%3a%2f%2fwww.microsoft.com%2frpsauth%2fv1%2faccount%2fSignInCallback%3fstate%3deyJSdSI6Imh0dHBzOi8vd3d3Lm1pY3Jvc29mdC5jb20vcnUtcnUiLCJMYyI6IjEwNDkiLCJIb3N0Ijoid3d3Lm1pY3Jvc29mdC5jb20ifQ&lc=1049&id=74335&aadredir=0");
                } catch (IOException var2) {
                    var2.printStackTrace();
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public float scroll;
    public float scrollAn;

    public boolean hoveredFirst;
    public boolean hoveredSecond;

    public float hoveredFirstAn;
    public float hoveredSecondAn;

    private String altName = "";
    private boolean typing;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll += delta * 1;
        return super.mouseScrolled(mouseX, mouseY, delta);

    }

    private boolean isMouseOverButton(int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        return mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        scrollAn = AnimationMath.lerp(scrollAn, scroll, 5);
        hoveredFirst = RenderUtils.isInRegion(mouseX, mouseY, (int) ((int) (IMinecraft.mc.getMainWindow().scaledWidth() / 2f) - 79.5f + 21), IMinecraft.mc.getMainWindow().scaledHeight() / 2 - 80 + 17 + 90, (int) 58.5f, 19);
        hoveredSecond = RenderUtils.isInRegion(mouseX, mouseY, (int) ((int) (IMinecraft.mc.getMainWindow().scaledWidth() / 2f) - 79.5f + 21 + 60), IMinecraft.mc.getMainWindow().scaledHeight() / 2 - 80 + 17 + 90, (int) 58.5f, 19);
        hoveredFirstAn = AnimationMath.lerp(hoveredFirstAn, hoveredFirst ? 1 : 0, 10);
        hoveredSecondAn = AnimationMath.lerp(hoveredSecondAn, hoveredSecond ? 1 : 0, 10);

        IMinecraft.mc.gameRenderer.setupOverlayRendering(2);
        int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());

        RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/mainscreen/background.png"), 0, 0, mc.getMainWindow().scaledWidth(), mc.getMainWindow().scaledHeight(), -1);
        RenderUtils.Render2D.drawImageAlpha(new ResourceLocation("newcode/images/all/mainscreen/background2.png"), 0, 0, mc.getMainWindow().scaledWidth(), mc.getMainWindow().scaledHeight(), new Vector4i(ColorUtils.setAlpha(firstColor2, 155), ColorUtils.setAlpha(firstColor2, 155), ColorUtils.setAlpha(firstColor2, 155), ColorUtils.setAlpha(firstColor2, 155)));
        GaussianBlur.startBlur();
        RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 200f,mc.getMainWindow().scaledHeight() / 2 - 48.5f, 132.5f, 97, 5f, new Color(30, 30, 30, 200).getRGB());
        GaussianBlur.endBlur(6, 1);

        RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 200f,mc.getMainWindow().scaledHeight() / 2 - 48.5f, 132.5f, 97, 5f, new Color(16,16,16,25).getRGB());
        RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 200f,mc.getMainWindow().scaledHeight() / 2 - 48.5f, 132.5f, 97, 5f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                new Color(27, 27, 27, 179).getRGB(),
                new Color(27, 27, 27, 179).getRGB(),
                new Color(27, 27, 27, 179).getRGB(),
                new Color(27, 27, 27, 179).getRGB()
        ));
        Fonts.newcode[14].drawString(matrixStack, "ENTER YOU DATA", (mc.getMainWindow().scaledWidth() / 2f) - 162.5f,mc.getMainWindow().scaledHeight() / 2 - 37, new Color(64, 64, 64, 255).getRGB());

        GaussianBlur.startBlur();
        RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 50f,mc.getMainWindow().scaledHeight() / 2 - 58.5f, 220f, 97 + 20, 5f, new Color(30, 30, 30, 200).getRGB());
        GaussianBlur.endBlur(6, 1);

        RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 50f,mc.getMainWindow().scaledHeight() / 2 - 58.5f, 245f + 54, 97 + 40, 5f, new Color(16, 16, 16,25).getRGB());
        RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 50f,mc.getMainWindow().scaledHeight() / 2 - 58.5f, 245f + 54, 97 + 40, 5f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                new Color(27, 27, 27, 179).getRGB(),
                new Color(27, 27, 27, 179).getRGB(),
                new Color(27, 27, 27, 179).getRGB(),
                new Color(27, 27, 27, 179).getRGB()
        ));

        if (accounts.isEmpty()) {
            RenderUtils.Render2D.drawImage(
                    new ResourceLocation("newcode/images/all/other/emptyru.png"),
                    (mc.getMainWindow().scaledWidth() / 2f) + 5f - 5, mc.getMainWindow().scaledHeight() / 2 - 33.5f, 150, 86,
                    new Color(55, 55, 55, 42).getRGB()
            );
        }

        if (isMouseOverButton(mouseX, mouseY, (int) ((int) (mc.getMainWindow().scaledWidth() / 2f) - 191f), (int) (mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4), (int) (132.5f - 20 + 2), (int) 14f)) {
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 132.5f - 20 + 2, 14f, 3f, new Color(21,21,21,255).getRGB());
            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 132.5f - 20 + 2, 14f, 3f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100)
            ));
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"),(mc.getMainWindow().scaledWidth() / 2f) - 191f - 15,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 53 + Fonts.newcode[14].getWidth(altName.isEmpty() && !typing ? "Username" : altName), 14f, ColorUtils.setAlpha(firstColor2, 35));
            Fonts.icon[14].drawString(matrixStack, "a", (mc.getMainWindow().scaledWidth() / 2f) - 188f,mc.getMainWindow().scaledHeight() / 2 - 5.5f - 7, firstColor2);
            SmartScissor.push();
            SmartScissor.setFromComponentCoordinates((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 132.5f - 20, 14f);
            Fonts.newcode[14].drawString(
                    matrixStack,
                    (altName.isEmpty() && !typing ? "Username" : altName) + (typing ? (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : ""),
                    (mc.getMainWindow().scaledWidth() / 2f) - 180f,mc.getMainWindow().scaledHeight() / 2 - 5.5f - 8,
                    new Color(225, 225, 225, 255).getRGB());
            SmartScissor.unset();
            SmartScissor.pop();
        } else {
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 132.5f - 20 + 2, 14f, 3f, new Color(21,21,21,255).getRGB());
            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 132.5f - 20 + 2, 14f, 3f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    new Color(26,26,26,255).getRGB(),
                    new Color(26,26,26,255).getRGB(),
                    new Color(26,26,26,255).getRGB(),
                    new Color(26,26,26,255).getRGB()
            ));
            Fonts.icon[14].drawString(matrixStack, "a", (mc.getMainWindow().scaledWidth() / 2f) - 188f,mc.getMainWindow().scaledHeight() / 2 - 5.5f - 7, new Color(58,58,58,255).getRGB());
            SmartScissor.push();
            SmartScissor.setFromComponentCoordinates((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 22.5f + 4, 132.5f - 20, 14f);
            Fonts.newcode[14].drawString(
                    matrixStack,
                    (altName.isEmpty() && !typing ? "Username" : altName) + (typing ? (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : ""),
                    (mc.getMainWindow().scaledWidth() / 2f) - 180f,mc.getMainWindow().scaledHeight() / 2 - 5.5f - 8,
                    new Color(58,58,58,255).getRGB());
            SmartScissor.unset();
            SmartScissor.pop();
        }
        if (isMouseOverButton(mouseX, mouseY, (int) ((int) (mc.getMainWindow().scaledWidth() / 2f) - 191f), (int) (mc.getMainWindow().scaledHeight() / 2 - 5.5f + 5), (int) (132.5f - 20 + 2), (int) 14f)) {
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 5, 132.5f - 20 + 2, 14f, 3f, new Color(21,21,21,255).getRGB());
            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 5, 132.5f - 20 + 2, 14f, 3f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100)
            ));
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"),(mc.getMainWindow().scaledWidth() / 2f) - 191f - 15,mc.getMainWindow().scaledHeight() / 2 - 5f + 4, 77, 14f, ColorUtils.setAlpha(firstColor2, 40));
            Fonts.icon[14].drawString(matrixStack, "m", (mc.getMainWindow().scaledWidth() / 2f) - 188f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 11, firstColor2);
            Fonts.newcode[14].drawString(matrixStack, "Password", (mc.getMainWindow().scaledWidth() / 2f) - 180f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 10, new Color(225, 225, 225, 255).getRGB());
        } else {
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 5, 132.5f - 20 + 2, 14f, 3f, new Color(21,21,21,255).getRGB());
            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 191f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 5, 132.5f - 20 + 2, 14f, 3f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    new Color(26,26,26,255).getRGB(),
                    new Color(26,26,26,255).getRGB(),
                    new Color(26,26,26,255).getRGB(),
                    new Color(26,26,26,255).getRGB()
            ));
            Fonts.icon[14].drawString(matrixStack, "m", (mc.getMainWindow().scaledWidth() / 2f) - 188f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 11, new Color(58,58,58,255).getRGB());
            Fonts.newcode[14].drawString(matrixStack, "Password", (mc.getMainWindow().scaledWidth() / 2f) - 180f,mc.getMainWindow().scaledHeight() / 2 - 5.5f + 10, new Color(58,58,58,255).getRGB());
        }

        if (isMouseOverButton(mouseX, mouseY, (int) ((mc.getMainWindow().scaledWidth() / 2f) - 93f), (int) (mc.getMainWindow().scaledHeight() / 2 + 24f), 16, 16)) {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 5 + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 5+ 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 93f,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100)
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, new Color(16,16,16,25).getRGB());
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"), (mc.getMainWindow().scaledWidth() / 2f) - 93f,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, ColorUtils.setAlpha(firstColor2, 40));
            Fonts.icon[18].drawCenteredString(matrixStack, "v", (mc.getMainWindow().scaledWidth() / 2f) - 84.5f,mc.getMainWindow().scaledHeight() / 2 + 30f, firstColor2);
        } else {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 5 + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 5+ 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 93f,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB()
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, new Color(16,16,16,25).getRGB());
            Fonts.icon[18].drawCenteredString(matrixStack, "v", (mc.getMainWindow().scaledWidth() / 2f) - 84.5f,mc.getMainWindow().scaledHeight() / 2 + 30f, new Color(120, 120, 120, 255).getRGB());
        }

        if (isMouseOverButton(mouseX, mouseY, (int) ((mc.getMainWindow().scaledWidth() / 2f) - 93f - 20), (int) (mc.getMainWindow().scaledHeight() / 2 + 24f), 16, 16)) {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 5 + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1 - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 5+ 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1 - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 93f - 20,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100)
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 20,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, new Color(16,16,16,25).getRGB());
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"), (mc.getMainWindow().scaledWidth() / 2f) - 93f - 20,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, ColorUtils.setAlpha(firstColor2, 40));
            Fonts.icon[18].drawCenteredString(matrixStack, "w", (mc.getMainWindow().scaledWidth() / 2f) - 84.5f - 20,mc.getMainWindow().scaledHeight() / 2 + 30f, firstColor2);
        } else {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 5 + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1 - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 5+ 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f + 0.1f + 16 - 1 - 20, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 93f - 20,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB()
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 20,mc.getMainWindow().scaledHeight() / 2 + 24f, 16, 16, 4f, new Color(16,16,16,25).getRGB());
            Fonts.icon[18].drawCenteredString(matrixStack, "w", (mc.getMainWindow().scaledWidth() / 2f) - 84.5f - 20,mc.getMainWindow().scaledHeight() / 2 + 30f, new Color(120, 120, 120, 255).getRGB());
        }

        if (isMouseOverButton(mouseX, mouseY, (int) ((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98), (int) (mc.getMainWindow().scaledHeight() / 2 + 24f), 51, 16)) {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 5 + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f + 51 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 5+ 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f + 51 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98,mc.getMainWindow().scaledHeight() / 2 + 24f, 51, 16, 4f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100)
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98,mc.getMainWindow().scaledHeight() / 2 + 24f, 51, 16, 4f, new Color(16,16,16,25).getRGB());
            Fonts.icon[18].drawCenteredString(matrixStack, "w", (mc.getMainWindow().scaledWidth() / 2f) - 84.5f - 20,mc.getMainWindow().scaledHeight() / 2 + 30f, new Color(120, 120, 120, 255).getRGB());
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"), (mc.getMainWindow().scaledWidth() / 2f) - 93f - 98,mc.getMainWindow().scaledHeight() / 2 + 24f, 51, 16, ColorUtils.setAlpha(firstColor2, 40));

            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4,mc.getMainWindow().scaledHeight() / 2 + 28f - 0.5f + 0.5f  + 0.5f, 3, 3, 0f, new Color(255, 0, 0, 255).getRGB());
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4,mc.getMainWindow().scaledHeight() / 2 + 28 - 0.5f + 4.5f - 0.5f  + 0.5f, 3, 3, 0f, new Color(3, 178, 218, 255).getRGB());
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4 + 4.5f - 1,mc.getMainWindow().scaledHeight() / 2 + 28f - 0.5f + 0.5f  + 0.5f, 3, 3, 0f, new Color(6, 202, 16, 255).getRGB());
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4 + 4.5f - 1,mc.getMainWindow().scaledHeight() / 2 + 28 - 0.5f + 4.5f - 0.5f  + 0.5f, 3, 3, 0f, new Color(255, 234, 0, 255).getRGB());
            Fonts.newcode[15].drawCenteredString(matrixStack, "Microsoft", (mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4 + 4.5f + 21,mc.getMainWindow().scaledHeight() / 2 + 30f, new Color(255, 255, 255, 255).getRGB());
        } else {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 5 + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f + 51 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 5+ 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 0.1f + 51 - 1, mc.getMainWindow().scaledHeight() / 2 + 24f + 4f - 1.5f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98,mc.getMainWindow().scaledHeight() / 2 + 24f, 51, 16, 4f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB()
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98,mc.getMainWindow().scaledHeight() / 2 + 24f, 51, 16, 4f, new Color(16,16,16,25).getRGB());
            Fonts.icon[18].drawCenteredString(matrixStack, "w", (mc.getMainWindow().scaledWidth() / 2f) - 84.5f - 20,mc.getMainWindow().scaledHeight() / 2 + 30f, new Color(120, 120, 120, 255).getRGB());

            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4,mc.getMainWindow().scaledHeight() / 2 + 28f - 0.5f + 0.5f  + 0.5f, 3, 3, 0f, new Color(255, 0, 0, 255).getRGB());
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4,mc.getMainWindow().scaledHeight() / 2 + 28 - 0.5f + 4.5f - 0.5f  + 0.5f, 3, 3, 0f, new Color(3, 178, 218, 255).getRGB());
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4 + 4.5f - 1,mc.getMainWindow().scaledHeight() / 2 + 28f - 0.5f + 0.5f  + 0.5f, 3, 3, 0f, new Color(6, 202, 16, 255).getRGB());
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4 + 4.5f - 1,mc.getMainWindow().scaledHeight() / 2 + 28 - 0.5f + 4.5f - 0.5f  + 0.5f, 3, 3, 0f, new Color(255, 234, 0, 255).getRGB());
            Fonts.newcode[15].drawCenteredString(matrixStack, "Microsoft", (mc.getMainWindow().scaledWidth() / 2f) - 93f - 98 + 4 + 4.5f + 21,mc.getMainWindow().scaledHeight() / 2 + 30f, new Color(120, 120, 120, 255).getRGB());

        }

        GaussianBlur.startBlur();
        RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 141f,mc.getMainWindow().scaledHeight() / 2 + 50.5f, 20, 20, 5f, new Color(16,16,16,25).getRGB());
        GaussianBlur.endBlur(6, 1);

        if (isMouseOverButton(mouseX, mouseY, (int) ((mc.getMainWindow().scaledWidth() / 2f) - 141f), (int) (mc.getMainWindow().scaledHeight() / 2 + 50.5f), 20, 20)) {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 5 + 4f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 4f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f + 0.1f + 20 - 1, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 5+ 4f, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f+ 0.1f + 20 - 1, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 4f , 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 141f,mc.getMainWindow().scaledHeight() / 2 + 50.5f, 20, 20, 5f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100),
                    ColorUtils.setAlpha(firstColor2, 100)
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 141f,mc.getMainWindow().scaledHeight() / 2 + 50.5f, 20, 20, 5f, new Color(16,16,16,25).getRGB());
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"), (mc.getMainWindow().scaledWidth() / 2f) - 141f,mc.getMainWindow().scaledHeight() / 2 + 50.5f, 20, 20, ColorUtils.setAlpha(firstColor2, 40));
            Fonts.icon[23].drawCenteredString(matrixStack, "o", (mc.getMainWindow().scaledWidth() / 2f) - 131.5f,mc.getMainWindow().scaledHeight() / 2 + 57f, firstColor2);
        } else {
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 5 + 4f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f + 0.1f, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 4f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f + 0.1f + 20 - 1, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 5+ 4f, 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
            RenderUtils.Render2D.drawShadow((mc.getMainWindow().scaledWidth() / 2f) - 141f+ 0.1f + 20 - 1, mc.getMainWindow().scaledHeight() / 2 + 50.5f + 4f , 0.5f, 5, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));

            RenderUtils.Render2D.drawRoundOutline((mc.getMainWindow().scaledWidth() / 2f) - 141f,mc.getMainWindow().scaledHeight() / 2 + 50.5f, 20, 20, 5f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB(),
                    new Color(27, 27, 27, 179).getRGB()
            ));
            RenderUtils.Render2D.drawRoundedRect((mc.getMainWindow().scaledWidth() / 2f) - 141f,mc.getMainWindow().scaledHeight() / 2 + 50.5f, 20, 20, 5f, new Color(16,16,16,25).getRGB());
            Fonts.icon[23].drawCenteredString(matrixStack, "o", (mc.getMainWindow().scaledWidth() / 2f) - 131.5f,mc.getMainWindow().scaledHeight() / 2 + 57f, new Color(120, 120, 120, 255).getRGB());
        }


        float altX = (IMinecraft.mc.getMainWindow().scaledWidth() / 2f) - 79.5f - 85 + 3, altY = 298 / 2f;
        float iter = scrollAn;
        float size = 0;
        SmartScissor.push();
        SmartScissor.setFromComponentCoordinates((mc.getMainWindow().scaledWidth() / 2f) - 50f + 1,mc.getMainWindow().scaledHeight() / 2 - 58.5f + 7, 293, 97 + 42 - 13);

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            float panWidth = -6.5f;
            int accountIndex = i % 3;
            float xOffset = 0;
            float yOffset = 0;


            if (accountIndex == 0) {
                xOffset = 0;
                yOffset = 0;
            } else if (accountIndex == 1) {
                xOffset = 95.5f;
                yOffset = -11;
            } else if (accountIndex == 2) {
                xOffset = 191.0f;
                yOffset = -22;
            }

            float acX = altX + 120 - 1 + xOffset;
            float acY = IMinecraft.mc.getMainWindow().scaledHeight() / 2 + yOffset - 92.5f + 26 + (iter * (panWidth + 17f)) + 23.5f - 8;
            SmartScissor.push();
            SmartScissor.setFromComponentCoordinates(acX,acY + 1.5f, 293, 29f);
            if (!account.accountName.equalsIgnoreCase(IMinecraft.mc.session.getUsername())) {
                RenderUtils.Render2D.drawRoundedRect(acX,acY + 1.5f, 93, 28.5f, 5f, new Color(21,21,21,255).getRGB());
                RenderUtils.Render2D.drawRoundOutline(acX,acY + 1.5f, 93, 28.5f, 5f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                        new Color(26,26,26,255).getRGB(),
                        new Color(26,26,26,255).getRGB(),
                        new Color(26,26,26,255).getRGB(),
                        new Color(26,26,26,255).getRGB()
                ));
                RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"), acX,acY + 16.5f, 93, 30f, new Color(87, 87, 87, 33).getRGB());

                RenderUtils.Render2D.drawShadow(acX  + 0.5f,acY + 5f + 10, 0.5f, 10, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
                RenderUtils.Render2D.drawShadow(acX + 0.5f,acY + 5f, 0.5f, 10, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));
                RenderUtils.Render2D.drawShadow(acX + 93 - 0.5f,acY + 5f + 10, 0.5f, 10, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0));
                RenderUtils.Render2D.drawShadow(acX + 93 - 0.5f,acY + 5f, 0.5f, 10, 0, ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 0), ColorUtils.setAlpha(new Color(87, 87, 87, 153).getRGB(), 255));

                Fonts.newcode[15].drawCenteredString(matrixStack, account.accountName, acX + 26 + (Fonts.newcode[15].getWidth(account.accountName) / 2f), acY + 10f, ColorUtils.rgba(138,136,136,255));
                Fonts.newcode[12].drawCenteredString(matrixStack, "localhost", acX + 26.4f + (Fonts.newcode[12].getWidth("localhost") / 2f), acY + 19f, ColorUtils.rgba(59,57,57,255));

            }
            if (account.accountName.equalsIgnoreCase(IMinecraft.mc.session.getUsername())) {
                RenderUtils.Render2D.drawRoundedRect(acX,acY + 1.5f, 93, 28.5f, 5f, new Color(21,21,21,255).getRGB());
                RenderUtils.Render2D.drawRoundOutline(acX,acY + 1.5f, 93, 28.5f, 5f, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(
                        ColorUtils.setAlpha(firstColor2, 100),
                        ColorUtils.setAlpha(firstColor2, 100),
                        ColorUtils.setAlpha(firstColor2, 100),
                        ColorUtils.setAlpha(firstColor2, 100)
                ));
                RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/bloom.png"), acX,acY + 16.5f, 93, 30f, ColorUtils.setAlpha(firstColor2, 25));

                RenderUtils.Render2D.drawShadow(acX  + 0.5f,acY + 5f + 10, 0.5f, 10, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
                RenderUtils.Render2D.drawShadow(acX + 0.5f,acY + 5f, 0.5f, 10, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));
                RenderUtils.Render2D.drawShadow(acX + 93 - 0.5f,acY + 5f + 10, 0.5f, 10, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
                RenderUtils.Render2D.drawShadow(acX + 93 - 0.5f,acY + 5f, 0.5f, 10, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));

                Fonts.newcode[15].drawCenteredString(matrixStack, account.accountName, acX + 26 + (Fonts.newcode[15].getWidth(account.accountName) / 2f), acY + 10f, ColorUtils.rgba(248,241,241,255));
                Fonts.newcode[12].drawCenteredString(matrixStack, "localhost", acX + 26.4f + (Fonts.newcode[12].getWidth("localhost") / 2f), acY + 19f, ColorUtils.rgba(133,123,123,255));

            }


            StencilUtils.initStencilToWrite();
            RenderUtils.Render2D.drawRoundedRect(acX + 4.5F, acY + 6f, 19, 19, 4, Color.BLACK.getRGB());
            StencilUtils.readStencilBuffer(1);
            mc.getTextureManager().bindTexture(account.skin);
            AbstractGui.drawScaledCustomSizeModalRect(acX + 4.5F, acY + 6f, 8F, 8F, 8F, 8F, 19, 19, 64, 64);
            StencilUtils.uninitStencilBuffer();
            SmartScissor.unset();
            SmartScissor.pop();
            iter++;
            size++;
        }

        scroll = MathHelper.clamp(scroll, size > 12 ? -size + 4 : 0, 0);

        SmartScissor.unset();
        SmartScissor.pop();

        IMinecraft.mc.gameRenderer.setupOverlayRendering();
    }


}