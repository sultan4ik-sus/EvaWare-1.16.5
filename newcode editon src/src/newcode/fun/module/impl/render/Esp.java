package newcode.fun.module.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.impl.player.EventModelRender;
import newcode.fun.module.api.Module;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.render.*;
import org.joml.Vector4d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.math.PlayerPositionTracker;
import newcode.fun.utils.render.animation.AnimationMath;

import java.util.*;

@Annotation(name = "Esp", type = TypeList.Render, desc = "Отображает визуальные элементы к игрокам")
public class Esp extends Module {
    public MultiBoxSetting elements = new MultiBoxSetting("Отображать",
            new BooleanOption("Боксы", true),
            new BooleanOption("Здоровье", true),
            new BooleanOption("Свечение", true));
    public SliderSetting size = new SliderSetting("Размер", 0.9f, 0.01f, 10f, 0.01f);
    public SliderSetting sizeglow = new SliderSetting("Размер свечение", 6f, 3f, 10f, 1f).setVisible(() -> elements.get(2));
    public SliderSetting powerglow = new SliderSetting("Сила свечение", 2f, 1f, 3f, 0.5f).setVisible(() -> elements.get(2));

    public Esp() {
        addSettings(elements, sizeglow, powerglow);
    }
    public static Framebuffer framebuffer = new Framebuffer(1, 1, true, false);

    public Object2ObjectOpenHashMap<Vector4d, PlayerEntity> positions = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean onEvent(Event event) {

        if (event instanceof EventModelRender e) {
            framebuffer.bindFramebuffer(false);
            e.render();
            framebuffer.unbindFramebuffer();
            mc.getFramebuffer().bindFramebuffer(true);

        }
        if (event instanceof EventRender render) {

            if (render.isRender3D()) {
                updatePlayerPositions(render.partialTicks);
            }

            if (render.isRender2D()) {
                renderPlayerElements(render.matrixStack);
            }
            if (event instanceof EventRender e) {
                if (e.isRender2D() && elements.get(2)) {
                    GlStateManager.enableBlend();
                    OutlineUtils.registerRenderCall(() -> {
                        framebuffer.bindFramebufferTexture();
                        ShaderUtils.drawQuads();
                    });
                    BloomHelper.registerRenderCallHand(() -> {
                        framebuffer.bindFramebufferTexture();
                        ShaderUtils.drawQuads();
                    });
                    OutlineUtils.draw(1.3f, ColorUtils.getColorStyle(0.0F));
                    BloomHelper.drawC(sizeglow.getValue().intValue(), powerglow.getValue().floatValue(), true, ColorUtils.getColorStyle(0.0F), 1.0F);
                    OutlineUtils.setupBuffer(framebuffer);
                    mc.getFramebuffer().bindFramebuffer(true);
                }
                if (e.isRender3D()) {
                    updatePlayerPositions(e.partialTicks);
                }

                if (e.isRender2D()) {
                    renderPlayerElements(e.matrixStack);
                }
            }
        }
        return false;
    }

    private void updatePlayerPositions(float partialTicks) {
        this.positions.clear();
        Iterator var2 = mc.world.getPlayers().iterator();

        while(true) {
            PlayerEntity player;
            do {
                do {
                    do {
                        if (!var2.hasNext()) {
                            return;
                        }

                        player = (PlayerEntity)var2.next();
                    } while(!PlayerPositionTracker.isInView(player));
                } while(!player.botEntity);

                if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
                    break;
                }

                Minecraft var10001 = mc;
            } while(player == Minecraft.player);

            Vector4d position = PlayerPositionTracker.updatePlayerPositions(player, partialTicks);
            if (position != null) {
                this.positions.put(position, player);
            }
        }
    }

    private void renderPlayerElements(MatrixStack stack) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        IMinecraft.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        Iterator var3 = this.positions.entrySet().iterator();
        for (Map.Entry<Vector4d, PlayerEntity> entry : positions.entrySet()) {
            Vector4d position = entry.getKey();
            PlayerEntity player = entry.getValue();
            if (ClientUtils.isConnectedToServer("reallyworld") || ClientUtils.isConnectedToServer("funtime") || ClientUtils.isConnectedToServer("legendsgrief")) {
                if (player != null) {
                    for (Map.Entry<ScoreObjective, Score> entrys : mc.world.getScoreboard().getObjectivesForEntity(player.getName().getString()).entrySet()) {
                        ScoreObjective objective = entrys.getKey();
                        Score score = entrys.getValue();

                        int newHealth = score.getScorePoints();

                        if (newHealth >= 1) {
                            player.setHealth(newHealth);
                        } else {
                            player.setHealth(1);
                        }
                    }
                }
            }
            int colorsbox2 = Manager.FRIEND_MANAGER.isFriend(player.getName().getString())
                    ? ColorUtils.rgba(0, 255, 0, 255)
                    : ColorUtils.rgba(255, 255, 255, 255
            );
            int colors2 = new java.awt.Color(5, 190, 36, 255).getRGB();
            int colors1 = new java.awt.Color(171, 5, 30, 255).getRGB();
            int colors3 = new java.awt.Color(47, 47, 47, 179).getRGB();
            if (elements.get(0)) {
                renderBoxCorners(position.x, position.y, position.z, position.w, colorsbox2);
            }
            if (elements.get(1)) {
                float height = (float) (position.w - position.y);
                player.animationPerc = AnimationMath.fast(player.animationPerc, MathHelper.clamp((player.getHealth() / player.getMaxHealth()), 0, 1), 15);
                RenderUtils.Render2D.drawVerticalBuilding(position.x - 1 - size.getValue().floatValue(), position.y, size.getValue().floatValue(), (double)(height), colors3, colors3);
                RenderUtils.Render2D.drawVerticalBuilding(position.x - 1 - size.getValue().floatValue(), position.y + (height * (1 - player.animationPerc)), size.getValue().floatValue(), height - height * (1 - player.animationPerc) - 0.7f, colors1, colors2);
            }
        }
        IMinecraft.tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    private void renderBoxCorners(double x, double y, double endX, double endY, int colors) {
        float size = MathHelper.clamp(this.size.getValue().floatValue(), 1, 2);
        float alpha = 0.5f;
        RenderUtils.Render2D.drawRectOutlineBuildingGradient(x, y, endX - 0.3f, endY - 0.9f, size - 0.3f, colors & 0x00FFFFFF | ((int)(alpha * 255) << 24)); // Set alpha component of the color
    }
}

