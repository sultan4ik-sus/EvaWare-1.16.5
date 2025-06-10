package eva.ware.ui.clienthud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import eva.ware.Evaware;
import eva.ware.events.AttackEvent;
import eva.ware.events.EventRender2D;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.manager.Theme;
import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;
import eva.ware.utils.animations.impl.EaseBackIn;
import eva.ware.manager.drag.Dragging;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.math.Vector4i;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.render.other.Scissor;
import eva.ware.utils.render.other.Stencil;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.engine2d.RectUtility;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TargetHud implements ElementRenderer {
    final TimerUtility timerUtility = new TimerUtility();
    @Getter
    final Dragging drag;
    @Getter
    LivingEntity entity = null;
    boolean allow;
    final AnimationUtility animation = new EaseBackIn(400, 1, 1);
    float healthAnimation = 0.0f;
    float absorptionAnimation = 0.0f;
    float width = 168 / 1.5f;
    float height = 55 / 1.5f;

    int headSize = 25;
    float spacing = 5;

    public void onAttack(AttackEvent e) {
        if (e.entity == mc.player) {
            return;
        }
        if (entity == null) {
            return;
        }
        if (e.entity instanceof LivingEntity) {
            for (int i = 0; i < 7; ++i) {
                Evaware.getInst().getModuleManager().getHud().getParticles().add(new TargetHud.HeadParticle(new Vector3d(drag.getX() + spacing + headSize / 2f, drag.getY() + spacing + headSize / 2f, 0.0)));
            }
        }
    }

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();
        entity = getTarget(entity);
        float size;

        boolean nigggerBotIsState = Evaware.getInst().getModuleManager().getTriggerbot().isEnabled();
        boolean hitAuraIsState = Evaware.getInst().getModuleManager().getHitAura().isEnabled();
        boolean out = nigggerBotIsState && !hitAuraIsState ? timerUtility.isReached(500) : !allow; // maybe !allow
        animation.setDuration(out ? 300 : 200);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);
        FloatFormatter formatter = new FloatFormatter();

        if (animation.getOutput() == 0.0f) {
            entity = null;
        }

        if (entity != null) {
            String name = entity.getName().getString();

            float posX = drag.getX();
            float posY = drag.getY();

            drag.setWidth(width);
            drag.setHeight(height);
            Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();
            String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

            if (entity instanceof PlayerEntity) {
                if (ClientUtility.isConnectedToServer("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }
            }

            healthAnimation = MathUtility.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);
            absorptionAnimation = MathUtility.fast(absorptionAnimation, MathHelper.clamp(entity.getAbsorptionAmount() / maxHp, 0, 1), 10);
            float animationValue = (float) animation.getOutput();
            float halfAnimationValueRest = (1 - animationValue) / 2f;
            float testX = posX + (width * halfAnimationValueRest);
            float testY = posY + (height * halfAnimationValueRest);
            float testW = width * animationValue;
            float testH = height * animationValue;
            float finalHp;

            if (ClientUtility.isConnectedToServer("funtime")) {
                finalHp = formatter.format(hp);
            } else if (ClientUtility.isConnectedToServer("reallyworld")) {
                finalHp = formatter.format(hp);
            } else {
                finalHp = formatter.format((hp + entity.getAbsorptionAmount()));
            }

            String ownDivider = "";
            String ownStatus = "";

            if (entity != mc.player) {
                ownDivider = " | ";
                double targetCheck = MathUtility.entity(entity, true, true, false, 1, false);
                double playerCheck = MathUtility.entity(mc.player, true, true, false,1, false);
                if (targetCheck > playerCheck) {
                    ownStatus = "Lose";
                } else if (targetCheck < playerCheck) {
                    ownStatus = "Win";
                } else if (targetCheck == playerCheck) {
                    ownStatus = "Tie";
                } else {
                    ownStatus = "Unknown";
                }
            } else {
                ownStatus = ownDivider = "";
            }

            GlStateManager.pushMatrix();
            RenderUtility.sizeAnimation(posX + (width / 2), posY + height / 2, animation.getOutput());
            if (is("Обычный")) {
                height = 55 / 1.5f;
                width = 168 / 1.5f;
                headSize = 25;
                RenderUtility.drawStyledShadowRectWithChange(ms, posX, posY, width, height);

                drawHead(eventRender2D.getMatrixStack(), entity, posX + spacing, posY + spacing + 1, headSize);

                Scissor.push();
                Scissor.setFromComponentCoordinates(testX, testY, testW, testH);
                Fonts.montserrat.drawText(eventRender2D.getMatrixStack(), name, posX - 0.5f + headSize + spacing + spacing, posY + 1 + spacing, Theme.textColor, 8, .07f);
                Fonts.montserrat.drawText(eventRender2D.getMatrixStack(), "Health: " + (finalHp) + ownDivider + ownStatus, posX - 0.5f + headSize + spacing + spacing, posY + 1.5f + spacing + spacing + spacing, Theme.darkTextColor, 6, .05f);
                Scissor.unset();
                Scissor.pop();

                Vector4i vector4i = new Vector4i(Theme.rectColor, Theme.rectColor, Theme.mainRectColor, Theme.mainRectColor);

                RenderUtility.drawRoundedRect(posX + 5 + headSize + spacing + spacing - 5, posY + height - spacing * 2, (width - 40), 5, new Vector4f(3, 3, 3, 3), ColorUtility.rgb(32, 32, 32));
                RenderUtility.drawRoundedRect(posX + 5 + headSize + spacing + spacing - 5, posY + height - spacing * 2, (width - 40) * healthAnimation, 5, new Vector4f(3, 3, 3, 3), vector4i);
                RenderUtility.drawShadow(posX + 5 + headSize + spacing + spacing - 5, posY + height - spacing * 2, (width - 40) * healthAnimation, 5, 8, ColorUtility.setAlpha(Theme.rectColor, 80), ColorUtility.setAlpha(Theme.mainRectColor, 80));
            }

            if (is("Кругляк")) {
                height = 46 / 1.5f;
                width = 150 / 1.5f;
                headSize = 15;
                RenderUtility.drawStyledShadowRectWithChange(ms, posX, posY, width, height);

                Stencil.initStencilToWrite();
                RenderUtility.drawCircle(posX + spacing + headSize / 2f, posY + spacing + headSize / 2f, headSize, -1);
                Stencil.readStencilBuffer(1);
                drawHead(eventRender2D.getMatrixStack(), entity, posX + spacing, posY + spacing, headSize);
                Stencil.uninitStencilBuffer();

                RenderUtility.drawCircleWithFill(posX + spacing + headSize / 2f, posY + spacing + headSize / 2f, 0, 360, headSize / 2f, 1.5f, false, Theme.mainRectColor);
                Stencil.initStencilToWrite();
                RenderUtility.drawCircle(posX + spacing + headSize / 2f, posY + spacing + headSize / 2f, headSize, -1);
                Stencil.readStencilBuffer(0);
                RenderUtility.drawShadow(posX + spacing, posY + spacing, headSize, headSize, headSize, Theme.mainRectColor);
                Stencil.uninitStencilBuffer();

                Scissor.push();
                Scissor.setFromComponentCoordinates(testX, testY, testW, testH);
                Fonts.montserrat.drawText(eventRender2D.getMatrixStack(), name, posX - 0.5f + headSize + spacing * 1.5f, posY + spacing, Theme.textColor, 7, .07f);
                Fonts.montserrat.drawText(eventRender2D.getMatrixStack(), "Health: " + (finalHp) + ownDivider + ownStatus, posX + headSize + spacing * 1.5f, posY + 0.5f + spacing * 2.5f, Theme.darkTextColor, 5.5f, .07f);
                Scissor.unset();
                Scissor.pop();

                Vector4i vector4i = new Vector4i(Theme.rectColor, Theme.rectColor, Theme.mainRectColor, Theme.mainRectColor);

                RenderUtility.drawRoundedRect(posX + spacing, posY + height - spacing * 1.5f, (width - spacing * 2), 3, new Vector4f(2,2,2,2), ColorUtility.rgb(32, 32, 32));
                RenderUtility.drawRoundedRect(posX + spacing, posY + height - spacing * 1.5f, (width - spacing * 2) * healthAnimation, 3, new Vector4f(2,2,2,2), vector4i);
                RenderUtility.drawShadow(posX + spacing, posY + height - spacing * 1.5f, (width - spacing * 2) * healthAnimation, 3, 8, ColorUtility.setAlpha(Theme.rectColor, 160), ColorUtility.setAlpha(Theme.mainRectColor, 160));
            }

            drawItemStack(posX + 5, posY - 11, 8, 0.5f);

            GlStateManager.popMatrix();
        }

        if (Evaware.getInst().getModuleManager().getHud().particlesOnTarget.getValue()) {
            for (HeadParticle p : Evaware.getInst().getModuleManager().getHud().getParticles()) {
                if (System.currentTimeMillis() - p.time > 2000L) {
                    Evaware.getInst().getModuleManager().getHud().getParticles().remove(p);
                }
                p.update();
                size = 1.0f - (float)(System.currentTimeMillis() - p.time) / 2000.0f;
                RenderUtility.drawCircle((float)p.pos.x, (float)p.pos.y, 3.5f, ColorUtility.setAlpha(Theme.rectColor, (int)(255.0f * p.alpha * size)));
            }
        }
    }

    private LivingEntity getTarget(LivingEntity nullTarget) {
        boolean hitAuraIsState = Evaware.getInst().getModuleManager().getHitAura().isEnabled();
        LivingEntity niggerBotTarget = Evaware.getInst().getModuleManager().getTriggerbot().getTarget();
        LivingEntity finalTarget = hitAuraIsState ? Evaware.getInst().getModuleManager().getHitAura().getTarget() : niggerBotTarget;
        LivingEntity target = nullTarget;
        if (finalTarget != null) {
            timerUtility.reset();
            allow = true;
            target = finalTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            timerUtility.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }

    private void drawHead(MatrixStack matrix, final Entity entity, final double x, final double y, final int size) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
            RenderSystem.enableTexture();
            mc.getTextureManager().bindTexture(player.getLocationSkin());
            float hurtPercent = (((AbstractClientPlayerEntity) entity).hurtTime - (((AbstractClientPlayerEntity) entity).hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
            RenderSystem.color4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
            AbstractGui.blit(matrix, (float) x, (float) y, size, size, 4F, 4F, 4F, 4F, 32F, 32F);
            RenderUtility.scaleStart((float) (x + size / 2F), (float) (y + size / 2F), 1.1F);
            AbstractGui.blit(matrix, (float) x, (float) y, size, size, 20, 4, 4, 4, 32, 32);
            RenderUtility.scaleEnd();
            RenderSystem.disableBlend();
        } else {
            int color = ColorUtility.getColor(20, 128);
            RectUtility.getInstance().drawRoundedRectShadowed(matrix, (float) x, (float) y, (float) (x + size), (float) (y + size), 2F, 1, color, color, color, color, false, false, true, true);
            ClientFonts.interRegular[size * 2].drawCenteredString(matrix, "?", x + (size / 2F), y + 3 + (size / 2F) - (ClientFonts.interRegular[size * 2].getFontHeight() / 2F), -1);
        }
    }

    private void drawItemStack(float x, float y, float offset, float scale) {
        ArrayList<ItemStack> stackList = new ArrayList(Arrays.asList(this.entity.getHeldItemMainhand(), this.entity.getHeldItemOffhand()));
        stackList.addAll((Collection)this.entity.getArmorInventoryList());
        AtomicReference<Float> posX = new AtomicReference(x);
        stackList.stream().filter((stack) -> {
            return !stack.isEmpty();
        }).forEach((stack) -> {
            drawItemStack(stack, (Float)posX.getAndAccumulate(offset, Float::sum), y, true, true, scale);
        });
    }

    public void drawItemStack(ItemStack stack, float x, float y, boolean withoutOverlay, boolean scale, float scaleValue) {
        RenderSystem renderSystem = new RenderSystem();
        renderSystem.pushMatrix();
        renderSystem.translatef(x, y, 0.0F);
        if (scale) {
            GL11.glScaled((double)scaleValue, (double)scaleValue, (double)scaleValue);
        }

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (withoutOverlay) {
            mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, 0, 0);
        }

        renderSystem.popMatrix();
    }

    public static boolean is(String name) {
        return Evaware.getInst().getModuleManager().getHud().tHudMode.is(name);
    }

    public class FloatFormatter {
        public float format(float value) {
            float multiplier = (float) Math.pow(10, 1);
            return Math.round(value * multiplier) / multiplier;
        }
    }

    public static class HeadParticle {
        private Vector3d pos;
        private final Vector3d end;
        private final long time;
        private float alpha;

        public HeadParticle(Vector3d pos) {
            this.pos = pos;
            this.end = pos.add((double)(-ThreadLocalRandom.current().nextFloat(-80.0F, 80.0F)), (double)(-ThreadLocalRandom.current().nextFloat(-80.0F, 80.0F)), (double)(-ThreadLocalRandom.current().nextFloat(-80.0F, 80.0F)));
            this.time = System.currentTimeMillis();
        }

        public void update() {
            this.alpha = MathUtility.lerp(this.alpha, 1.0F, 10.0F);
            this.pos = MathUtility.fast(this.pos, this.end, 0.5F);
        }
    }
}
