package newcode.fun.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.control.Manager;
import newcode.fun.utils.SoundUtils;
import newcode.fun.utils.anim.Animation;
import newcode.fun.utils.anim.Direction;
import newcode.fun.utils.anim.impl.EaseBackIn;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.font.styled.StyledFont;
import newcode.fun.utils.render.*;
import newcode.fun.utils.render.animation.AnimationMath;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

import static newcode.fun.utils.IMinecraft.mc;
import static newcode.fun.utils.IMinecraft.sr;

public class Window extends Screen {

    private Vector2f position = new Vector2f(0, 0);

    public static Vector2f size = new Vector2f(500, 400);

    public static int dark = new Color(18, 19, 25).getRGB();
    public static int medium = new Color(18, 19, 25).brighter().getRGB();
    public static int light = new Color(129, 134, 153).getRGB();
    private TypeList currentCategory;

    public static ArrayList<ModuleObject> objects = new ArrayList<>();

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for(newcode.fun.ui.clickgui.Panel p:panels){
            p.onScroll(mouseX,mouseY,delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public Window(ITextComponent titleIn) {
        super(titleIn);
        scrolling = 0;
        for (Module module : Manager.FUNCTION_MANAGER.getFunctions()) {
            objects.add(new ModuleObject(module));
        }
        size = new Vector2f(450, 350);
        position = new Vector2f(mc.getMainWindow().scaledWidth() / 2f, mc.getMainWindow().scaledHeight() / 2f);
        float offset = 0;
        float width = 120;
        for(TypeList typeList : TypeList.values()){
            panels.add(new newcode.fun.ui.clickgui.Panel(typeList,(mc.getMainWindow().scaledWidth() / 2f)+offset, mc.getMainWindow().scaledHeight() / 2f,width,300));
            offset+=width+3;
        }
    }

    ArrayList<newcode.fun.ui.clickgui.Panel> panels = new ArrayList<>();

    @Override
    protected void init() {
        super.init();
        panels.clear();
        size = new Vector2f(450, 350);
        float offset = 0;
        float width = 120;
        float height = 300;
        position = new Vector2f(mc.getMainWindow().scaledWidth() / 2f - (TypeList.values().length * width) / 2f, (mc.getMainWindow().scaledHeight() / 2f)-height/2f);
        for(TypeList typeList : TypeList.values()){
            panels.add(new newcode.fun.ui.clickgui.Panel(typeList,position.x + offset, position.y, width,height));
            offset+=width-29;
        }
        if (Manager.FUNCTION_MANAGER.clickGui.sounds.get()) {
            SoundUtils.playSound("guiopen.wav", 62, false);
        }
    }

    public static float scrolling;
    public static float scrollingOut;

    public static boolean searching;
    public static String searchText = "";
    public static boolean openAnimation=false;
    public Animation animation = new EaseBackIn(400, 1, 1.5f);

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        MatrixStack ms = new MatrixStack();

        GL11.glPushMatrix();
        mc.gameRenderer.setupOverlayRendering(2);

        Vec2i fixed = ScaleMath.getMouse(mouseX, mouseY);
        int scaledMouseX = fixed.getX();
        int scaledMouseY = fixed.getY();

        if (openAnimation) {
            animation.setDirection(Direction.FORWARDS);
        } else {
            animation.setDirection(Direction.BACKWARDS);
        }

        for (newcode.fun.ui.clickgui.Panel p : panels) {
            p.render(matrixStack, scaledMouseX, scaledMouseY);
        }
        newcode.fun.ui.clickgui.Panel.search(matrixStack);
        StyledFont newcode = Fonts.newcode[14];

        if (!searching && searchText.isEmpty()) {
            newcode.drawCenteredString(ms, "Search for something...", sr.scaledWidth() / 2 - 3f, sr.scaledHeight() / 1.17f + 5, ColorUtils.rgba(200, 200, 200, 200));
        } else {
            newcode.drawString(ms, searchText + (searching ? (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : ""), sr.scaledWidth() / 2 - 40f, sr.scaledHeight() / 1.17f + 5, ColorUtils.rgba(200, 200, 200, 200));
        }
        scrollingOut = AnimationMath.fast(scrollingOut, scrolling, 15);

        StencilUtils.initStencilToWrite();
        RenderUtils.Render2D.drawRoundedCorner(position.x, position.y, size.x, size.y, new Vector4f(8.5f, 8.5f, 8.5f, 8.5f), -1);
        StencilUtils.readStencilBuffer(0);
        StencilUtils.uninitStencilBuffer();

        if (animation.getOutput() < 0.1f && !openAnimation) {
            openAnimation = true;
        }
        GL11.glPopMatrix();
    }


    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searching && searchText.length() < 13) {
            searchText += codePoint;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for(newcode.fun.ui.clickgui.Panel p:panels){
            p.onKey(keyCode,scanCode,modifiers);
        }

        if(keyCode == 256){
            mc.displayGuiScreen(null);
            openAnimation = false;
        }

        if (keyCode == 259 && searching && !searchText.isEmpty()) {
            searchText = searchText.substring(0, searchText.length() - 1);
        }

        if (keyCode == 257) {
            searchText = "";
            searching = false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(newcode.fun.ui.clickgui.Panel p:panels){
            p.onRelease(mouseX,mouseY,button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        searching = false;
        searchText = "";
        openAnimation = false;
        for (ModuleObject m : objects) {
            m.exit();
        }
        if (Manager.FUNCTION_MANAGER.clickGui.sounds.get()) {
            SoundUtils.playSound("guiclose.wav", 62, false);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        for (TypeList typeList : TypeList.values()) {
            if (RenderUtils.isInRegion(mouseX, mouseY, sr.scaledWidth() / 2 - 55, sr.scaledHeight() / 1.17f, 110, 13)) {
                currentCategory = typeList;
                searching = false;
            }
        }

        for (ModuleObject m : objects) {

            if (searching || !searchText.isEmpty()) {
                if (!searchText.isEmpty()) {
                    if (!m.module.name.toLowerCase().contains(searchText.toLowerCase())) continue;
                }
                m.mouseClicked((int) mouseX, (int) mouseY, button);
            } else {
                if (m.module.category == currentCategory) {
                    m.mouseClicked((int) mouseX, (int) mouseY, button);
                }
            }
        }

        if (RenderUtils.isInRegion(mouseX, mouseY, sr.scaledWidth() / 2 - 55, sr.scaledHeight() / 1.17f, 110, 13)) {
            searching = !searching;
        }

        for (Panel p : panels) {
            p.onClick(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}