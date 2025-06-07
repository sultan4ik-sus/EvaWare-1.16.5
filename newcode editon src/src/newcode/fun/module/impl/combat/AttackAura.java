package newcode.fun.module.impl.combat;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.anim.Animation;
import newcode.fun.utils.anim.Direction;
import newcode.fun.utils.anim.impl.DecelerateAnimation;
import newcode.fun.utils.math.*;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventInput;
import newcode.fun.control.events.impl.player.EventInteractEntity;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.move.MoveUtil;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.ProjectionUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.world.InventoryUtils;
import org.joml.Vector2d;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static java.lang.Math.*;
import static net.minecraft.client.Minecraft.player;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;
import static newcode.fun.utils.math.MathUtil.calculateDelta;
import static org.joml.Math.lerp;

@SuppressWarnings("all")
@Annotation(name = "AttackAura", type = TypeList.Combat, desc = "Бьёт женщин и детей, берцами")
public class AttackAura extends Module {
    @Getter
    public static LivingEntity target = null;
    private Vector2f rotateVector = new Vector2f(0, 0);
    public Vector2f rotate = new Vector2f(0, 0);
    private long lastHitTime = 0;
    private Vector3f fakePrevRotateVector;
    private Vector3f fakeRotateVector;
    private static final long HIT_COLOR_DURATION = 350;
    public final ModeSetting rotationMode = new ModeSetting("Выбор режимов", "Snaped", "Smotion", "Snaped");
    private final ModeSetting sortMode = new ModeSetting("Сортировать",
            "Продвинутая",
            "Продвинутая", "Приоритет"
    );
    private final TimerUtil timerUtil = new TimerUtil();

    private ResourceLocation texture;
    private static final int MAX_POINTS = 180;
    private final MultiBoxSetting targets = new MultiBoxSetting("Выбор целей",
            new BooleanOption("Игроки", true),
            new BooleanOption("Друзья", false),
            new BooleanOption("Голые Игроки", true),
            new BooleanOption("Монстры", false)
    );
    private final TimerUtil stopWatch = new TimerUtil();
    private long lastTime = System.currentTimeMillis();

    private final Animation alpha = new DecelerateAnimation(600, 255);
    public static final long detime = System.currentTimeMillis();
    private LivingEntity currentTarget;
    public static SliderSetting distance = new SliderSetting("Дистанция аттаки", 3.0f, 2.5f, 6f, 0.1f);
    private final SliderSetting speedrot = new SliderSetting("Скорость наведение", 0.45f, 0.3f, 1f, 0.025f).setVisible(() -> !rotationMode.is("Smotion"));
    private final SliderSetting fov2 = new SliderSetting("Fov аттаки", 360f, 30f, 360f, 10f).setVisible(() -> !rotationMode.is("Smotion"));
    private final SliderSetting rotateDistance = new SliderSetting("Дистанция ротации", 0.0f, 0.0f, 3.0f, 0.1f);
    private final SliderSetting elytrarotate = (new SliderSetting("Элитра ротация", 12.5F, 0.0F, 32.0F, 0.5F));
    private final SliderSetting elytradist = (new SliderSetting("Элитра дистанция", 0.7F, 0.0F, 0.7F, 0.05F));
    private final ModeSetting corection = new ModeSetting("Режим коррекции", "Свободный", "Свободный", "Сфокусированный");
    boolean isRotated;
    private final long startTime = System.currentTimeMillis();

    public final MultiBoxSetting settings = new MultiBoxSetting("Настройки",
            new BooleanOption("Только критами", true),
            new BooleanOption("Коррекция движения", true),
            new BooleanOption("Отжимать щит", true),
            new BooleanOption("Ломать щит", true),
            new BooleanOption("Только с пробелом", false),
            new BooleanOption("Бить через стены", true),
            new BooleanOption("Не бить если ешь", true),
            new BooleanOption("Откл интерполяцию", false),
            new BooleanOption("Дистанция под Grim", true));
    float ticksUntilNextAttack;
    private boolean hasRotated;
    float rotYaw;
    float rotPitch;
    int ticks;
    private long cpsLimit = 0;
    public float getRotYaw() {
        return this.rotYaw;
    }
    private boolean isActiveHandEngaged = false;

    public float getRotPitch() {
        return this.rotPitch;
    }
    public AttackAura() {
        this.addSettings(targets, rotationMode, sortMode, distance, speedrot, fov2, rotateDistance, corection, elytrarotate, elytradist, settings);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (event instanceof EventPacket e) {
            onPacket(e);
        }
        if (event instanceof EventRender e) {
            handleRenderEvent(e);
        }
        if (event instanceof EventPacket) {
            EventPacket e = (EventPacket) event;
            if (e.getPacket() instanceof CUseEntityPacket) {
                CUseEntityPacket use = (CUseEntityPacket) e.getPacket();
                if (use.getAction() == CUseEntityPacket.Action.ATTACK) {
                    Entity entity = use.getEntityFromWorld(mc.world);
                    if (mc.world != null && entity != null) {
                        lastHitTime = System.currentTimeMillis();
                    }
                }
            }
        }
        if (Manager.FUNCTION_MANAGER.targetEsp.state) {
            switch (Manager.FUNCTION_MANAGER.targetEsp.mode.getIndex()) {
                case 0 -> {
                    if (event instanceof EventUpdate) {
                        if (AttackAura.target != null) {
                            currentTarget = AttackAura.target;
                        }
                        alpha.setDirection(!state || AttackAura.target == null ? Direction.BACKWARDS : Direction.FORWARDS);
                    }
                    double sin;
                    sin = Math.sin((double) System.currentTimeMillis() / 1000.0);

                    if (event instanceof EventRender) {
                        EventRender render = (EventRender) event;
                        if (alpha.finished(Direction.BACKWARDS)) {
                            return false;
                        }
                        Vector3d interpolated;

                        if (currentTarget != null && currentTarget != player) {
                            double x = currentTarget.lastTickPosX + (currentTarget.getPosX() - currentTarget.lastTickPosX) * render.partialTicks;
                            double y = currentTarget.lastTickPosY + (currentTarget.getPosY() - currentTarget.lastTickPosY) * render.partialTicks;
                            double z = currentTarget.lastTickPosZ + (currentTarget.getPosZ() - currentTarget.lastTickPosZ) * render.partialTicks;
                            Vector2d pos = ProjectionUtils.project(x, y + 1, z);
                            int color = ColorUtils.setAlpha(ColorUtils.getColorStyle(0), (int) alpha.getOutput());
                            long currentTime = System.currentTimeMillis();

                            if (Manager.FUNCTION_MANAGER.targetEsp.attack.get() && currentTime - lastHitTime < HIT_COLOR_DURATION) {
                                color = ColorUtils.setAlpha(new Color(198, 24, 24, 255).getRGB(), (int) alpha.getOutput());
                            }

                            int color2 = ColorUtils.setAlpha(ColorUtils.getColorStyle(0), (int) alpha.getOutput());

                            double distance = player.getDistance(currentTarget);
                            interpolated = this.currentTarget.getPositon(render.getPartialTicks());

                            float size = (float) this.getScale(interpolated, Manager.FUNCTION_MANAGER.targetEsp.size.getValue().floatValue() / 7);
                            float size2 = (Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON ? max(25, Manager.FUNCTION_MANAGER.targetEsp.size.getValue().intValue() + 40 - 15 - (float) (distance - 0.2f) * 11f) : max(Manager.FUNCTION_MANAGER.targetEsp.size.getValue().intValue() + 40 - 50, Manager.FUNCTION_MANAGER.targetEsp.size.getValue().intValue() + 40 - 35 - (float) (distance - 0.2f) * 7f) / 1.4F);

                            int alpha;
                            alpha = (int) this.alpha.getOutput();

                            if (pos != null) {
                                GlStateManager.pushMatrix();
                                GlStateManager.translatef((float) pos.x, (float) pos.y, 0.0F);
                                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                                GlStateManager.translatef((float) -pos.x, (float) -pos.y, 0.0F);
                                GlStateManager.enableBlend();
                                GlStateManager.blendFunc(770, 1);
                                RenderUtils.Render2D.drawImageAlpha(new ResourceLocation("newcode/images/all/targetesp/target.png"), (float) (pos.x - size / 2.0F), (float) (pos.y - size / 2.0F), size, size, new Vector4i(ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha)));
                                GlStateManager.disableBlend();
                                GlStateManager.popMatrix();

                            }
                        }
                    }
                }
                case 1 -> {
                    if (AttackAura.target != null) {

                        if (event instanceof EventUpdate) {
                            if (AttackAura.target != null) {
                                currentTarget = AttackAura.target;
                            }
                        }

                        if (event instanceof EventRender e) {
                            if (currentTarget != null && currentTarget != player) {
                                MatrixStack ms = new MatrixStack();
                                ms.push();
                                RenderSystem.pushMatrix();
                                RenderSystem.disableLighting();
                                depthMask(false);
                                RenderSystem.enableBlend();
                                RenderSystem.shadeModel(7425);
                                RenderSystem.disableCull();
                                RenderSystem.disableAlphaTest();
                                RenderSystem.blendFuncSeparate(770, 1, 0, 1);
                                double x = currentTarget.lastTickPosX + (currentTarget.getPosX() - currentTarget.lastTickPosX) * e.partialTicks;
                                double y = currentTarget.lastTickPosY + (currentTarget.getPosY() - currentTarget.lastTickPosY) * e.partialTicks + currentTarget.getHeight() / 2f;
                                double z = currentTarget.lastTickPosZ + (currentTarget.getPosZ() - currentTarget.lastTickPosZ) * e.partialTicks;

                                double radius = 0.67;
                                float speed = 45;
                                float size = 0.4f;
                                double distance = 19;
                                int length = 20;
                                int maxAlpha = 255;
                                int alphaFactor = 15;
                                ActiveRenderInfo camera = mc.getRenderManager().info;
                                ms.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());

                                Vector3d interpolated = MathUtil.interpolate2(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.partialTicks);

                                interpolated.y += 0.75f;
                                ms.translate(interpolated.x + 0.2f, interpolated.y + 0.5f, interpolated.z);
                                mc.getTextureManager().bindTexture(new ResourceLocation("newcode/images/all/targetesp/glow.png"));
                                for (int i = 0; i < length; i++) {
                                    Quaternion r = camera.getRotation().copy();
                                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                                    double s = Math.sin(angle) * radius;
                                    double c = Math.cos(angle) * radius;
                                    ms.translate(s, (c), -c);
                                    ms.translate(-size / 2f, -size / 2f, 0);
                                    ms.rotate(r);
                                    ms.translate(size / 2f, size / 2f, 0);
                                    int color = ColorUtils.setAlpha(ColorUtils.getColorStyle(0), (int) alpha.getOutput());
                                    long currentTime = System.currentTimeMillis();
                                    if (Manager.FUNCTION_MANAGER.targetEsp.attack.get() && currentTime - lastHitTime < HIT_COLOR_DURATION) {
                                        color = ColorUtils.setAlpha(new Color(198, 24, 24, 255).getRGB(), (int) alpha.getOutput());
                                    }
                                    int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                                    tessellator.draw();
                                    ms.translate(-size / 2f, -size / 2f, 0);
                                    r.conjugate();
                                    ms.rotate(r);
                                    ms.translate(size / 2f, size / 2f, 0);
                                    ms.translate(-(s), -(c), (c));
                                }
                                for (int i = 0; i < length; i++) {
                                    Quaternion r = camera.getRotation().copy();
                                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                                    double s = Math.sin(angle) * radius;
                                    double c = Math.cos(angle) * radius;
                                    ms.translate(-s, s, -c);
                                    ms.translate(-size / 2f, -size / 2f, 0);
                                    ms.rotate(r);
                                    ms.translate(size / 2f, size / 2f, 0);
                                    int color = ColorUtils.setAlpha(ColorUtils.getColorStyle(0), (int) alpha.getOutput());
                                    long currentTime = System.currentTimeMillis();
                                    if (Manager.FUNCTION_MANAGER.targetEsp.attack.get() && currentTime - lastHitTime < HIT_COLOR_DURATION) {
                                        color = ColorUtils.setAlpha(new Color(198, 24, 24, 255).getRGB(), (int) alpha.getOutput());
                                    }
                                    int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                                    tessellator.draw();
                                    ms.translate(-size / 2f, -size / 2f, 0);
                                    r.conjugate();
                                    ms.rotate(r);
                                    ms.translate(size / 2f, size / 2f, 0);
                                    ms.translate((s), -(s), (c));
                                }
                                for (int i = 0; i < length; i++) {
                                    Quaternion r = camera.getRotation().copy();
                                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                                    double s = Math.sin(angle) * radius;
                                    double c = Math.cos(angle) * radius;
                                    ms.translate(-(s), -(s), (c));
                                    ms.translate(-size / 2f, -size / 2f, 0);
                                    ms.rotate(r);
                                    ms.translate(size / 2f, size / 2f, 0);
                                    int color = ColorUtils.setAlpha(ColorUtils.getColorStyle(0), (int) alpha.getOutput());
                                    long currentTime = System.currentTimeMillis();
                                    if (Manager.FUNCTION_MANAGER.targetEsp.attack.get() && currentTime - lastHitTime < HIT_COLOR_DURATION) {
                                        color = ColorUtils.setAlpha(new Color(198, 24, 24, 255).getRGB(), (int) alpha.getOutput());
                                    }
                                    int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);

                                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                                    tessellator.draw();
                                    ms.translate(-size / 2f, -size / 2f, 0);
                                    r.conjugate();
                                    ms.rotate(r);
                                    ms.translate(size / 2f, size / 2f, 0);
                                    ms.translate((s), (s), -(c));
                                }
                                RenderSystem.defaultBlendFunc();
                                RenderSystem.disableBlend();
                                RenderSystem.enableCull();
                                RenderSystem.enableAlphaTest();
                                depthMask(true);
                                RenderSystem.popMatrix();
                                ms.pop();
                            }
                        }
                    }
                }
                case 2 -> {
                    if (event instanceof EventRender e) {
                        if (e.isRender3D() && target != null) {
                            drawCircle(target, e);
                        }
                    }
                }
            }
        }
        if (event instanceof EventInteractEntity entity) {
            if (target != null)
                entity.setCancel(true);
        }
        if (event instanceof EventInput eventInput) {
            if (settings.get(1)) {
                switch (corection.getIndex()) {
                    case 0 -> {
                        if (Manager.FUNCTION_MANAGER.freeCam.player == null) {
                            MoveUtil.fixMovement(eventInput, Manager.FUNCTION_MANAGER.autoPotionFunction.isActivePotion ? Minecraft.getInstance().player.rotationYaw : rotate.x);
                        }
                    }
                    case 1 -> {
                    }
                }
            }
        }

        if (event instanceof EventUpdate updateEvent) {
            if (!(target != null && isValidTarget(target))) {
                target = findTarget();
            }
            if (target == null) {
                cpsLimit = System.currentTimeMillis();
                rotate = new Vector2f(player.rotationYaw, player.rotationPitch);
                return false;
            }
            attackAndRotateOnEntity(target);
        }
        if (event instanceof EventMotion motionEvent) {
            handleMotionEvent(motionEvent);
        }
        return false;
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);
        return Math.max(10.0, 1000.0 / distance) * (size / 30.0) / (fov == 70.0 ? 1.0 : fov / 70.0);
    }

    public Vector2f clientRot = null;

    private void handleMotionEvent(EventMotion motionEvent) {
        if (target == null || Manager.FUNCTION_MANAGER.autoPotionFunction.isActivePotion)
            return;

        motionEvent.setYaw(rotate.x);
        motionEvent.setPitch(rotate.y);
        mc.player.rotationYawHead = rotate.x;
        mc.player.renderYawOffset = rotate.x;
        mc.player.rotationPitchHead = rotate.y;
    }

    private double prevCircleStep, circleStep;

    private void drawCircle(LivingEntity target, EventRender e) {
        EntityRendererManager rm = mc.getRenderManager();

        double x = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * e.partialTicks - rm.info.getProjectedView().getX();
        double y = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * e.partialTicks - rm.info.getProjectedView().getY();
        double z = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * e.partialTicks - rm.info.getProjectedView().getZ();

        float height = target.getHeight();

        double duration = 2000;
        double elapsed = (System.currentTimeMillis() % duration);

        boolean side = elapsed > (duration / 2);
        double progress = elapsed / (duration / 2);

        if (side) progress -= 1;
        else progress = 1 - progress;

        progress = (progress < 0.5) ? 2 * progress * progress : 1 - pow((-2 * progress + 2), 2) / 2;

        double eased = (height / 2) * ((progress > 0.5) ? 1 - progress : progress) * ((side) ? -1 : 1);

        RenderSystem.pushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableCull();

        RenderSystem.lineWidth(1.5f);
        RenderSystem.color4f(-1f, -1f, -1f, -1f);

        buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);

        float[] colors = null;

        for (int i = 0; i <= 360; i++) {
            colors = RenderUtils.IntColor.rgb(Manager.STYLE_MANAGER.getCurrentStyle().getColor(i));

            buffer.pos(x + cos(toRadians(i)) * target.getWidth() * 0.8, y + (height * progress), z + sin(toRadians(i)) * target.getWidth() * 0.8)
                    .color(colors[0], colors[1], colors[2], 0.5F).endVertex();
            buffer.pos(x + cos(toRadians(i)) * target.getWidth() * 0.8, y + (height * progress) + eased, z + sin(toRadians(i)) * target.getWidth() * 0.8)
                    .color(colors[0], colors[1], colors[2], 0F).endVertex();
        }

        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);
        RenderSystem.color4f(-1f, -1f, -1f, -1f);

        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i <= 360; i++) {
            buffer.pos(x + cos(toRadians(i)) * target.getWidth() * 0.8, y + (height * progress), z + sin(toRadians(i)) * target.getWidth() * 0.8)
                    .color(colors[0], colors[1], colors[2], 0.5F).endVertex();
        }

        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.popMatrix();
    }

    float lastYaw, lastPitch;
    private void attackAndRotateOnEntity(LivingEntity target) {
        Vector3d vec;
        if (Manager.FUNCTION_MANAGER.elytraVector.mode.is("Движению")) {
            Vector3d targetMotion = target.getMotion();

            Vector3d normalizedMotion = targetMotion.normalize();

            Vector3d shiftVec = normalizedMotion.multiply(Manager.FUNCTION_MANAGER.elytraVector.elytradistance.getValue().floatValue() / 19);

            if (mc.player.isElytraFlying() && (target.isElytraFlying() || target.isForcedDown()) && Manager.FUNCTION_MANAGER.elytraVector.state) {
                vec = target.getPositionVec().add(0.0, MathHelper.clamp(target.getPosY() - target.getHeight(), 0.0, target.getHeight() / 2.0F), 0.0).add(shiftVec).subtract(mc.player.getEyePosition(1.0F));
            } else {
                vec = target.getPositionVec().add(0.0, target.getHeight() - 0.2f, 0.0).subtract(mc.player.getEyePosition(1.0F));
            }
        } else {
            vec = target.getPositionVec()
                    .add(0.0,
                            mc.player.isElytraFlying() && (target.isElytraFlying() || target.isForcedDown()) && Manager.FUNCTION_MANAGER.elytraVector.state
                                    ? MathHelper.clamp(target.getPosY() - target.getHeight(), 0.0, target.getHeight() / 2.0F)
                                    : target.getHeight() - 0.2f,
                            0.0)
                    .subtract(mc.player.getEyePosition(1.0F));

            if (mc.player.isElytraFlying() && (target.isElytraFlying() || target.isForcedDown()) && Manager.FUNCTION_MANAGER.elytraVector.state) {
                double factor = Manager.FUNCTION_MANAGER.elytraVector.elytradistance.getValue().floatValue() / 20.0F;

                Vector3d predictedMotion = target.getMotion().mul(factor, factor, factor);

                Vector3d playerAdjustment = mc.player.getMotion().mul(0.1, 0.1, 0.1);

                vec = vec.add(predictedMotion).add(playerAdjustment);
            }
        }

        this.isRotated = true;
        float yawToTarget = (float) wrapDegrees(toDegrees(atan2(vec.z, vec.x)) - 90.0);
        float pitchToTarget = (float)(-toDegrees(atan2(vec.y, hypot(vec.x, vec.z))));

        float yawDelta = wrapDegrees(yawToTarget - this.rotate.x);
        float pitchDelta = wrapDegrees(pitchToTarget - this.rotate.y);

        int roundedYaw = (int) yawDelta;
        boolean elytraFly = false;
        float rotationYawSpeed = 150;
        float rotationPitchSpeed = 150;
        float clampedYaw = min(max(abs(yawDelta), 0.0f), rotationYawSpeed);
        float clampedPitch = max(abs(pitchDelta), 0.0f);
        float yaw;
        float pitch;
        float gcd = SensUtil.getGCDValue();
        float fov = fov2.getValue().floatValue(); 

        if (!settings.get(7)) {
            vec = vec.add(
                    (target.getPosX() - target.lastTickPosX),
                    (target.getPosY() - target.lastTickPosY),
                    (target.getPosZ() - target.lastTickPosZ)
            );
        }
        switch (rotationMode.getIndex()) {
            case 1 -> {
                if (this.shouldAttack(target) && !Manager.FUNCTION_MANAGER.autoPotionFunction.isActivePotion && isWithinFOV(fov)) {
                    if (settings.get(6) && mc.player.isHandActive() && !mc.player.isBlocking()) return;

                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            attackEntityAndSwing(target);
                        }
                    }, 1);
                    ticksUntilNextAttack = 2f;
                }

                if (this.ticksUntilNextAttack > 0) {
                    this.setRotation(target, false);
                    --this.ticksUntilNextAttack;
                } else {
                    float smoothFactor = speedrot.getValue().floatValue();
                    rotate.x += (mc.player.rotationYaw - rotate.x) * smoothFactor;
                    rotate.y += (mc.player.rotationPitch - rotate.y) * smoothFactor;
                }
            }
            case 0 -> {
                if (this.shouldAttack(target) && !Manager.FUNCTION_MANAGER.autoPotionFunction.isActivePotion) {
                    if (settings.get(6) && mc.player.isHandActive() && !mc.player.isBlocking()) return;
                    attackEntityAndSwing(target);
                }
                if (mc.player.isElytraFlying() && (target.isElytraFlying() || target.isForcedDown()) && Manager.FUNCTION_MANAGER.elytraVector.state) {
                    double futureX = target.getPosX() + target.motion.x * 1.5;
                    double futureY = target.getPosY() + target.motion.y * 1.5;
                    double futureZ = target.getPosZ() + target.motion.z * 1.5;

                    yaw = Math.min(Math.max(Math.abs(yawDelta), 1.0F), rotationYawSpeed * 1.2F); // Усиливаем yaw
                    pitch = Math.max(Math.abs(pitchDelta), 1.0F);

                    float sensitivityFix = SensUtil.getSensitivity((float) (Math.cos(System.currentTimeMillis() / 50.0) * Manager.FUNCTION_MANAGER.elytraVector.traska.getValue().floatValue()));

                    gcd = rotate.x + (yawDelta > 0.0F ? yaw : -yaw) + sensitivityFix;
                    pitch = MathHelper.clamp(rotate.y + (pitchDelta > 0.0F ? pitch : -pitch), -89.0F, 89.0F) + sensitivityFix;

                    float gcdValue = SensUtil.getGCDValue() * 0.5F;
                    gcd -= (gcd - rotate.x) % gcdValue;
                    pitch -= (pitch - rotate.y) % gcdValue;

                    rotate = new Vector2f(gcd, pitch);

                    this.lastYaw = yaw;
                    this.lastPitch = pitch;
            } else {
                    yaw = rotate.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                    pitch = clamp(rotate.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);
                    yaw -= (yaw - rotate.x) % gcd;
                    pitch -= (pitch - rotate.y) % gcd;
                    rotate = new Vector2f(yaw, pitch);
                    this.lastYaw = clampedYaw;
                    this.lastPitch = clampedPitch;
                }
            }
        }
    }

    private boolean isWithinFOV(float fov) {
        // Player's look direction (view vector)
        float yaw = mc.player.rotationYaw;
        float pitch = mc.player.rotationPitch;

        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);

        // Direction vector based on yaw and pitch
        double x = -Math.sin(radYaw) * Math.cos(radPitch);
        double y = -Math.sin(radPitch);
        double z = Math.cos(radYaw) * Math.cos(radPitch);
        Vector3d playerLookDirection = new Vector3d(x, y, z);

        // Target direction (from player to target)
        Vector3d targetPosition = new Vector3d(target.getPosX(), target.getPosY(), target.getPosZ());
        Vector3d playerPosition = new Vector3d(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        Vector3d targetDirection = targetPosition.subtract(playerPosition).normalize();

        // Calculate the angle between the two vectors using dot product
        double dotProduct = playerLookDirection.dotProduct(targetDirection);
        double angle = Math.acos(dotProduct); // in radians
        double angleInDegrees = Math.toDegrees(angle);

        // If the angle is smaller than half the FOV, target is within the FOV
        return angleInDegrees <= fov / 2;
    }


    private void findTarget(CUseEntityPacket packet) {
        if (mc.world != null && player != null) {
            if (packet.getEntityFromWorld(mc.world) instanceof PlayerEntity player && player != target) {
                target = player;
            }
        }
    }
    private void setRotation(final LivingEntity base, final boolean attack) {
        this.hasRotated = true;

        Vector3d vec3d = AuraUtil.getVector(base);

        final double diffX = vec3d.x;
        final double diffY = vec3d.y;
        final double diffZ = vec3d.z;

        float[] rotations = new float[]{
                (float) toDegrees(atan2(diffZ, diffX)) - 90.0F,
                (float) (-toDegrees(atan2(diffY, hypot(diffX, diffZ))))
        };

        float deltaYaw = wrapDegrees(calculateDelta(rotations[0], this.rotate.x));
        float deltaPitch = calculateDelta(rotations[1], this.rotate.y);

        float limitedYaw = min(max(abs(deltaYaw), 0.5F), 200.0F);
        float limitedPitch = (float) min(max(abs(deltaPitch), 0.5F), 200.0F);

        float finalYaw = this.rotate.x + (deltaYaw > 0.0f ? limitedYaw : -limitedYaw) + ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f);
        float finalPitch = clamp(this.rotate.y + (deltaPitch > 0.0f ? limitedPitch : -limitedPitch) + ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), -89.0f, 89.0f);

        float gcd = GCDUtil.getGCDValue();
        finalYaw = (float) ((double) finalYaw - (double) (finalYaw - this.rotate.x) % gcd);
        finalPitch = (float) ((double) finalPitch - (double) (finalPitch - rotate.y) % gcd);

        this.rotate.x = finalYaw;
        this.rotate.y = finalPitch;
    }
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CUseEntityPacket packet) {
            findTarget(packet);
        }
    }

    private void handleRenderEvent(EventRender e) {
        if (e.isRender3D() && target != player && isElytra() && Manager.FUNCTION_MANAGER.elytraVector.state && Manager.FUNCTION_MANAGER.elytraVector.draw.get()) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            {
                Vector3d lookVec = target.getLookVec();
                Vector3d offset = lookVec.scale(Manager.FUNCTION_MANAGER.elytraVector.elytradistance.getValue().floatValue() / 20.0F);
                if (target.isElytraFlying()) {
                    RenderUtils.Render3D.drawBoxVectorElytra(target.getBoundingBox().offset(-mc.getRenderManager().info.getProjectedView().x + offset.x, -mc.getRenderManager().info.getProjectedView().y + offset.y, -mc.getRenderManager().info.getProjectedView().z + offset.z), -1);
                }
            }
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    private boolean isElytra() {
        if (target != null) {
            ItemStack chestItem = player.inventory.armorItemInSlot(2);
            return chestItem.getItem() == Items.ELYTRA;
        }
        return false;
    }

    private float[] calculateRotations(LivingEntity target) {
        double deltaX = target.getPosX() - mc.player.getPosX();
        double deltaY = target.getPosY() + target.getEyeHeight() - (mc.player.getPosY() + mc.player.getEyeHeight());
        double deltaZ = target.getPosZ() - mc.player.getPosZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));
        return new float[]{yaw, pitch};
    }

    private void attackEntityAndSwing(final LivingEntity targetEntity) {
        if (settings.get(2) && mc.player.isBlocking()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        boolean sprint = false;
        if (CEntityActionPacket.lastUpdatedSprint) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            sprint = true;
        }

        boolean isAxeOrSword = isWeapon(mc.player.getHeldItemMainhand().getItem());
        if (Manager.FUNCTION_MANAGER.syncTps.state) {
            int randomDelay = 607 + (int) (Math.random() * (655 - 607 + 1));
            cpsLimit = System.currentTimeMillis() + randomDelay;
            mc.playerController.attackEntity(mc.player, targetEntity);
            mc.player.swingArm(Hand.MAIN_HAND);
        } else {
            if (isAxeOrSword) {
                if (timerUtil.hasTimeElapsed(456)) {
                    if (mc.player.getCooledAttackStrength(0.5f) >= 1.0f && targetEntity.isAlive()) {
                        mc.playerController.attackEntity(mc.player, targetEntity);
                        mc.player.swingArm(Hand.MAIN_HAND);
                        timerUtil.reset();
                    }
                }
            }
            if (!isAxeOrSword) {
                cpsLimit = System.currentTimeMillis() + 552;

                mc.playerController.attackEntity(mc.player, targetEntity);
                mc.player.swingArm(Hand.MAIN_HAND);
            }
        }

        if (settings.get(3)) {
            breakShieldAndSwapSlot();
        }

        if (sprint) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
        }
    }

    private boolean isWeapon(Item item) {
        return item instanceof AxeItem;
    }

    private void breakShieldAndSwapSlot() {
        LivingEntity targetEntity = target;
        if (targetEntity instanceof PlayerEntity player) {
            boolean isBlocking = target.isActiveItemStackBlocking(1);
            boolean isShieldEquipped = (target.getHeldItemOffhand().getItem() == Items.SHIELD
                    || target.getHeldItemMainhand().getItem() == Items.SHIELD);

            if (isBlocking && !player.isSpectator() && !player.isCreative() && isShieldEquipped) {
                int slot = breakShield(player);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
        }
    }

    public int breakShield(LivingEntity target) {
        int hotBarSlot = InventoryUtils.getAxe(true);
        if (hotBarSlot != -1) {
            player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
            mc.playerController.attackEntity(player, target);
            player.swingArm(Hand.MAIN_HAND);
            player.connection.sendPacket(new CHeldItemChangePacket(player.inventory.currentItem));
            return hotBarSlot;
        } else {
            InventoryUtils.inventorySwapAxe(false);
            mc.playerController.attackEntity(player, target);
            player.swingArm(Hand.MAIN_HAND);
        }
        return -1;
    }


    private boolean shouldAttack(LivingEntity targetEntity) {
        return this.canAttack() && targetEntity != null && this.cpsLimit <= System.currentTimeMillis();
    }

    public boolean canAttack() {
        boolean headInWeb = false;
        boolean feetInWeb = false;
        double x;
        double z;

        for (x = -0.31; x <= 0.31; x += 0.31) {
            for (z = -0.31; z <= 0.31; z += 0.31) {
                for (double y = (double) mc.player.getEyeHeight(); y >= 0.0; y -= 0.1) {
                    BlockPos headPos = new BlockPos(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z);
                    if (mc.world.getBlockState(headPos).getBlock() == Blocks.COBWEB) {
                        headInWeb = true;
                        break;
                    }
                }
            }
        }

        if (!headInWeb) {
            for (x = -0.31; x <= 0.31; x += 0.31) {
                for (z = -0.31; z <= 0.31; z += 0.31) {
                    BlockPos pos = new BlockPos(mc.player.getPosX() + x, mc.player.getPosY(), mc.player.getPosZ() + z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        feetInWeb = true;
                        break;
                    }
                }
            }
        }

        BlockPos headPosAbove = new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 1.5, mc.player.getPosZ());
        boolean blockAbove = mc.world.getBlockState(headPosAbove).getBlock() != Blocks.AIR;
        boolean onSpace = this.settings.get(4) && player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown();
        boolean reasonForAttack = player.isOnLadder() || player.isInWater() && player.areEyesInFluid(FluidTags.WATER)
                || player.isRidingHorse() || player.abilities.isFlying || player.isElytraFlying()
                || player.isPotionActive(Effects.LEVITATION) || player.isPotionActive(Effects.SLOW_FALLING)
                || (mc.player.isInLava() && mc.player.areEyesInFluid(FluidTags.LAVA)) || headInWeb || feetInWeb;

        float elytraDistance = 0.0F;
        if (player.isElytraFlying()) {
            elytraDistance = elytradist.getValue().floatValue() - 0.05F;
        }

        double dynamicDistance = this.distance.getValue().floatValue();
        if (this.settings.get(8) && mc.player.isElytraFlying()) {
            dynamicDistance = Math.max(3.0, Math.min(dynamicDistance, this.getDistance(target) + 0.5));
        }

        if (!(this.getDistance(target) >= (double) (dynamicDistance - elytraDistance))
                && !(player.getCooledAttackStrength(1.5F) < 0.92F)) {
            if (Manager.FUNCTION_MANAGER.freeCam.player != null) {
                return true;
            } else if (blockAbove && mc.player.isForcedDown() && mc.player.collidedVertically) {
                return true;
            } else if (!reasonForAttack && this.settings.get(0)) {
                return onSpace || (!player.isOnGround() && player.fallDistance > (player.isInBlock() ? 0.0f : (blockAbove ? 0.4f : 0.0f)));
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean isInWeb() {
        for (double x = -0.31; x <= 0.31; x += 0.31) {
            for (double z = -0.31; z <= 0.31; z += 0.31) {
                for (double y = mc.player.getEyeHeight(); y >= 0.0; y -= 0.1) {
                    BlockPos pos = new BlockPos(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static float bpstarget() {
        double distance = Math.sqrt(Math.pow(target.getPosX() - target.prevPosX, 2) +
                Math.pow(target.getPosY() - target.prevPosY, 2) +
                Math.pow(target.getPosZ() - target.prevPosZ, 2));
        float bps = (float) (distance * mc.timer.timerSpeed * 20.0D);
        return (float) (Math.round(bps * 10) / 10.0f);
    }

    private LivingEntity findTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity && isValidTarget((LivingEntity) entity)) {
                targets.add((LivingEntity) entity);

            }
        }

        if (targets.isEmpty()) {
            return null;
        }

        if (targets.size() > 1) {
            switch (sortMode.get()) {
                case "Продвинутая" -> {
                    targets.sort(Comparator.comparingDouble(target -> {
                        if (target instanceof PlayerEntity player) {
                            if (player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
                                return Double.MIN_VALUE;
                            }
                            return -this.getEntityArmor(player);
                        }
                        if (target instanceof LivingEntity livingEntity) {
                            return -livingEntity.getTotalArmorValue();
                        }
                        return 0.0;
                    }).thenComparing((o, o1) -> {
                        double health = getEntityHealth((LivingEntity) o);
                        double health1 = getEntityHealth((LivingEntity) o1);
                        return Double.compare(health, health1);
                    }).thenComparing((object, object2) -> {
                        double d2 = getDistance((LivingEntity) object);
                        double d3 = getDistance((LivingEntity) object2);
                        return Double.compare(d2, d3);
                    }));
                }
                case "Приоритет" -> {
                    targets.sort(Comparator.comparingDouble(this::getEntityHealth).thenComparingDouble(player::getDistance));
                }
            }
        } else {
            cpsLimit = System.currentTimeMillis();
        }
        return targets.get(0);
    }

    private boolean isValidTarget(final LivingEntity base) {
        if (base.getShouldBeDead() || !base.isAlive() || base == player) return false;
        if (base instanceof PlayerEntity) {
            boolean nearElytraPlayer = false;
            Iterator var3 = mc.world.getPlayers().iterator();

            while(var3.hasNext()) {
                PlayerEntity entity = (PlayerEntity)var3.next();
                if (entity != player && !Manager.FRIEND_MANAGER.isFriend(entity.getName().getString()) && ((ItemStack)entity.inventory.armorInventory.get(2)).getItem() == Items.ELYTRA && player.getDistance(entity) < this.elytrarotate.getValue().floatValue() + (this.distance.getValue().floatValue())) {
                    nearElytraPlayer = true;
                }
            }
        }
        if (base instanceof PlayerEntity) {
            String playerName = base.getName().getString();
            if (Manager.FRIEND_MANAGER.isFriend(playerName) && !targets.get(1) || Manager.FUNCTION_MANAGER.freeCam.player != null && playerName.equals(Manager.FUNCTION_MANAGER.freeCam.player.getName().getString()) || base.getTotalArmorValue() == 0 && (!targets.get(0) || !targets.get(2))) return false;
        }
        if (AntiBot.checkBot(base)) {
            return false;
        } else {
            if ((base instanceof MobEntity) && !targets.get(3)) return false;
            if ((base instanceof PlayerEntity) && !targets.get(0)) return false;

            if (base instanceof ArmorStandEntity || base instanceof PlayerEntity && ((PlayerEntity) base).isBot)
                return false;

            if (!(base instanceof ArmorStandEntity) && (!(base instanceof PlayerEntity) || !((PlayerEntity) base).isBot)) {
                if (!settings.get(5) && !canAttackThroughWalls(base))
                    return false;

                float elytrarotate1 = 0.0F;
                if (player.isElytraFlying()) {
                    elytrarotate1 = this.elytrarotate.getValue().floatValue();
                }

                if (!player.isElytraFlying()) {
                    elytrarotate1 = 0.0F;
                }

                return this.getDistance(base) <= (double) ((this.distance.getValue().floatValue()) + this.rotateDistance.getValue().floatValue() + elytrarotate1);
            }
        }
        return false;
    }

    private boolean canAttackThroughWalls(LivingEntity targetEntity) {
        Vector3d targetVec = targetEntity.getPositionVec().add(0.0, (double)targetEntity.getEyeHeight(), 0.0);
        Vector3d playerVec = Minecraft.player.getPositionVec().add(0.0, (double)Minecraft.player.getEyeHeight(), 0.0);
        RayTraceResult result = mc.world.rayTraceBlocks(new RayTraceContext(playerVec, targetVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, Minecraft.player));
        return ((RayTraceResult)result).getType() == RayTraceResult.Type.MISS;
    }

    private double getDistance(LivingEntity entity) {
        return AuraUtil.getVector(entity).length();
    }

    public double getEntityArmor(PlayerEntity target) {
        double totalArmor = 0.0;

        for (ItemStack armorStack : target.inventory.armorInventory) {
            if (armorStack != null && armorStack.getItem() instanceof ArmorItem) {
                totalArmor += getProtectionLvl(armorStack);
            }
        }

        return totalArmor;
    }

    public double getEntityHealth(Entity ent) {
        if (ent instanceof PlayerEntity player) {
            double armorValue = getEntityArmor(player) / 20.0;
            return (player.getHealth() + player.getAbsorptionAmount()) * armorValue;
        } else if (ent instanceof LivingEntity livingEntity) {
            return livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
        }
        return 0.0;
    }


    private double getProtectionLvl(ItemStack stack) {
        ArmorItem armor = (ArmorItem) stack.getItem();
        double damageReduce = armor.getDamageReduceAmount();
        if (stack.isEnchanted()) {
            damageReduce += (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25;
        }
        return damageReduce;
    }

    @Override
    public void onDisable() {
        this.rotate = new Vector2f(player.rotationYaw, player.rotationPitch);
        target = null;
        cpsLimit = System.currentTimeMillis();
        super.onDisable();
    }

    private Boolean get() {
        return settings.get(3);
    }
}