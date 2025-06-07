package newcode.fun.control.drag;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MainWindow;
import newcode.fun.module.api.Module;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.math.MathUtil;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.Vec2i;

import static newcode.fun.utils.math.MathUtil.lerp;

public class Dragging {
    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;

    private float targetXPos;
    private float targetYPos;

    public float initialXVal;
    public float initialYVal;

    private float startX, startY;
    private boolean dragging;

    private float width, height;

    @Expose
    @SerializedName("name")
    private final String name;

    private final Module module;

    private static final float CENTER_LINE_WIDTH = 1f;
    private static final float SNAP_THRESHOLD = 10.0f;

    private float lineAlpha = 0.0f;
    private long lastUpdateTime;

    private boolean snapToCenter, snapToCenterx, snapToCenter2x, snapToCenter3x, snapToCenter4x, snapToCenter5x, snapToCenter2, snapToCenter3, snapToCenter4, snapToCenter5;

    private static final float LERP_SPEED = 0.19f;

    public Dragging(Module module, String name, float initialXVal, float initialYVal) {
        this.module = module;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.targetXPos = initialXVal;
        this.targetYPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getX() {
        return xPos;
    }

    public void setX(float x) {
        this.xPos = x;
    }

    public float getY() {
        return yPos;
    }

    public void setY(float y) {
        this.yPos = y;
    }

    public final void onDraw(int mouseX, int mouseY, MainWindow res) {
        Vec2i fixed = ClientUtils.getMouse(mouseX, mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float centerX = res.scaledWidth() / 2.0f;
        float centerY = res.scaledHeight() / 2.0f;

        float centerX2 = res.scaledWidth() / 4.0f;
        float centerY2 = res.scaledHeight() / 4.0f;
        float centerX3 = res.scaledWidth() / 8.0f;
        float centerY3 = res.scaledHeight() / 8.0f;

        float centerX4 = res.scaledWidth() / 1.15f;
        float centerY4 = res.scaledHeight() / 1.15f;
        float centerX5 = res.scaledWidth() / 1.35f;
        float centerY5 = res.scaledHeight() / 1.35f;

        snapToCenter = snapToCenterx = snapToCenter2x = snapToCenter3x = snapToCenter4x = snapToCenter5x = snapToCenter2 = snapToCenter3 = snapToCenter4 = snapToCenter5 = false;

        if (dragging) {
            targetXPos = (mouseX - startX);
            targetYPos = (mouseY - startY);

            boolean snapped = false;

            if (Math.abs(targetXPos + width / 2.0f - centerX) < SNAP_THRESHOLD) {
                targetXPos = centerX - width / 2.0f;
                snapToCenterx = true;
                snapped = true;
            }

            if (Math.abs(targetYPos + height / 2.0f - centerY) < SNAP_THRESHOLD) {
                targetYPos = centerY - height / 2.0f;
                snapToCenter = true;
                snapped = true;
            }

            if (Math.abs(targetXPos + width / 2.0f - centerX2) < SNAP_THRESHOLD) {
                targetXPos = centerX2 - width / 2.0f;
                snapToCenter2x = true;
                snapped = true;
            }

            if (Math.abs(targetYPos + height / 2.0f - centerY2) < SNAP_THRESHOLD) {
                targetYPos = centerY2 - height / 2.0f;
                snapToCenter2 = true;
                snapped = true;
            }

            if (Math.abs(targetXPos + width / 2.0f - centerX3) < SNAP_THRESHOLD) {
                targetXPos = centerX3 - width / 2.0f;
                snapToCenter3x = true;
                snapped = true;
            }

            if (Math.abs(targetYPos + height / 2.0f - centerY3) < SNAP_THRESHOLD) {
                targetYPos = centerY3 - height / 2.0f;
                snapToCenter3 = true;
                snapped = true;
            }

            if (Math.abs(targetXPos + width / 2.0f - centerX4) < SNAP_THRESHOLD) {
                targetXPos = centerX4 - width / 2.0f;
                snapToCenter4x = true;
                snapped = true;
            }

            if (Math.abs(targetYPos + height / 2.0f - centerY4) < SNAP_THRESHOLD) {
                targetYPos = centerY4 - height / 2.0f;
                snapToCenter4 = true;
                snapped = true;
            }

            if (Math.abs(targetXPos + width / 2.0f - centerX5) < SNAP_THRESHOLD) {
                targetXPos = centerX5 - width / 2.0f;
                snapToCenter5x = true;
                snapped = true;
            }

            if (Math.abs(targetYPos + height / 2.0f - centerY5) < SNAP_THRESHOLD) {
                targetYPos = centerY5 - height / 2.0f;
                snapToCenter5 = true;
                snapped = true;
            }

            if (targetXPos + width > res.scaledWidth()) {
                targetXPos = res.scaledWidth() - width;
            }
            if (targetYPos + height > res.scaledHeight()) {
                targetYPos = res.scaledHeight() - height;
            }
            if (targetXPos < 0) {
                targetXPos = 0;
            }
            if (targetYPos < 0) {
                targetYPos = 0;
            }

            xPos = lerp(xPos, targetXPos, LERP_SPEED);
            yPos = lerp(yPos, targetYPos, LERP_SPEED);

            updateLineAlpha(snapped);
        } else {
            updateLineAlpha(false);
        }

        drawCenterLines(res);
    }


    private boolean checkSnapToCenter(MainWindow res) {
        float centerX = res.scaledWidth() / 2.0f;
        float centerY = res.scaledHeight() / 2.0f;
        float centerX2 = res.scaledWidth() / 4.0f;
        float centerY2 = res.scaledHeight() / 4.0f;
        float centerX3 = res.scaledWidth() / 8.0f;
        float centerY3 = res.scaledHeight() / 8.0f;
        float centerX4 = res.scaledWidth() / 1.15f;
        float centerY4 = res.scaledHeight() / 1.15f;
        float centerX5 = res.scaledWidth() / 1.35f;
        float centerY5 = res.scaledHeight() / 1.35f;

        snapToCenter = snapToCenterx = snapToCenter2x = snapToCenter3x = snapToCenter4x = snapToCenter5x = snapToCenter2 = snapToCenter3 = snapToCenter4 = snapToCenter5 = false;
        boolean snapped = false;

        if (Math.abs(targetXPos + width / 2.0f - centerX) < SNAP_THRESHOLD) {
            targetXPos = centerX - width / 2.0f;
            snapToCenterx = true;
            snapped = true;
        }

        if (Math.abs(targetYPos + height / 2.0f - centerY) < SNAP_THRESHOLD) {
            targetYPos = centerY - height / 2.0f;
            snapToCenter = true;
            snapped = true;
        }

        if (Math.abs(targetXPos + width / 2.0f - centerX2) < SNAP_THRESHOLD) {
            targetXPos = centerX2 - width / 2.0f;
            snapToCenter2x = true;
            snapped = true;
        }

        if (Math.abs(targetYPos + height / 2.0f - centerY2) < SNAP_THRESHOLD) {
            targetYPos = centerY2 - height / 2.0f;
            snapToCenter2 = true;
            snapped = true;
        }

        if (Math.abs(targetXPos + width / 2.0f - centerX3) < SNAP_THRESHOLD) {
            targetXPos = centerX3 - width / 2.0f;
            snapToCenter3x = true;
            snapped = true;
        }

        if (Math.abs(targetYPos + height / 2.0f - centerY3) < SNAP_THRESHOLD) {
            targetYPos = centerY3 - height / 2.0f;
            snapToCenter3 = true;
            snapped = true;
        }

        if (Math.abs(targetXPos + width / 2.0f - centerX4) < SNAP_THRESHOLD) {
            targetXPos = centerX4 - width / 2.0f;
            snapToCenter4x = true;
            snapped = true;
        }

        if (Math.abs(targetYPos + height / 2.0f - centerY4) < SNAP_THRESHOLD) {
            targetYPos = centerY4 - height / 2.0f;
            snapToCenter4 = true;
            snapped = true;
        }

        if (Math.abs(targetXPos + width / 2.0f - centerX5) < SNAP_THRESHOLD) {
            targetXPos = centerX5 - width / 2.0f;
            snapToCenter5x = true;
            snapped = true;
        }

        if (Math.abs(targetYPos + height / 2.0f - centerY5) < SNAP_THRESHOLD) {
            targetYPos = centerY5 - height / 2.0f;
            snapToCenter5 = true;
            snapped = true;
        }

        return snapped;
    }

    private void updateLineAlpha(boolean active) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        float fadeSpeed = 2.0f;
        float fadeOutSpeed = 2.0f;

        if (active) {
            lineAlpha += deltaTime * fadeSpeed;
            if (lineAlpha > 1.0f) {
                lineAlpha = 1.0f;
            }
        } else {
            lineAlpha -= deltaTime * fadeOutSpeed;
            if (lineAlpha < 0.0f) {
                lineAlpha = 0.0f;
            }
        }
    }

    private void drawCenterLines(MainWindow res) {
        if (lineAlpha > 0.0f) {
            float centerX = res.scaledWidth() / 2.0f;
            float centerY = res.scaledHeight() / 2.0f;
            float centerX2 = res.scaledWidth() / 4.0f;
            float centerY2 = res.scaledHeight() / 4.0f;
            float centerX3 = res.scaledWidth() / 8.0f;
            float centerY3 = res.scaledHeight() / 8.0f;
            float centerX4 = res.scaledWidth() / 1.15f;
            float centerY4 = res.scaledHeight() / 1.15f;
            float centerX5 = res.scaledWidth() / 1.35f;
            float centerY5 = res.scaledHeight() / 1.35f;

            int color = (int) (lineAlpha * 255) << 24 | 0xFFFFFF;
            if (snapToCenterx) {
                RenderUtils.Render2D.drawRoundedRect(centerX - CENTER_LINE_WIDTH / 3.0f, 0, CENTER_LINE_WIDTH, res.scaledHeight(), 1f, color);
            }
            if (snapToCenter) {
                RenderUtils.Render2D.drawRoundedRect(0, centerY - CENTER_LINE_WIDTH / 3.0f, res.scaledWidth(), CENTER_LINE_WIDTH, 1f, color);
            }
            if (snapToCenter2x) {
                RenderUtils.Render2D.drawRoundedRect(centerX2 - CENTER_LINE_WIDTH / 3.0f, 0, CENTER_LINE_WIDTH, res.scaledHeight(), 1f, color);
            }
            if (snapToCenter2) {
                RenderUtils.Render2D.drawRoundedRect(0, centerY2 - CENTER_LINE_WIDTH / 3.0f, res.scaledWidth(), CENTER_LINE_WIDTH, 1f, color);
            }
            if (snapToCenter3x) {
                RenderUtils.Render2D.drawRoundedRect(centerX3 - CENTER_LINE_WIDTH / 3.0f, 0, CENTER_LINE_WIDTH, res.scaledHeight(), 1f, color);
            }
            if (snapToCenter3) {
                RenderUtils.Render2D.drawRoundedRect(0, centerY3 - CENTER_LINE_WIDTH / 3.0f, res.scaledWidth(), CENTER_LINE_WIDTH, 1f, color);
            }
            if (snapToCenter4x) {
                RenderUtils.Render2D.drawRoundedRect(centerX4 - CENTER_LINE_WIDTH / 3.0f, 0, CENTER_LINE_WIDTH, res.scaledHeight(), 1f, color);
            }
            if (snapToCenter4) {
                RenderUtils.Render2D.drawRoundedRect(0, centerY4 - CENTER_LINE_WIDTH / 3.0f, res.scaledWidth(), CENTER_LINE_WIDTH, 1f, color);
            }
            if (snapToCenter5x) {
                RenderUtils.Render2D.drawRoundedRect(centerX5 - CENTER_LINE_WIDTH / 3.0f, 0, CENTER_LINE_WIDTH, res.scaledHeight(), 1f, color);
            }
            if (snapToCenter5) {
                RenderUtils.Render2D.drawRoundedRect(0, centerY5 - CENTER_LINE_WIDTH / 3.0f, res.scaledWidth(), CENTER_LINE_WIDTH, 1f, color);
            }
        }
    }

    public final boolean onClick(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtils.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();
        if (button == 0 && MathUtil.isHovered((float) mouseX, (float) mouseY, xPos, yPos, width, height)) {
            dragging = true;
            startX = (int) (mouseX - xPos);
            startY = (int) (mouseY - yPos);
            lastUpdateTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public final void onRelease(int button) {
        if (button == 0) dragging = false;
    }
}