package eva.ware.ui.clienthud.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.platform.GlStateManager;
import eva.ware.events.EventRender2D;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.manager.Theme;
import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;
import eva.ware.manager.drag.Dragging;
import eva.ware.utils.animations.impl.EaseBackIn;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.other.Scissor;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.potion.Effect;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PotionHud implements ElementRenderer {

    final Dragging dragging;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    float width;
    float height;
    private Map<String, CompactAnimation> animations = new HashMap<>();

    final AnimationUtility animation = new EaseBackIn(300, 1, 1);

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;
        float iconSize = 10;
        boolean isAnyPotionActive = false;

        for (EffectInstance effectInstance : mc.player.getActivePotionEffects()) {
            if (effectInstance.getDuration() > 0) {
                isAnyPotionActive = true;
                break;
            }
        }

        if (mc.currentScreen instanceof ChatScreen) {
            isAnyPotionActive = true;
        }


        String name = "Potions";
        float nameWidth = Fonts.montserrat.getWidth(name, fontSize, 0.07f);

        animation.setDirection(isAnyPotionActive ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyPotionActive ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtility.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());

        RenderUtility.drawStyledRect(ms, posX, posY, width, height);
        ClientFonts.icons_nur[20].drawString(ms, "B", posX + 5, posY + 7f, Theme.textColor);
        Fonts.montserrat.drawText(ms, name, posX + iconSize + 8, posY + padding + 0.5f, Theme.textColor, fontSize, 0.07f);
        posY += fontSize + padding + 2;
        posY += 5f;

        float maxWidth = Fonts.montserrat.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        for (Iterator ef = mc.player.getActivePotionEffects().iterator(); ef.hasNext();) {

            EffectInstance effectInstance = (EffectInstance)ef.next();
            String ampStr = "";
            int amp = effectInstance.getAmplifier() + 1;

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + amp;
            }

            String nameText = I18n.format(effectInstance.getEffectName()) + ampStr;
            String durText = EffectUtils.getPotionDurationString(effectInstance, 1.0F);
            float effectWidth = Fonts.montserrat.getWidth(nameText, fontSize);
            float durWidth = Fonts.montserrat.getWidth(durText, fontSize);
            float localWidth = effectWidth + durWidth + padding * 3 + 10;

            PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
            Effect effect = effectInstance.getPotion();
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
            mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());

            CompactAnimation efAnimation = animations.getOrDefault(effectInstance.getEffectName(), null);
            if (efAnimation == null) {
                efAnimation = new CompactAnimation(Easing.EASE_OUT_CIRC, 250);
                animations.put(effectInstance.getEffectName(), efAnimation);
            }

            boolean potionActive = effectInstance.getDuration() > 3;
            efAnimation.run(potionActive ? 1 : 0);

            int color = ColorUtility.reAlphaInt(Theme.textColor, (int) (255 * efAnimation.getValue()));
            float off = (float) (8 * efAnimation.getValue());
            if (efAnimation.getValue() >= 0.3) {
                float potX = (float) (posX + 4.5f - 1 + 1 * efAnimation.getValue());
                float potY = (float) ((posY + 6 - 5 * efAnimation.getValue()));
                DisplayEffectsScreen.blit(ms, potX, potY, 10, off, off, textureatlassprite);
            }

            Scissor.push();
            Scissor.setFromComponentCoordinates(posX, posY, width, height);
            Fonts.montserrat.drawText(ms, nameText, posX + padding + 0.5f + off, (float) (posY + 5.5 - 3 * efAnimation.getValue()), color, (float) (fontSize - 3 + 3 * efAnimation.getValue()), 0.05f);
            Fonts.montserrat.drawText(ms, durText, posX + width - padding - durWidth + (8 - off) * 2, (float) (posY + 5.5 - 3 * efAnimation.getValue()), color, (float) (fontSize - 3 + 3 * efAnimation.getValue()), 0.05f);
            Scissor.unset();
            Scissor.pop();

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (float) ((fontSize + padding - 3) * efAnimation.getValue());
            localHeight += (fontSize + padding - 3);
        }

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth, nameWidth + iconSize + 25));
        width = (float) widthAnimation.getValue();
        heightAnimation.run((localHeight + 5.5f));
        height = (float) heightAnimation.getValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }
}
