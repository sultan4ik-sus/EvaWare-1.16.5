package newcode.fun.module.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.Manager;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.math.PlayerPositionTracker;
import newcode.fun.utils.misc.HudUtil;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.render.points.Point;
import org.joml.Vector2d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.ProjectionUtils;
import newcode.fun.utils.render.RenderUtils;
import org.joml.Vector4i;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.max;
import static net.minecraft.client.Minecraft.player;

@Annotation(name="Particles", type= TypeList.Render)
public class Particles
        extends Module {
    public MultiBoxSetting spawn = new MultiBoxSetting("Появляется при",new BooleanOption("Ударе", true), new BooleanOption("Ходьбе", true), new BooleanOption("Метаний", true));
    private ModeSetting mode = new ModeSetting("Мод", "Шарики", "Шарики","Снежинки","Сердечки","Звезда","Молния","Свинка","Свастунчик","Лапка");
    public MultiBoxSetting spawnin = new MultiBoxSetting("При метаний", new BooleanOption("Пёрлов", true), new BooleanOption("Стрел", true), new BooleanOption("Трезубцев", true)).setVisible(() -> spawn.get(2));
    CopyOnWriteArrayList<Point> points = new CopyOnWriteArrayList();
    public BooleanOption rotation = new BooleanOption("Рандом ротация", true);
    private final TimerUtil timerUtil = new TimerUtil();
    public final SliderSetting siz = new SliderSetting("Размер", 1.5f, 1F, 2F, 0.1F);
    public final SliderSetting fizika = new SliderSetting("Физика", 1.5f, 0f, 2f, 0.5F);

    public Particles() {
        this.addSettings(spawn, spawnin, mode, siz, fizika, rotation);
    }

    @Override
    public boolean onEvent(Event event) {
        Event e;
        CUseEntityPacket use;
        EventPacket e2;
        String bps = HudUtil.calculateBPS() + "";

        IPacket var4;
        if (spawn.get(1)) {
            float currentBps = Float.parseFloat(bps);
            if (currentBps > 0.1f) {
                if (timerUtil.hasTimeElapsed(ThreadLocalRandom.current().nextInt(40, 160))) {
                    this.createPointsRandom(mc.player.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.1f, 0.3f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                    this.createPointsRandom(mc.player.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.1f, 0.3f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                    timerUtil.reset();
                }
            }
        }
        if (spawn.get(0) && event instanceof EventPacket && (var4 = (e2 = (EventPacket)event).getPacket()) instanceof CUseEntityPacket && (use = (CUseEntityPacket)var4).getAction() == CUseEntityPacket.Action.ATTACK) {
            Entity entity = use.getEntityFromWorld(Particles.mc.world);
            if (Particles.mc.world != null && entity != null) {
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
                this.createPoints(entity.getPositionVec().add(ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f), ThreadLocalRandom.current().nextFloat(0.3f, 2.0f), ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f)));
            }
        }

        if (event instanceof EventMotion) {

            for (Entity entity : Particles.mc.world.getAllEntities()) {
                if (spawn.get(2)) {
                    if (entity instanceof EnderPearlEntity && spawnin.get(0)) {
                        EnderPearlEntity p = (EnderPearlEntity) entity;
                        this.points.add(new Point(this, p.getPositionVec()));
                    }
                    if (entity instanceof TridentEntity && spawnin.get(2)) {
                        TridentEntity t = (TridentEntity) entity;
                        if (!t.isInGround()) {
                            this.points.add(new Point(this, t.getPositionVec()));
                        }
                    }
                    if (entity instanceof ArrowEntity && spawnin.get(1)) {
                        ArrowEntity a = (ArrowEntity) entity;
                        if (!a.isInGround()) {
                            this.points.add(new Point(this, a.getPositionVec()));
                        }
                    }
                }
            }
        }

        if (event instanceof EventRender && ((EventRender)(e = (EventRender)event)).isRender2D()) {
            for (Point point : this.points) {
                int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
                int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
                long alive = System.currentTimeMillis() - point.createdTime;
                if (alive <= point.aliveTime) {
                    if (Minecraft.player.canVectorBeSeenFixed(point.position) && PlayerPositionTracker.isInView(point.position)) {
                        Vector2d pos = ProjectionUtils.project(point.position.x, point.position.y, point.position.z);
                        if (pos == null) continue;
                        point.update();
                        double distance = Math.sqrt(
                                Math.pow(player.getPosX() - point.position.x, 2) +
                                        Math.pow(player.getPosY() - point.position.y, 2) +
                                        Math.pow(player.getPosZ() - point.position.z, 2)
                        );


                        float sized = (float) this.getScale(point.position, siz.getValue().floatValue());

                        float alpha = Math.min(1.0f, (float) alive / 1100.0f);
                        alpha = Math.min(alpha, 1.0f - ((float) alive / (float) 4900));
                        alpha = Math.max(0.0f, alpha);

                        if (rotation.get()) {
                            GlStateManager.pushMatrix();
                            GlStateManager.translatef((float) pos.x, (float) pos.y, 0.0F);
                            GlStateManager.rotatef(point.rotation, 0.0F, 0.0F, 1.0F); // Используем сохраненный угол
                            GlStateManager.translatef((float) -pos.x, (float) -pos.y, 0.0F);
                            GlStateManager.enableBlend();
                            GlStateManager.blendFunc(770, 1);
                        }
                        switch (this.mode.get()) {
                            case "Шарики": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/bloom.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )
                                );
                                break;
                            }
                            case "Снежинки": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/snow.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )
                                );
                                break;
                            }
                            case "Сердечки": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/heart.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )

                                );
                                break;
                            }
                            case "Молния": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/thunder.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )
                                );
                                break;
                            }
                            case "Свинка": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/pig.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )
                                );
                                break;
                            }
                            case "Свастунчик": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/svastun.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )
                                );
                                break;
                            }
                            case "Лапка": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/paw.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )
                                );
                                break;
                            }
                            case "Звезда": {
                                RenderUtils.Render2D.drawImageAlph(
                                        new ResourceLocation("newcode/images/all/particle/star.png"),
                                        (float) pos.x, (float) pos.y, sized, sized,
                                        new Vector4i(
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha)),
                                                ColorUtils.setAlpha(firstColor2, (int)(155 * alpha))
                                        )
                                );
                            }
                        }
                        if (rotation.get()) {
                            GlStateManager.disableBlend();
                            GlStateManager.popMatrix();
                        }
                    }
                }
            }
        }
        return false;
    }

    private void createPointsRandom(Vector3d position2) {
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(1, 2); ++i) {
            this.points.add(new Point(this, position2));
        }
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);
        return Math.max(10.0, 920.0 / distance) * (size / 30.0) / (fov == 70.0 ? 1.0 : fov / 70.0);
    }

    private void createPoints(Vector3d position2) {
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(1, 2); ++i) {
            this.points.add(new Point(this, position2));
        }
    }
}
