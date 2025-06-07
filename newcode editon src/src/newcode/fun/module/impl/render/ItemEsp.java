package newcode.fun.module.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.utils.render.RenderUtils;
import org.joml.Vector4d;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.math.PlayerPositionTracker;

import java.util.HashMap;
import java.util.Map;

import static newcode.fun.utils.math.PlayerPositionTracker.isInView;
import static newcode.fun.utils.render.ColorUtils.rgba;
import static newcode.fun.utils.render.RenderUtils.Render2D.*;

@Annotation(name = "ItemEsp", type = TypeList.Render)
public class ItemEsp extends Module {

    private static final int MAX_ITEMS = 35; // Максимум отображаемых предметов

    private final MultiBoxSetting elements = new MultiBoxSetting("Элементы",
            new BooleanOption("Боксы", "Boxes", false),
            new BooleanOption("Имена", "Names", true));

    private final Map<Vector4d, ItemEntity> positions = new HashMap<>();

    public ItemEsp() {
        addSettings(elements);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender render) {
            if (render.isRender3D()) {
                updateItemsPositions(render.partialTicks);
            }
            if (render.isRender2D()) {
                renderItemElements(render.matrixStack);
            }
        }
        return false;
    }

    private void updateItemsPositions(float partialTicks) {
        positions.clear();
        int count = 0; // Счётчик предметов

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ItemEntity item && isInView(entity)) {
                Vector4d position = PlayerPositionTracker.updatePlayerPositions(item, partialTicks);
                if (position != null) {
                    positions.put(position, item);
                    count++;

                    // Прерываем цикл, если достигнут максимум
                    if (count >= MAX_ITEMS) {
                        break;
                    }
                }
            }
        }
    }

    private void renderItemElements(MatrixStack stack) {
        GL11.glColor4f(1, 1, 1, 1);

        int boxBackgroundColor = rgba(0, 0, 0, 128);
        Vector4i gradientColors = new Vector4i(-1, -1, -1, -1);

        for (var entry : positions.entrySet()) {
            Vector4d position = entry.getKey();
            ItemEntity item = entry.getValue();

            double x = position.x;
            double y = position.y;
            double endX = position.z;
            double endY = position.w;

            if (elements.get(0)) { // Рисование боксов
                drawRectangles(x, y, endX, endY, boxBackgroundColor);
                drawGradientBorders(x, y, endX, endY, gradientColors, 1.0f);
            }

            if (elements.get(1)) { // Отображение имен
                renderItemName(stack, item, x, y, endX);
            }
        }
    }

    private void drawRectangles(double x, double y, double endX, double endY, int color) {
        drawMcRect(x - 0.5F, y - 0.5F, endX + 1, y + 1, color);
        drawMcRect(x - 0.5F, endY - 0.5F, endX + 1, endY + 1, color);
        drawMcRect(x - 0.5F, y + 1, x + 1, endY - 0.5F, color);
        drawMcRect(endX - 0.5F, y + 1, endX + 1, endY - 0.5F, color);
    }

    private void drawGradientBorders(double x, double y, double endX, double endY, Vector4i colors, float size) {
        drawMCHorizontal(x, y, endX, y + 0.5F * size, colors.y, colors.x);
        drawMCHorizontal(x, endY, endX, endY + 0.5F * size, colors.w, colors.z);
        drawMCVertical(x, y, x + 0.5F * size, endY + 0.5F * size, colors.w, colors.y);
        drawMCVertical(endX, y, endX + 0.5F * size, endY + 0.5F * size, colors.z, colors.x);
    }

    private void renderItemName(MatrixStack stack, ItemEntity item, double x, double y, double endX) {
        String tag = item.getItem().getDisplayName().getString() +
                (item.getItem().getCount() > 1 ? TextFormatting.RED + " x" + item.getItem().getCount() : "");

        float tagWidth = Fonts.newcode[10].getWidth(tag);
        float posX = (float) (x + ((endX - x) / 2) - tagWidth / 2);

        int nameBackgroundColor = rgba(33, 33, 33, 128);
        RenderUtils.Render2D.drawRoundedRect(posX - 3, (float) y - 9.6f, tagWidth + 6, 8, 0F, nameBackgroundColor);
        Fonts.newcode[10].drawStringWithShadow(stack, tag, posX, (float) y - 5, -1);
    }
}
