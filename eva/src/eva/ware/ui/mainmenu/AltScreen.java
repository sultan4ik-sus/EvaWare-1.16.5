package eva.ware.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.Evaware;
import eva.ware.manager.config.AltConfig;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.client.IMinecraft;
import eva.ware.utils.math.Vector2i;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.TimerUtility;

import eva.ware.utils.player.MouseUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RectUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.other.Scissor;
import eva.ware.utils.text.font.ClientFonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AltScreen extends Screen implements IMinecraft {

    public AltScreen() {
        super(new StringTextComponent(""));
    }

    public final TimerUtility timer = new TimerUtility();

    public final List<Alt> alts = new ArrayList<>();

    public float scroll;
    public float scrollAn;

    private String altName = "";
    private boolean typing;
    float minus = 14;
    float offset = 6f;
    float width = 250, height = 240;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        scrollAn = MathUtility.lerp(scrollAn, scroll, 5);

        RenderUtility.drawShader(timer);

        mc.gameRenderer.setupOverlayRendering(2);

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;


        // Квадрат фона
        float bgX = x - offset, bgY = y - offset, bgWidth = width + offset * 2, bgHeight = height + offset * 2;
        int bgRectColor = ColorUtility.rgba(40, 40, 40, 160);
        RectUtility.getInstance().drawRoundedRectShadowed(matrixStack, bgX, bgY, bgX + bgWidth, bgY + bgHeight, 8, 5, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);

        
//        RenderUtility.drawShadow(x - offset, y - offset, width + offset * 2f, height + offset * 2f, 8, ColorUtility.rgba(40, 40, 40, 160));
//        RenderUtility.drawRoundedRect(x - offset, y - offset, width + offset * 2f, height + offset * 2f, 4, ColorUtility.rgba(40, 40, 40, 160));

        // alt screen name
        RenderUtility.drawShadow(x - 2 + width / 2 - ClientFonts.msSemiBold[22].getWidth("Alt Manager") / 2, y + offset * 2 - 2, ClientFonts.msSemiBold[22].getWidth("Alt Manager") + 2, ClientFonts.msSemiBold[22].getFontHeight(), 10, ColorUtility.rgba(255, 255, 255, 40));
        ClientFonts.msSemiBold[22].drawCenteredString(matrixStack, "Alt Manager", x + width / 2, y + offset * 2, -1);
        ClientFonts.tenacity[16].drawCenteredString(matrixStack, "Текущий ник: " + mc.session.getUsername(), x + width / 2, y + height - offset * 2 + 2, ColorUtility.rgb(200, 200, 200));

        // Квадратик для ввода ника
        RenderUtility.drawShadow(x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f, 8, ColorUtility.rgba(35, 35, 35, 100));
        RenderUtility.drawRoundedRect(x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f, 2f, ColorUtility.rgba(35, 35, 35, 100));

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width - offset * 2f, 20f);
        ClientFonts.tenacity[15].drawString(matrixStack, typing ? (altName + (typing ? "_" : "")) : "Укажите свой ник!", x + offset + 5f,
                y + offset + 72 - minus * 2.5f + 177 - offset * 2, ColorUtility.rgb(152, 152, 152));
        Scissor.unset();
        Scissor.pop();

        // Знак для ввода рандомного ника
        int col = ColorUtility.rgb(38, 33, 54);
        RenderUtility.drawRoundedRect(x + width - offset - ClientFonts.tenacity[22].getWidth("Random") - offset * 2, y + offset + 64 - minus * 2.5f + 177 - offset * 2, ClientFonts.tenacity[22].getWidth("Random") + 13f, 20, new Vector4f(0, 0, 3, 3), ColorUtility.rgba(60, 60, 60, 70));
        ClientFonts.tenacity[22].drawCenteredString(matrixStack, "Random", x + width - offset * 2 - 20, y + offset + 64 - minus * 2.5f + 175 - offset * 2 + ClientFonts.tenacity[22].getFontHeight() / 2, -1);

        // Вывод никнеймов
        float dick = 1;
        RenderUtility.drawShadow(x + offset - dick, y + offset + 60f - minus * 2, width - offset * 2f + dick * 2, 177.5f - minus * 2, 8, ColorUtility.rgba(35, 30, 30, 90));
        RenderUtility.drawRoundedRect(x + offset - dick, y + offset + 60f - minus * 2, width - offset * 2f + dick * 2, 177.5f - minus * 2, 2f, ColorUtility.rgba(30, 30, 30, 90));

        // Надпись при пустом листе аккаунтов
        if (alts.isEmpty()) ClientFonts.msSemiBold[22].drawCenteredString(matrixStack, "Чота пустенько >_<", x + width / 2f, y + offset + 60f - minus * 2.5 + (177.5f - minus) / 2, -1);
        float size = 0f, iter = scrollAn, offsetAccounts = 0f;

        boolean hovered = false;

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2);
        for (Alt alt : alts) {
            float scrollY = y + iter * 22f;
            int color = (mc.session.getUsername().equals(alt.name)) ? ColorUtility.rgba(80, 80, 80, 80) : ColorUtility.rgba(50, 50, 50, 80);

            RenderUtility.drawShadow(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, 6, color);
            RenderUtility.drawRoundedRect(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, 2f, color);

            ClientFonts.msSemiBold[15].drawString(matrixStack, alt.name, x + offset + 24f, scrollY + offset + 70 + offsetAccounts - minus * 2, -1);

            mc.getTextureManager().bindTexture(alt.skin);
            AbstractGui.drawScaledCustomSizeModalRect(x + offset + 4f + 0.5f, scrollY + offset + 63.5f + offsetAccounts - minus * 2, 8F, 8F, 8F, 8F, 16, 16, 64, 64);

            iter++;
            size++;
        }
        scroll = MathHelper.clamp(scroll, size > 8 ? -size + 4 : 0, 0);
        Scissor.unset();
        Scissor.pop();

        mc.gameRenderer.setupOverlayRendering();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!altName.isEmpty() && typing)
                altName = altName.substring(0, altName.length() - 1);
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!altName.isEmpty() && altName.length() >= 3) {
                alts.add(new Alt(altName));
                AltConfig.updateFile();
                SoundPlayer.playSound("success.wav");
            }
            typing = false;
            altName = "";
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (typing) {
                typing = false;
                altName = "";
            }
        }

        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (typing) {
            if (ClientUtility.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
                try {
                    altName += ClientUtility.pasteString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (ClientUtility.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                try {
                    altName = "";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (altName.length() <= 20) altName += Character.toString(codePoint);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixed = MathUtility.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (button == 0 && RenderUtility.isInRegion(mouseX, mouseY, x + width - offset - ClientFonts.tenacity[22].getWidth("Random") - offset * 2, y + offset + 64 - minus * 2.5f + 177 - offset * 2, ClientFonts.tenacity[22].getWidth("Random") + 13f, 20)) {
            alts.add(new Alt(Evaware.getInst().randomNickname()));
            AltConfig.updateFile();
            SoundPlayer.playSound("success.wav", 0.1f);
        }
        if (button == 0 && RenderUtility.isInRegion(mouseX, mouseY, x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f)
                && !RenderUtility.isInRegion(mouseX, mouseY, x + width - offset - ClientFonts.tenacity[22].getWidth("Random") - offset * 2, y + offset + 64 - minus * 2.5f + 177 - offset * 2, ClientFonts.tenacity[22].getWidth("Random") + 12f, 20)) {
            typing = !typing;
        }

        // Основной функционал позволяющий позволяющий брать/удалять ник
        float iter = scrollAn, offsetAccounts = 0f;
        Iterator<Alt> iterator = alts.iterator();
        while (iterator.hasNext()) {
            Alt account = iterator.next();

            float scrollY = y + iter * 22f;

            if (RenderUtility.isInRegion(mouseX, mouseY, x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f)) {
                if (button == 0) {
                    SoundPlayer.playSound("altselect.wav", 0.05f);
                    mc.session = new Session(account.name, "", "", "mojang");
                } else if (button == 1) {
                    iterator.remove();
                    AltConfig.updateFile();
                    SoundPlayer.playSound("friendremove.wav");
                }
            }

            iter++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vector2i fixed = MathUtility.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (MouseUtility.isHovered(mouseX, mouseY, x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2)) scroll += delta * 1;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    public void tick() {
        super.tick();
    }
}
