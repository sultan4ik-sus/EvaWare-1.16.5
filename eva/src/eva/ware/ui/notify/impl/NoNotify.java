package eva.ware.ui.notify.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.ui.notify.Notify;
import eva.ware.utils.math.Vector4i;

import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.font.ClientFonts;
import net.minecraft.client.gui.screen.ChatScreen;

public class NoNotify extends Notify {
    public NoNotify(String content, long delay) {
        super(content, delay);
        animationY.setValue(window.getScaledHeight());
        alphaAnimation.setValue(0);
    }

    @Override
    public void render(MatrixStack matrixStack, int multiplierY) {
        int fontSize = 14;
        int iconSizeF = 26;
        this.end = ((this.getInit() + this.getDelay()) - System.currentTimeMillis()) <= (this.getDelay() - 500) - this.getDelay();
        if (mc.currentScreen instanceof ChatScreen) {
            chatOffset.run(ClientFonts.msMedium[fontSize].getFontHeight() + 32);
        } else {
            chatOffset.run(ClientFonts.msMedium[fontSize].getFontHeight() + 2);
        }

        float contentWidth = ClientFonts.msMedium[fontSize].getWidth(getContent());

        float x, y, width, height;

        width = margin + contentWidth + margin;
        height = (margin / 2F) + ClientFonts.msMedium[fontSize].getFontHeight() + (margin / 2F);

        x = (float) (window.getScaledWidth() - width - margin) + 2;
        y = (float) ((window.getScaledHeight() - height) - 1 - (height * multiplierY) - (multiplierY * 4) - chatOffset.getValue());
        float iconSize = ClientFonts.icons_wex[iconSizeF].getWidth("I");
        alphaAnimation.run(this.end ? 0 : 1);
        animationY.run(this.end ? y + 1 : y);

        float posX = x;
        float posY = (float) animationY.getValue();

        double alphaValue = alphaAnimation.getValue();
        int i = ColorUtility.rgba(255, 86, 86, (int) (60 * alphaValue));
        int o = ColorUtility.rgba(255, 44, 44, (int) (alphaValue));

        float animPos = (float) (width - width * alphaValue);
        RenderUtility.drawShadow(posX - iconSize + animPos, posY, width + iconSize, height, 8, i, o);
        RenderUtility.drawRoundedRect(posX - iconSize + animPos, posY, width + iconSize, height, 6, new Vector4i(i, i, o, o));
        ClientFonts.icons_wex[iconSizeF].drawString(matrixStack, "I", posX - (iconSize - 2.5) + animPos, posY - height / 2 + ClientFonts.icons_wex[iconSizeF].getFontHeight() - margin / 2, ColorUtility.reAlphaInt(-1, (int) (255 * alphaValue)));
        ClientFonts.msMedium[fontSize].drawString(matrixStack, getContent(), posX + margin + animPos, posY + margin / 2F + 2.5f, ColorUtility.reAlphaInt(-1, (int) (255 * alphaValue)));
    }

    @Override
    public boolean hasExpired() {
        return this.animationY.isFinished() && this.end;
    }
}
