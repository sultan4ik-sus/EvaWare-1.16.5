package eva.ware.ui.mainmenu;

import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.math.animation.Animation;
import eva.ware.utils.math.animation.util.Easings;
import eva.ware.utils.render.engine2d.RectUtility;
import com.mojang.blaze3d.matrix.MatrixStack;

import eva.ware.Evaware;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.client.IMinecraft;
import eva.ware.utils.math.Vector2i;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.BetterText;
import eva.ware.utils.text.font.ClientFonts;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;


public class MainScreen extends Screen implements IMinecraft {
    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));

    }

    public final TimerUtility timer = new TimerUtility();
    public static float o = 0;

    private final List<Button> buttons = new ArrayList<>();


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        float offset = 4;

        float widthButton = 100;
        float heightButton = 20;
        float x = ClientUtility.calc(width) / 2f - widthButton / 2;
        float y = Math.round(5 + ClientUtility.calc(height) / 2f - (heightButton * 6) / 2);
        buttons.clear();
        buttons.add(new Button(x, y, widthButton, heightButton, "single", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "multy", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "altmgr", () -> {
            mc.displayGuiScreen(Evaware.getInst().getAltScreen());
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "settings", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "quit", mc::shutdownMinecraftApplet));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        mc.gameRenderer.setupOverlayRendering(2);

        MainWindow mainWindow = mc.getMainWindow();

        RenderUtility.drawShader(timer);

        float widthButton = 100;
        float heightButton = 20;
        float x = ClientUtility.calc(width) / 2f - widthButton / 2;
        float y = Math.round(ClientUtility.calc(height) / 2f - (heightButton * 8) / 2);

        float widthRect = widthButton * 1.5f;
        float xRect = x - widthButton / 4;
        float heightRect = (heightButton * 8);

        int bgRectColor = ColorUtility.rgba(40, 40, 40, 160);


        RectUtility.getInstance().drawRoundedRectShadowed(matrixStack, xRect, y - 5, xRect + widthRect, y - 5 + heightRect, 8, 5, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);

        RenderUtility.drawShadow(mainWindow.getScaledWidth() / 2 - 4 - (ClientFonts.msSemiBold[22].getWidth("Evaware") / 2), y + 2, ClientFonts.msSemiBold[22].getWidth("Evaware") + 8, ClientFonts.msSemiBold[22].getFontHeight(), 12, ColorUtility.rgba(255, 255, 255, 40));
        ClientFonts.msSemiBold[22].drawCenteredString(matrixStack, "Evaware", mainWindow.getScaledWidth() / 2, y + 4, -1);
        ClientFonts.msSemiBold[14].drawString(matrixStack, setMessage(), 3, mainWindow.getScaledHeight() - ClientFonts.msSemiBold[14].getFontHeight() + 1, -1);

        Vector2i fixed = ClientUtility.getMouse(mouseX, mouseY);

        drawButtons(matrixStack, fixed.getX(), fixed.getY(), partialTicks);

        mc.gameRenderer.setupOverlayRendering();
    }

    private final BetterText gavno = new BetterText(List.of(
            " <3", " >_<", " UwU", " O_O", " OwO", " :>", " <3", " >w<", "~~"
    ), 2000);

    private String setMessage() {
        gavno.update();
        String emoji = gavno.getOutput().toString();
        String userName = ClientUtility.getUsername() + emoji;

        return ClientUtility.getGreetingMessage() + ", " + userName;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixed = ClientUtility.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {
        buttons.forEach(b -> b.render(stack, mX, mY, pt));
    }

    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;
        public Animation animation = new Animation();
        boolean hovered;

        public Button(float x, float y, float width, float height, String text, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            animation.update();
            animation.run(hovered ? 1 : 0, hovered ? 0.25 : 0.5, Easings.BACK_OUT);
            hovered = MathUtility.isHovered(mouseX, mouseY, x, y, width, height);
            float offset = 1.5f;
            float hoverSize = (float) (offset * animation.getValue());
            float textY = (float) (y + 2 + height / 2 - ClientFonts.tenacity[(int) (19 + 3 * animation.getValue())].getFontHeight() / 2);
            int interColor = ColorUtility.interpolateColor(-1, ColorUtility.rgb(150, 150, 150), (float) animation.getValue());
            int bgRectColor = ColorUtility.rgba(90, 90, 90, 100);

            RectUtility.getInstance().drawRoundedRectShadowed(stack, x, y, x + width, y + height, 6, 2, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);
            ClientFonts.tenacity[(int) (19 + 3 * animation.getValue())].drawCenteredString(stack, text, x + width / 2f, textY, ColorUtility.setAlpha(interColor, (int) (255)));
            RenderUtility.drawImage(new ResourceLocation("eva/images/mainmenu/" + text + ".png"), x + offset * 2 - hoverSize, y + offset * 2 - hoverSize, height - offset * 4 + hoverSize * 2, height - offset * 4 + hoverSize * 2, interColor);
        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height)) {
                action.run();
                SoundPlayer.playSound("buttonclick.wav", .1f);
            }
        }

    }

}
