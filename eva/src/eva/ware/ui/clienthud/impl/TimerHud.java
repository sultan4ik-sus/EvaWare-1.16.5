package eva.ware.ui.clienthud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import eva.ware.Evaware;
import eva.ware.events.EventRender2D;
import eva.ware.events.EventUpdate;
import eva.ware.modules.impl.movement.Timer;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.ui.clienthud.updater.ElementUpdater;
import eva.ware.manager.Theme;
import eva.ware.manager.drag.Dragging;
import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;
import eva.ware.utils.animations.impl.EaseBackIn;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.player.MoveUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TimerHud implements ElementRenderer, ElementUpdater {
    final AnimationUtility animation = new EaseBackIn(400, 1, 1);
    final Dragging dragging;
    float perc;

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();
        Timer timer = Evaware.getInst().getModuleManager().getTimer();
        String text = "Timer";

        boolean isTimerCharged = false;
        float posX = dragging.getX();
        float posY = dragging.getY();
        float quotient = timer.maxViolation / timer.speed.getValue();
        float minimumValue = Math.min(timer.violation, quotient);
        float textWidth = ClientFonts.msMedium[16].getWidth(text);
        float width = 40 + textWidth + 3;
        float height = 15;
        float timerWidth = ((width - textWidth - 9) * perc);
        perc = MathUtility.lerp(perc, ((quotient - minimumValue) / quotient), 10);
        dragging.setWidth(width);
        dragging.setHeight(height);

        if (perc < 0.96 || mc.currentScreen instanceof ChatScreen) {
            isTimerCharged = true;
        }

        animation.setDirection(isTimerCharged ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isTimerCharged ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtility.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());

        RenderUtility.drawStyledShadowRectWithChange(ms, posX, posY, width, height);

        RenderUtility.drawRoundedRect(posX + 3, posY + 3, timerWidth, height - (3 * 2), new Vector4f(3, 3, 3, 3), Theme.mainRectColor);
        RenderUtility.drawShadow(posX + 3, posY + 3, timerWidth, height - (3 * 2), 8, Theme.mainRectColor);

        ClientFonts.msMedium[16].drawString(ms, text, Math.max(posX + 3, posX + timerWidth + 5.5f), posY - 2f + 7.5f, Theme.textColor);

        GlStateManager.popMatrix();
    }

    @Override
    public void update(EventUpdate e) {
        Timer timer = Evaware.getInst().getModuleManager().getTimer();

        if (!MoveUtility.isMoving()) {
            timer.violation = (float) ((double) timer.violation - ((double) timer.ticks.getValue() + 0.4));
        } else if (timer.moveUp.getValue()) {
            timer.violation -= (timer.moveUpValue.getValue());
        }

        timer.violation = (float) MathHelper.clamp(timer.violation, 0.0, Math.floor(timer.maxViolation));
    }
}
