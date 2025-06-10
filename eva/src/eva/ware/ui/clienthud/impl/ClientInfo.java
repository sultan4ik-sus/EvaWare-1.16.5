package eva.ware.ui.clienthud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import eva.ware.Evaware;
import eva.ware.events.EventRender2D;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;

import eva.ware.utils.player.PlayerUtility;
import eva.ware.utils.text.font.ClientFonts;
import eva.ware.utils.text.font.styled.StyledFont;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.text.TextFormatting;

@RequiredArgsConstructor
public class ClientInfo implements ElementRenderer {
    final CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_QUAD, 100);

    @Override
    public void render(EventRender2D eventRender2D) {
        final StyledFont font = ClientFonts.msMedium[12];
        boolean isChatScreen = (mc.currentScreen instanceof ChatScreen);
        int margin = 2;
        animation.run((isChatScreen ? 10 + margin : 0));
        double chat = animation.getValue();
        MatrixStack ms = eventRender2D.getMatrixStack();

        // хорошо что дцп не приговор
        String bps = PlayerUtility.stringEntityBps(mc.player, true);
        String tps = String.format("%.1f", Evaware.getInst().getTpsCalc().getTPS());

        StringBuilder leftSide = new StringBuilder();
        if (isEnabled("Координаты")) {
            leftSide.append(TextFormatting.WHITE).append("XYZ: ").append(TextFormatting.GRAY).append((int) mc.player.getPosX()).append(", ").append((int) mc.player.getPosY()).append(", ").append((int) mc.player.getPosZ()).append(TextFormatting.WHITE);
        }

        if (isEnabled("Координаты") && isEnabled("Скорость")) {
            leftSide.append(" ");
        }
        if (isEnabled("Скорость")) {
            leftSide.append(TextFormatting.WHITE).append("BPS: ").append(TextFormatting.GRAY).append(bps);
        }

        StringBuilder rightSide = new StringBuilder();
        if (isEnabled("ТПС")) {
            rightSide.append(TextFormatting.WHITE).append("TPS: ").append(TextFormatting.GRAY).append(tps);
        }
        float y = (float) (scaled().y - font.getFontHeight() - margin + 2);

        font.drawStringWithOutline(ms, leftSide.toString(), margin, y - chat, -1);
        font.drawStringWithOutline(ms, rightSide.toString(), scaled().x - margin - font.getWidth(rightSide.toString()), y - (chat * 2.62), -1);
    }

    private boolean isEnabled(String check) {
        return Evaware.getInst().getModuleManager().getHud().infoOptions.is(check).getValue();
    }
}

