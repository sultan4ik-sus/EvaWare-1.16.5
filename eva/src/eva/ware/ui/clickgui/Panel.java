package eva.ware.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.Evaware;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.ui.clickgui.components.builder.IBuilder;
import eva.ware.ui.clickgui.components.builder.Component;
import eva.ware.ui.clickgui.components.ModuleComponent;
import eva.ware.manager.Theme;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.font.ClientFonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Panel implements IBuilder {

    private final Category category;
    protected float x;
    protected float y;
    protected final float width = 115;
    protected float height;

    public boolean binding;

    private List<ModuleComponent> modules = new ArrayList<>();
    private float scroll, animatedScrool;

    public Panel(Category category) {
        this.category = category;

        for (Module module : Evaware.getInst().getModuleManager().getModules()) {
            if (module.getCategory() == category) {
                ModuleComponent component = new ModuleComponent(module);
                component.setPanel(this);
                modules.add(component);
            }
        }

        updateHeight();
    }

    double base = 20;
    double biba = 28.5;
    double boba = 8.5;

    public void updateHeight() {
        final double additionalHeight = modules.stream().filter(ModuleComponent::isOpen).mapToDouble(ModuleComponent::getHeight).sum();

        this.height = (float) Math.max(biba, base + additionalHeight + boba);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        animatedScrool = MathUtility.fast(animatedScrool, scroll, 10);
        float headerFont = 9;

        updateHeight();
        height = (float) Math.max(biba, modules.stream().filter(component -> !Evaware.getInst().getClickGuiScreen().searchCheck(component.getModule().getName())).mapToDouble(ModuleComponent::getHeight).sum() + base + boba);

        RenderUtility.drawShadowFancyRectNoOutline(stack, x, y, width, height, (int) (180 * ClickGuiScreen.getGlobalAnim().getValue()));

        RenderUtility.drawRoundedRect(x + 2, y + 2, ClientFonts.icons_wex[35].getWidth(category.getIcon()) + 5, ClientFonts.icons_wex[35].getFontHeight() + 2, 4, ColorUtility.setAlpha(Theme.rectColor, (int) (70 * ClickGuiScreen.getGlobalAnim().getValue())));
        ClientFonts.tenacity[24].drawString(stack, category.name(), x + 9 + ClientFonts.icons_wex[35].getWidth(category.getIcon()), y + 12 - ClientFonts.tenacity[18].getFontHeight() / 2f, ColorUtility.rgba(255, 255, 255, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        ClientFonts.icons_wex[35].drawString(stack, category.getIcon(), x + 5, y + 14 - ClientFonts.icons_wex[30].getFontHeight() / 2f, ColorUtility.rgba(255, 255, 255, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));

        RenderUtility.drawRectHorizontalW(x + 0.5f, y + 18 + Fonts.montserrat.getHeight(headerFont) / 2f, width - (0.5f * 2), 2.5f, ColorUtility.rgba(0,0,0,0), ColorUtility.rgba(0,0,0, (int) (65 * ClickGuiScreen.getGlobalAnim().getValue())));

        drawComponents(stack, mouseX, mouseY);
    }

    float max = 0;

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float offset = -1;
        float header = 25;

        if (max > height - header - 10) {
            scroll = MathHelper.clamp(scroll, -max + height - header - 10, 0);
            animatedScrool = MathHelper.clamp(animatedScrool, -max + height - header - 10, 0);
        } else {
            scroll = 0;
            animatedScrool = 0;
        }

        for (ModuleComponent component : modules) {
            component.setX(getX() + 0.5f);
            component.setY(getY() + header + offset + 0.5f + animatedScrool);
            component.setWidth(getWidth() - 1);
            component.setHeight(20);
            component.expandAnim.update();
            component.hoverAnim.update();
            component.bindAnim.update();
            component.noBindAnim.update();
            binding = component.bind;

            if (Evaware.getInst().getClickGuiScreen().searchCheck(component.getModule().getName())){
                continue;
            }

            if (component.expandAnim.getValue() > 0 && Evaware.getInst().getClickGuiScreen().getExpandedModule() == component) {
                float componentOffset = 0;
                for (Component component2 : component.getComponents()) {
                    if (component2.isVisible())
                        componentOffset += component2.getHeight();
                }
                componentOffset *= (float) component.expandAnim.getValue();
                component.setHeight(component.getHeight() + componentOffset);
            }

            component.render(stack, mouseX, mouseY);
            offset += component.getHeight() + 0.1f;
        }

        max = offset;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            if (Evaware.getInst().getClickGuiScreen().searchCheck(component.getModule().getName())){
                continue;
            }
            component.mouseClick(mouseX, mouseY, button);
        }
    }


    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (ModuleComponent component : modules) {
            component.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : modules) {
            component.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            component.mouseRelease(mouseX, mouseY, button);
        }
    }

}