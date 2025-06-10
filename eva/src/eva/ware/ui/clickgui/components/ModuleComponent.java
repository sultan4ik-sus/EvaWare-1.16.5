package eva.ware.ui.clickgui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.Evaware;
import eva.ware.modules.api.Module;
import eva.ware.modules.settings.api.Setting;
import eva.ware.modules.settings.impl.*;
import eva.ware.ui.clickgui.ClickGuiScreen;
import eva.ware.ui.clickgui.Panel;
import eva.ware.ui.clickgui.components.builder.Component;
import eva.ware.ui.clickgui.components.settings.*;
import eva.ware.manager.Theme;
import eva.ware.utils.client.SoundPlayer;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.Vector4i;
import eva.ware.utils.render.Cursors;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.render.other.Stencil;
import eva.ware.utils.render.engine2d.RectUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.BetterText;
import eva.ware.utils.text.font.ClientFonts;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import eva.ware.utils.client.KeyStorage;

import java.util.List;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(5, 5, 5, 5);
    private final Vector4i BORDER_COLOR = new Vector4i(ColorUtility.rgb(45, 46, 53), ColorUtility.rgb(25, 26, 31), ColorUtility.rgb(45, 46, 53), ColorUtility.rgb(25, 26, 31));
    private final Module module;
    protected Panel panel;
    public Animation expandAnim = new Animation();

    public Animation hoverAnim = new Animation();

    public Animation bindAnim = new Animation();
    public Animation noBindAnim = new Animation();

    public boolean open;

    public boolean bind;

    private double openAnimValue = 0.3, noOpenAnimValue = 0.4;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Module module) {
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof CheckBoxSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof ModeListSetting mode) {
                components.add(new MultiBoxComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }
            if (setting instanceof ColorSetting color) {
                components.add(new ColorComponent(color));
            }
        }
        expandAnim = expandAnim.animate(open ? 1 : 0, open ? openAnimValue : noOpenAnimValue, Easings.EXPO_OUT);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }

        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        module.getAnimation().update();
        betterText.update();

        if (Evaware.getInst().getClickGuiScreen().getExpandedModule() != this) open = false;

        boolean hover = MathUtility.isHovered(mouseX, mouseY, getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight());

        if (hover) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        }

        hoverAnim.animate(hover ? 1 : 0, 0.3, Easings.BACK_OUT);
        bindAnim.animate(bind ? 1 : 0, 0.3, Easings.BACK_OUT);
        noBindAnim.animate(!bind ? 1 : 0, 0.3, Easings.BACK_OUT);
        double posAnim = (1.5 * hoverAnim.getValue());

        int color = ColorUtility.interpolate(ColorUtility.setAlpha(Theme.textColor, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), ColorUtility.rgba(161, 164, 177, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), (float) module.getAnimation().getValue());
        int rectAlpha = (int) ((20 * module.getAnimation().getValue()) * ClickGuiScreen.getGlobalAnim().getValue());
        int hoverColorWhenNoActive = ColorUtility.interpolateColor(Theme.textColor, Theme.mainRectColor, (float) hoverAnim.getValue());
        int hoverColorWhenActive = ColorUtility.interpolateColor(Theme.textColor, Theme.rectColor, (float) hoverAnim.getValue());
        int rectColor = module.isEnabled() ? ColorUtility.setAlpha(hoverColorWhenActive, rectAlpha) : ColorUtility.setAlpha(hoverColorWhenNoActive, (int) (15 * ClickGuiScreen.getGlobalAnim().getValue()));
        int statusColor = ColorUtility.setAlpha(ColorUtility.interpolateColor(Theme.rectColor, Theme.mainRectColor, (float)module.getAnimation().getValue()), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));

        float offMe = (float) (0.5f + 2 * hoverAnim.getValue());

        RectUtility.getInstance().drawRoundedRectShadowed(stack, getX() + offMe, getY() + offMe, getX() + getWidth() - offMe, getY() + getHeight() - offMe, 3, (float) (3 * hoverAnim.getValue()), rectColor, rectColor, rectColor, rectColor, false, false, true, hoverAnim.getValue() > 0.1);

        int colorForModuleText = ColorUtility.setAlpha(color, (int) ((255 * noBindAnim.getValue() * ClickGuiScreen.getGlobalAnim().getValue())));
        int colorForBindText = ColorUtility.interpolateColor(ColorUtility.rgba(161, 164, 177, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), ColorUtility.setAlpha(ColorUtility.rgb(161, 164, 177), 0), (float) bindAnim.getValue());

        if (!bind) RenderUtility.drawShadow((float)(getX() + 6 + posAnim), getY() + 3.5F, ClientFonts.comfortaa[17].getWidth(module.getName()) + 3.0F, ClientFonts.comfortaa[17].getFontHeight(), 10, ColorUtility.setAlpha(color, (int)((45.0D * this.module.getAnimation().getValue()) * ClickGuiScreen.getGlobalAnim().getValue())));
        ClientFonts.comfortaa[17].drawString(stack, module.getName(),  (float)(getX() + 6 + posAnim), getY() + 6.5F, colorForModuleText);

        if (this.components.stream().filter(Component::isVisible).count() >= 1L) {
            RenderUtility.drawShadowCircle(getX() + getWidth() - 8, getY() + 10, 3, statusColor);
            RenderUtility.drawCircle(getX() + getWidth() - 8, getY() + 10 + 0.5f, 3f, ColorUtility.setAlpha(statusColor, (int) (120 * ClickGuiScreen.getGlobalAnim().getValue())));
        }

        Fonts.montserrat.drawText(stack, "Bind" + (module.getBind() == 0 ? betterText.getOutput() : ": " + KeyStorage.getReverseKey(module.getBind())), (float)(getX() + 6 + posAnim), getY() + 6F, colorForBindText, 8F, 0.03F);

        if (expandAnim.getValue() > 0) {
            if (components.stream().filter(Component::isVisible).count() >= 1) {
                RenderUtility.drawRectVerticalW(getX() + 5, getY() + 18, getWidth() - 10, 0.5f, ColorUtility.setAlpha(Theme.rectColor, (int) ((200 * expandAnim.getValue()) * ClickGuiScreen.getGlobalAnim().getValue())), ColorUtility.setAlpha(Theme.rectColor, (int) ((200 * expandAnim.getValue()) * ClickGuiScreen.getGlobalAnim().getValue())));
            }
            Stencil.initStencilToWrite();
            RenderUtility.drawRoundedRect(getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight() - 1, ROUNDING_VECTOR, ColorUtility.rgba(23, 23, 23, (int) (255 * 0.33)));
            Stencil.readStencilBuffer(1);

            float y = getY() + 20;
            for (Component component : components) {
                if (component.isVisible()){
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY );
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, getX() + 1, getY() + 1, getWidth() - 2, 18)) {
            ModuleComponent openModule = Evaware.getInst().getClickGuiScreen().getExpandedModule();
            if (openModule != null && openModule != this && button == 1 && !module.getSettings().isEmpty()) {
                openModule.open = false;
                openModule.expandAnim.animate(0, noOpenAnimValue, Easings.EXPO_OUT);
            }
            if (button == 0 && !bind) module.toggle();
            if (button == 1 && !bind && expandAnim.isDone()) {
                if (!module.getSettings().isEmpty()) {
                    open = !open;
                    SoundPlayer.playSound(open ? "moduleonopen.wav" : "moduleonclose.wav");
                    if (expandAnim.isDone()) SoundPlayer.playSound(open ? "moduleopen.wav" : "moduleclose.wav");

                    if (open) {
                        Evaware.getInst().getClickGuiScreen().setExpandedModule(this);
                        expandAnim = expandAnim.animate(1, openAnimValue, Easings.EXPO_OUT);
                    }

                    expandAnim = expandAnim.animate(open ? 1 : 0, open ? openAnimValue : noOpenAnimValue, Easings.EXPO_OUT);
                }
            }
            if (button == 2) {
                bind = !bind;

                SoundPlayer.playSound(bind ? "guibindingstart.wav" : "guibindingnull.wav");
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (open) {
                for (Component component : components) {
                    if (component.isVisible()) component.mouseClick(mouseX, mouseY, button);
                }
            }
        }
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.keyPressed(key, scanCode, modifiers);
        }
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) {
                module.setBind(0);
                SoundPlayer.playSound("guibindreset.wav");
            } else {
                module.setBind(key);
                SoundPlayer.playSound("guibinding.wav");
            }
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }

    private final BetterText betterText = new BetterText(List.of(
            "...", "...", "..."
    ), 100);
}