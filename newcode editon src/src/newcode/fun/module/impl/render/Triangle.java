package newcode.fun.module.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.control.Manager;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.anim.animations.Easing;
import newcode.fun.utils.anim.animations.TimeAnim;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.font.styled.StyledFont;
import newcode.fun.utils.move.MoveUtil;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;

import java.util.Iterator;

@Annotation(name = "Triangle", type = TypeList.Render, desc = "Создаёт стрелочки к игрокам")
public class Triangle extends Module {
    private final Minecraft mc = Minecraft.getInstance();
    public SliderSetting size3 = new SliderSetting("Размер", 17, 15, 20, 1);
    public SliderSetting size2 = new SliderSetting("Радиус", 58, 30, 110, 2);
    public final BooleanOption dinam = new BooleanOption("Динамический", true);
    TimeAnim animationStep = new TimeAnim(Easing.EASE_OUT_EXPO, 850L);
    private float animatedYaw;
    private float animatedPitch;
    public MultiBoxSetting elements = new MultiBoxSetting("Писать",
            new BooleanOption("Дистанция", true),
            new BooleanOption("Ник", true));
    public Triangle() {
        addSettings(elements, size3, size2, dinam);
    }


    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender render) {
            if (render.isRender2D()) {
                render2D(render);
            }
        }
        return false;
    }


    private void render2D(EventRender render) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        float size = size2.getValue().floatValue();
        if (mc.currentScreen instanceof InventoryScreen) {
            size += size2.getValue().floatValue() + 10f;
        }
        if (MoveUtil.isMoving() && dinam.get()) {
            size += 10.0f;
        }
        this.animationStep.run(size);
        Iterator var2 = mc.world.getPlayers().iterator();

        while (true) {
            PlayerEntity player;
            do {
                do {
                    do {
                        if (!var2.hasNext()) {
                            return;
                        }

                        player = (PlayerEntity) var2.next();
                    } while (player == mc.player);
                } while (!player.botEntity);
                Minecraft var10001 = mc;
            } while (player == Minecraft.player);
            double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * (double) mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
            double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * (double) mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
            double cos = MathHelper.cos((float) ((double) mc.getRenderManager().info.getYaw() * (Math.PI / 180)));
            double sin = MathHelper.sin((float) ((double) mc.getRenderManager().info.getYaw() * (Math.PI / 180)));
            double rotY = -(z * cos - x * sin);
            double rotX = -(x * cos + z * sin);
            float angle = (float) (Math.atan2(rotY, rotX) * 180.0 / Math.PI);
            MainWindow mainWindow = mc.getMainWindow();
            double x2 = this.animationStep.getValue() * (double) MathHelper.cos((float) Math.toRadians(angle)) + (double) ((float) mainWindow.getScaledWidth() / 2.0f);
            double y2 = this.animationStep.getValue() * (double) MathHelper.sin((float) Math.toRadians(angle)) + (double) ((float) mainWindow.getScaledHeight() / 2.0f);
            GlStateManager.pushMatrix();
            GlStateManager.disableBlend();
            GlStateManager.translated(x2 += (double) this.animatedYaw, y2 += (double) this.animatedPitch, 0.0);
            GlStateManager.rotatef(angle + 90.0f, 0.0f, 0.0f, 1.0f);
            int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
            int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
            int color = Manager.FRIEND_MANAGER.isFriend(player.getName().getString()) ? ColorUtils.rgba(0, 255, 0, 255) : firstColor2;
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/arrow/arrows.png"), 1.5f - (size3.getValue().floatValue() / 2), 8.0f, size3.getValue().floatValue(), size3.getValue().floatValue(), color);
            int dist = Math.min(99, (int) mc.player.getDistance(player));
            String distText = dist + "m";
            String nameText;

            if (Manager.FUNCTION_MANAGER.nameProtect.state && Manager.FUNCTION_MANAGER.nameProtect.friends.get()) {
                nameText = (Manager.FRIEND_MANAGER.isFriend(player.getName().getString()) ? "newcode" : player.getName().getString());
            } else {
                nameText = player.getName().getString();
            }

            if (nameText.length() > 7) {
                nameText = nameText.substring(0, 7);
            }

            if (elements.get("Ник")) {
                Fonts.newcode[11].drawString(render.matrixStack, nameText, 1.5f - (size3.getValue().floatValue() / 3f) - (Fonts.newcode[11].getWidth(nameText) / 2) + 4, 25.0f, -1);
            }
            if (elements.get("Дистанция")) {
                if (elements.get("Ник")) {
                    Fonts.newcode[11].drawString(render.matrixStack, distText, 1.5f - (size3.getValue().floatValue() / 3f) - (Fonts.newcode[11].getWidth(distText) / 2) + 4, 30.5f, -1);
                } else {
                    Fonts.newcode[11].drawString(render.matrixStack, distText, 1.5f - (size3.getValue().floatValue() / 3f) - (Fonts.newcode[11].getWidth(distText) / 2) + 4, 25.0f, -1);
                }
            }
            GlStateManager.enableBlend();
            GlStateManager.popMatrix();
        }
    }
}