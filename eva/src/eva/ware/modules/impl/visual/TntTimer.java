package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventRender2D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;
import eva.ware.utils.math.MathUtility;

import eva.ware.utils.render.engine2d.ProjectionUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.text.font.ClientFonts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;

@ModuleRegister(name = "TntTimer", category = Category.Visual)
public class TntTimer extends Module {

    private Map<String, CompactAnimation> animations = new HashMap<>();

    @Subscribe
    public void onDisplay(EventRender2D e) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof TNTEntity tnt) {
                final String name = MathUtility.round(tnt.getFuse() / 20.0F, 1) + " сек";
                Vector3d pos = ProjectionUtility.interpolate(tnt, e.getPartialTicks());
                Vector2f vec = ProjectionUtility.project(pos.x, pos.y + tnt.getHeight() + 0.5, pos.z);
                if (vec == null) return;

                float iconSize = 10;
                float width = ClientFonts.interBold[16].getWidth(name) + 4 + iconSize;
                float height = ClientFonts.interBold[16].getFontHeight();

                CompactAnimation easing = animations.getOrDefault(tnt.getDisplayName().getString(), null);
                if (easing == null) {
                    easing = new CompactAnimation(Easing.EASE_IN_OUT_CUBIC, 250);
                    animations.put(tnt.getDisplayName().getString(), easing);
                }

                boolean tntActive = tnt.getFuse() > 10;
                easing.run(tntActive ? 1 : 0);

                float x = (float) vec.x;
                float y = (float) vec.y;

                int black = ColorUtility.setAlpha(ColorUtility.rgb(10, 10, 10), (int) (140 * easing.getValue()));
                RenderUtility.drawImage(new ResourceLocation("eva/images/modules/timers/tnt.png"), (x - width / 2), y, iconSize , iconSize, ColorUtility.setAlpha(-1, (int) (255 * easing.getValue())));
                RenderUtility.drawRoundedRect((x - width / 2 - 2), (float) y - 2, (float) (width) + 4, (float) (height) + 4, 2, black);
                ClientFonts.interBold[16].drawCenteredString(e.getMatrixStack(), name, (x - width / 2 + iconSize * 2 + 8), y + 2.5f, ColorUtility.setAlpha(-1, (int) (255 * easing.getValue())));
            }
        }
    }

}
