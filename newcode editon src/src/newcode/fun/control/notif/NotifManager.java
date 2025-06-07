package newcode.fun.control.notif;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import newcode.fun.control.Manager;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.IWrapper;
import newcode.fun.utils.anim.Animation;
import newcode.fun.utils.anim.Direction;
import newcode.fun.utils.anim.impl.DecelerateAnimation;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.animation.AnimationMath;

import java.util.concurrent.CopyOnWriteArrayList;

import static newcode.fun.utils.render.ColorUtils.rgba;

public class NotifManager {

    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public void add(String text, String content, int time) {
        notifications.add(new Notification(text, content, time));
    }


    public void draw(MatrixStack stack) {
        float yOffset = 0;
        for (Notification notification : notifications) {

            if (System.currentTimeMillis() - notification.getTime() > (notification.time2 * 1000L) - 300) {
                notification.animation.setDirection(Direction.BACKWARDS);
            } else {
                notification.yAnimation.setDirection(Direction.FORWARDS);
                notification.animation.setDirection(Direction.FORWARDS);
            }
            notification.alpha = (float) notification.animation.getOutput();
            if (System.currentTimeMillis() - notification.getTime() > notification.time2 * 1000L) {
                notification.yAnimation.setDirection(Direction.BACKWARDS);
            }
            if (notification.yAnimation.finished(Direction.BACKWARDS)) {
                notifications.remove(notification);
                continue;
            }
            float x = IMinecraft.mc.getMainWindow().scaledWidth() - (Fonts.gilroyBold[14].getWidth(notification.getText()) + 8) - 10;
            float y = IMinecraft.mc.getMainWindow().scaledHeight() - 30;
            notification.yAnimation.setEndPoint(yOffset);
            notification.yAnimation.setDuration(300);
            y -= (float) (notification.draw(stack) * notification.yAnimation.getOutput()) - 4;
            notification.setX(x);
            notification.setY(AnimationMath.fast(notification.getY(), y, 15));
            yOffset += 0.7f;
        }
    }


    private class Notification {
        @Getter
        @Setter
        private float x, y = IMinecraft.mc.getMainWindow().scaledHeight() + 24;

        @Getter
        private String text;
        @Getter
        private String content;
        private boolean isState;
        private boolean state;
        @Getter
        private long time = System.currentTimeMillis();

        public Animation animation = new DecelerateAnimation(500, 1, Direction.FORWARDS);
        public Animation yAnimation = new DecelerateAnimation(500, 1, Direction.FORWARDS);
        float alpha;
        int time2 = 3;

        public Notification(String text, String content, int time) {
            this.text = text;
            this.content = content;
            time2 = time;
        }

        public float draw(MatrixStack stack) {
            float width = Fonts.gilroyBold[14].getWidth(text) + 8;
            int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
            int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x - 5, y - 2, width + 7, 14, 5f, RenderUtils.reAlphaInt(rgba(20, 20, 20, 200), (int) (200 * alpha))));
            RenderUtils.Render2D.drawRoundedRect(x - 5, y - 2, width + 7, 14, 5f, RenderUtils.reAlphaInt(rgba(20, 20, 20, 200), (int) (200 * alpha)));
            if (this.text.contains("enabled!")) {
                Fonts.icon[16].drawCenteredString(stack, "k", x - 2.7f + 5, y + 4f, RenderUtils.reAlphaInt(firstColor2, (int) (255 * alpha)));
            } else if (this.text.contains("disabled!")) {
                Fonts.icon[16].drawCenteredString(stack, "j", x - 2.7f + 5, y + 4f, RenderUtils.reAlphaInt(rgba(120, 120, 120, 255), (int) (255 * alpha)));
            } else {
                Fonts.icon[16].drawCenteredString(stack, "i", x - 2.7f + 5, y + 4f, RenderUtils.reAlphaInt(firstColor2, (int) (255 * alpha)));
            }

            Fonts.newcode[14].drawString(stack, text, x + 5 + 3, y + 3.5f, RenderUtils.reAlphaInt(-1, (int) (255 * alpha)));
            return 24;
        }
    }
}
