package eva.ware.ui.clienthud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.platform.GlStateManager;
import eva.ware.Evaware;
import eva.ware.events.EventRender2D;
import eva.ware.modules.api.Module;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.manager.Theme;
import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;
import eva.ware.utils.animations.impl.EaseBackIn;
import eva.ware.utils.client.KeyStorage;
import eva.ware.manager.drag.Dragging;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.other.Scissor;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Keybinds implements ElementRenderer {

    final Dragging dragging;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    double width;
    float height;
    final AnimationUtility animation = new EaseBackIn(300, 1, 1);

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;
        float iconSize = 10;
        float margin = 2f;
        String name = "Keybinds";
        float nameWidth = Fonts.montserrat.getWidth(name, fontSize, 0.07f);
        boolean isAnyModuleEnabled = false;


        for (Module f : Evaware.getInst().getModuleManager().getModules()) {
            f.getAnimation().update();
            if (f.getAnimation().getValue() > 0.1 && f.getBind() != 0 || mc.currentScreen instanceof ChatScreen) {
                isAnyModuleEnabled = true;
                break;
            }
        }

        animation.setDirection(isAnyModuleEnabled ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyModuleEnabled ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtility.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());

        RenderUtility.drawStyledRect(ms, posX, posY, (float) width, height);
        Fonts.montserrat.drawText(ms, name, posX + iconSize + 8, posY + padding + 0.5f, Theme.textColor, fontSize, 0.07f);
        ClientFonts.icons_nur[20].drawString(ms, "F", posX + 5, posY + 6.5f, Theme.textColor);

        float maxWidth = Fonts.montserrat.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        posY += fontSize + padding + 2f;
        posY += 4.5f;
        for (Module f : Evaware.getInst().getModuleManager().getModules()) {
            f.getAnimation().update();
            int color = ColorUtility.setAlpha(Theme.textColor, (int) (255 * f.getAnimation().getValue()));
            if (!(f.getAnimation().getValue() > 0.1) || f.getBind() == 0) continue;
            String nameText = f.getName();
            float moduleWidth = Fonts.montserrat.getWidth(nameText, fontSize);

            String bindText = KeyStorage.getKey(f.getBind());
            float bindWidth = Fonts.montserrat.getWidth(bindText, fontSize);

            float localWidth = moduleWidth + bindWidth + padding * 3;
            Fonts.montserrat.drawText(ms, nameText, (float) (posX + padding - 0.5f - 4 + 4 * f.getAnimation().getValue()), (float) (posY + 5.5 - 3 * f.getAnimation().getValue()), color, (float) (fontSize - 3 + 3 * f.getAnimation().getValue()), 0.05f);
            Fonts.montserrat.drawText(ms, bindText, (float) (posX + 1 + width - padding - bindWidth * f.getAnimation().getValue()), (float) (posY + 5.5 - 3 * f.getAnimation().getValue()), color, (float) (fontSize - 3 + 3 * f.getAnimation().getValue()), 0.05f);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (float) ((fontSize + padding - 3f) * f.getAnimation().getValue());
            localHeight += (float) (fontSize + padding - 3f);
        }
        Scissor.unset();
        Scissor.pop();

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth, nameWidth + iconSize + 25));
        width = widthAnimation.getValue();
        heightAnimation.run((localHeight + 5.5));
        height = (float) heightAnimation.getValue();
        dragging.setWidth((float) width);
        dragging.setHeight(height);
    }
}
