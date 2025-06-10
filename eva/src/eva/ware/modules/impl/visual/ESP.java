package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import eva.ware.Evaware;
import eva.ware.events.EventPreRender3D;
import eva.ware.events.EventUpdate;
import eva.ware.manager.friend.FriendManager;
import eva.ware.events.EventRender2D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.combat.AntiBot;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ColorSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import eva.ware.manager.Theme;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;
import eva.ware.utils.animations.impl.DecelerateAnimation;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.Vector4i;
import eva.ware.utils.render.engine2d.ProjectionUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RectUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.font.Fonts;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

@ModuleRegister(name = "ESP", category = Category.Visual)
public class ESP extends Module {
    public ModeListSetting remove = new ModeListSetting("Убрать", new CheckBoxSetting("Боксы", false), new CheckBoxSetting("Полоску хп", false), new CheckBoxSetting("Зачарования", false), new CheckBoxSetting("Список эффектов", false));
    public ModeListSetting targets = new ModeListSetting("Отображать",
            new CheckBoxSetting("Себя", true),
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Предметы", false),
            new CheckBoxSetting("Мобы", false)
            );

    public CheckBoxSetting targetEsp = new CheckBoxSetting("TargetESP", true);
    public ModeSetting mode = new ModeSetting("Мод", "Маркер", "Маркер", "Призраки", "Кругляшок").visibleIf(() -> targetEsp.getValue());

    private final AnimationUtility alpha = new DecelerateAnimation(600, 255);
    private LivingEntity currentTarget;
    private long lastTime = System.currentTimeMillis();

    float healthAnimation = 0.0f;

    public ESP() {
        addSettings(targets, remove, targetEsp, mode);
    }

    float length;

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    public ColorSetting color = new ColorSetting("Color", -1);

    @Subscribe
    private void onUpdate(EventUpdate e) {
        boolean bl = (Evaware.getInst().getModuleManager().getHitAura().isEnabled() || Evaware.getInst().getModuleManager().getTPInfluence().isEnabled());
        if (Evaware.getInst().getModuleManager().getHitAura().getTarget() != null) {
            currentTarget = Evaware.getInst().getModuleManager().getHitAura().getTarget();
        }

        this.alpha.setDirection(bl && Evaware.getInst().getModuleManager().getHitAura().getTarget() != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Subscribe
    public void onRender(EventPreRender3D e) {
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }

        if (mode.is("Кругляшок")) {
            drawCircleMarker(e.getMatrix(), e);
        }

        if (mode.is("Призраки")) {
            drawSoulsMarker(e.getMatrix(), e);
        }
    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.world == null || e.getType() != EventRender2D.Type.PRE) {
            return;
        }

        positions.clear();

        Vector4i colors = new Vector4i(Theme.rectColor, Theme.rectColor, Theme.mainRectColor, Theme.mainRectColor);
        Vector4i friendColors = new Vector4i(Hud.getColor(ColorUtility.rgb(144, 238, 144), ColorUtility.rgb(0, 139, 0), 0, 1), Hud.getColor(ColorUtility.rgb(144, 238, 144), ColorUtility.rgb(0, 139, 0), 90, 1), Hud.getColor(ColorUtility.rgb(144, 238, 144), ColorUtility.rgb(0, 139, 0), 180, 1), Hud.getColor(ColorUtility.rgb(144, 238, 144), ColorUtility.rgb(0, 139, 0), 270, 1));

        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            if (!(entity instanceof PlayerEntity && entity != mc.player && targets.is("Игроки").getValue()
                    || entity instanceof ItemEntity && targets.is("Предметы").getValue()
                    || (entity instanceof AnimalEntity || entity instanceof MobEntity) && targets.is("Мобы").getValue()
                    || entity == mc.player && targets.is("Себя").getValue() && !(mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) && !Evaware.getInst().getModuleManager().getWorldTweaks().child
            )) continue;

            double x = MathUtility.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
            double y = MathUtility.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
            double z = MathUtility.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());

            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

            Vector4f position = null;
            for (int i = 0; i < 8; i++) {
                Vector2f vector = ProjectionUtility.project(i % 2 == 0 ? aabb.minX : aabb.maxX, (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY, (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            positions.put(entity, position);
        }


        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Vector4f position = entry.getValue();
            if (entry.getKey() instanceof LivingEntity entity) {
                if (!remove.is("Боксы").getValue()) {
                    RenderUtility.drawBox(position.x - 0.5f, position.y - 0.5f, position.z + 0.5f, position.w + 0.5f, 2, ColorUtility.rgba(0, 0, 0, 128));
                    RenderUtility.drawBox(position.x, position.y, position.z, position.w, 1, FriendManager.isFriend(entity.getName().getString()) ? friendColors : colors);
                }
                float hpOffset = 3f;
                float out = 0.5f;
                if (!remove.is("Полоску хп").getValue()) {
                    String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
                    Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

                    float hp = entity.getHealth();
                    float maxHp = entity.getMaxHealth();

                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        hp = score.getScorePoints();
                        maxHp = 20;
                    }

                    RenderUtility.drawRectBuilding(position.x - hpOffset - out, position.y - out, position.x - hpOffset + 1 + out, position.w + out, ColorUtility.rgba(0, 0, 0, 128));
                    RenderUtility.drawRectBuilding(position.x - hpOffset, position.y, position.x - hpOffset + 1, position.w, ColorUtility.rgba(0, 0, 0, 128));
                    RenderUtility.drawMCVerticalBuilding(position.x - hpOffset, position.y + (position.w - position.y) * (1 - MathHelper.clamp(hp / maxHp, 0, 1)), position.x - hpOffset + 1, position.w, FriendManager.isFriend(entity.getName().getString()) ? friendColors.w : colors.w, FriendManager.isFriend(entity.getName().getString()) ? friendColors.x : colors.x);
                }
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living) {
                Score score = mc.world.getScoreboard().getOrCreateScore(living.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();

                String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();


                if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }

                Vector4f position = entry.getValue();
                float width = position.z - position.x;

                GL11.glPushMatrix();

                String friendPrefix = FriendManager.isFriend(entity.getName().getString()) ? TextFormatting.GREEN + "[F] " : "";
                String creativePrefix = "";
                if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
                    creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + "GM" + TextFormatting.GRAY + "]";
                } else {
                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]";
                    } else {
                        creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + ((int) hp + (int) living.getAbsorptionAmount()) + TextFormatting.GRAY + "]";
                    }
                }

                healthAnimation = MathUtility.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);

                TextComponent name = (TextComponent) ITextComponent.getTextComponentOrEmpty(friendPrefix);
                int colorRect = FriendManager.isFriend(entity.getName().getString()) ? ColorUtility.rgba(66, 163, 60, 160) : ColorUtility.rgba(10, 10, 10, 160);
                name.append(FriendManager.isFriend(entity.getName().getString()) ?
                        (Evaware.getInst().getModuleManager().getNameProtect().isEnabled() ? ITextComponent.getTextComponentOrEmpty(TextFormatting.RED + "protected") : entity.getDisplayName())
                                : entity.getDisplayName());
                name.appendString(creativePrefix);
                glCenteredScale(position.x + width / 2f - length / 2f - 4, position.y - 9, length + 8, 13, 0.5f);

                length = mc.fontRenderer.getStringPropertyWidth(name);
                float x1 = position.x + width / 2f - length / 2f - 4;
                float y1 = position.y - 15.5f;
                RectUtility.getInstance().drawRoundedRectShadowed(e.getMatrixStack(), x1, y1, x1 + length + 8, y1 + 13, 0, 2, colorRect, colorRect, colorRect, colorRect, false, false, true, false);
                mc.fontRenderer.func_243246_a(e.getMatrixStack(), name, position.x + width / 2f - length / 2f, position.y - 12.5f, -1);

                GL11.glPopMatrix();
                if (!remove.is("Список эффектов").getValue()) {
                    drawPotions(e.getMatrixStack(), living, position.z + 2, position.y);
                }
                drawItems(e.getMatrixStack(), living, (int) (position.x + width / 2f), (int) (position.y - 14.5f));
            } else if (entity instanceof ItemEntity item) {
                Vector4f position = entry.getValue();
                float width = position.z - position.x;
                ITextComponent displayName = new StringTextComponent((item.getItem().getDisplayName().getString() + (item.getItem().getCount() < 1 ? "" : " x" + item.getItem().getCount())));

                float length = mc.fontRenderer.getStringPropertyWidth(displayName);
                GL11.glPushMatrix();

                glCenteredScale(position.x + width / 2f - length / 2f, position.y - 7, length, 10, 0.5f);
                mc.fontRenderer.func_243248_b(e.getMatrixStack(), displayName, position.x + width / 2f - length / 2f, position.y - 7, -1);
                GL11.glPopMatrix();
            }
        }

        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }

        if (mode.is("Маркер")) {
            drawImageMarker(e);
        }
    }

    public boolean isInView(Entity ent) {

        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }
    int index = 0;
    private void drawPotions(MatrixStack matrixStack, LivingEntity entity, float posX, float posY) {
        for (Iterator var8 = entity.getActivePotionEffects().iterator(); var8.hasNext(); ++index) {
            EffectInstance effectInstance = (EffectInstance)var8.next();

            int amp = effectInstance.getAmplifier() + 1;
            String ampStr = "";

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + amp;
            }


            String text = EffectUtils.getPotionDurationString(effectInstance, 1) + " - " + I18n.format(effectInstance.getEffectName(), new Object[0]) + ampStr;
            PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
            Effect effect = effectInstance.getPotion();
            float iconSize = (float) (8);
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
            mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            DisplayEffectsScreen.blit(matrixStack, (float) (posX),  (int)posY - 0.5f, 10, 8, 8, textureatlassprite);

            Fonts.montserrat.drawTextWithOutline(matrixStack, text, posX + iconSize, posY, ColorUtility.setAlpha(-1, (int) (255)), 6, 0.02f);
            posY += Fonts.montserrat.getHeight(6) + 1;
        }
    }

    private void drawItems(MatrixStack matrixStack, LivingEntity entity, int posX, int posY) {
        int size = 8;
        int padding = 6;

        float fontHeight = Fonts.consolas.getHeight(6);

        List<ItemStack> items = new ArrayList<>();

        ItemStack mainStack = entity.getHeldItemMainhand();

        if (!mainStack.isEmpty()) {
            items.add(mainStack);
        }

        for (ItemStack itemStack : entity.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;
            items.add(itemStack);
        }

        ItemStack offStack = entity.getHeldItemOffhand();

        if (!offStack.isEmpty()) {
            items.add(offStack);
        }

        posX -= (items.size() * (size + padding)) / 2f;

        for (ItemStack itemStack : items) {
            if (itemStack.isEmpty()) continue;

            GL11.glPushMatrix();

            glCenteredScale(posX, posY - 5, size / 2f, size / 2f, 0.5f);

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY - 5);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY - 5, null);

            GL11.glPopMatrix();

            if (itemStack.isEnchanted() && !remove.is("Зачарования").getValue()) {
                int ePosY = (int) (posY - fontHeight);

                Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.getEnchantments(itemStack);

                for (Enchantment enchantment : enchantmentsMap.keySet()) {
                    int level = enchantmentsMap.get(enchantment);

                    if (level < 1 || !enchantment.canApply(itemStack)) continue;

                    IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(enchantment.getName());

                    String enchText = iformattabletextcomponent.getString().substring(0, 2) + level;

                    Fonts.consolas.drawText(matrixStack, enchText, posX, ePosY - 5, -1, 6, 0.05f);

                    ePosY -= (int) fontHeight;
                }
            }

            posX += size + padding;
        }
    }

    public boolean isValid(Entity e) {
        if (AntiBot.isBot(e)) return false;

        return isInView(e);
    }

    public static void drawMcRect(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        bufferbuilder.pos(left, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 1.0F).color(f, f1, f2, f3).endVertex();

    }

    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        glTranslatef(x + w / 2, y + h / 2, 0);
        glScalef(f, f, 1);
        glTranslatef(-x - w / 2, -y - h / 2, 0);
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return Math.max(10f, 1000 / distance) * (size / 30f) / (fov == 70 ? 1 : fov / 70.0f);
    }

    public void drawImageMarker(EventRender2D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            double sin = Math.sin(System.currentTimeMillis() / 1000.0);
            double distance = mc.player.getDistance(currentTarget);
            float maxSize = (float) getScale(currentTarget.getPositionVec(), 10);
            float size = Math.max(maxSize - (float)distance, 20.0F);
            Vector3d interpolated = currentTarget.getPositon(e.getPartialTicks());
            Vector2f pos = ProjectionUtility.project(interpolated.x, interpolated.y + currentTarget.getHeight() / 2f, interpolated.z);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(pos.x, pos.y, 0);
            GlStateManager.rotatef((float) sin * 360, 0, 0, 1);
            GlStateManager.translatef(-pos.x, -pos.y, 0);

            if (pos != null) {
                RenderUtility.drawImageAlpha(new ResourceLocation("eva/images/modules/target.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, new Vector4i(
                        ColorUtility.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput())),
                        ColorUtility.setAlpha(Theme.mainRectColor, (int) (alpha.getOutput())),
                        ColorUtility.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput())),
                        ColorUtility.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput()))
                ));
                GlStateManager.popMatrix();
            }
        }
    }

    public void drawSoulsMarker(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();

            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);

            double x = currentTarget.getPosX();
            double y = currentTarget.getPosY() + (currentTarget.getHeight() / 2f);
            double z = currentTarget.getPosZ();
            double radius = 0.35 + currentTarget.getWidth() / 2;
            float speed = 30;
            float size = 0.6f;
            double distance = 24;
            int length = 24;
            int color = ColorUtility.multAlpha(ColorUtility.getColor(), 1);
            int alpha = 1;
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());
            Vector3d interpolated = MathUtility.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            interpolated.y += 0.25 + currentTarget.getHeight() / 2;

            ms.translate(interpolated.x + 0.2, interpolated.y, interpolated.z);

            RectUtility.bindTexture(new ResourceLocation("eva/images/glow.png"));

            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                double angle = 0.05f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle * (Math.PI / 2)) * radius;
                double c = Math.cos(angle * (Math.PI / 2)) * radius;
                double o = Math.cos(angle * (Math.PI / 3)) * radius;
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                ms.translate(-s, o, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 0).endVertex();

                tessellator.draw();

                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                ms.translate(s, -o, c);
            }

            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                double angle = 0.05f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle * (Math.PI / 2)) * radius;
                double c = Math.cos(angle * (Math.PI / 2)) * radius;
                double o = Math.sin(angle * (Math.PI / 3)) * radius;
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                ms.translate(s, o, c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtility.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 0).endVertex();

                tessellator.draw();

                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                ms.translate(-s, -o, -c);
            }

            ms.translate(-x, -y, -z);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }

    public void drawCircleMarker(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            double radius = 0.4 + currentTarget.getWidth() / 2;
            float speed = 30;
            float size = 0.3f;
            double distance = 155;
            int lenght = (int) (distance + currentTarget.getWidth());
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                    -mc.getRenderManager().info.getProjectedView().getY(),
                    -mc.getRenderManager().info.getProjectedView().getZ());

            Vector3d interpolated = MathUtility.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            ms.translate(interpolated.x + 0.15, interpolated.y + 0.2 + currentTarget.getHeight() / 2, interpolated.z);
            RectUtility.bindTexture(new ResourceLocation("eva/images/glow.png"));
            for (int j = 0; j < 1; j++) {
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                    double angle = 0.1f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);

                    double s = Math.sin(angle + j * (Math.PI / 1.5)) * radius;
                    double c = Math.cos(angle + j * (Math.PI / 1.5)) * radius;

                    double yOffset = Math.sin(System.currentTimeMillis() * 0.003 + j) * 0.8;

                    ms.translate(0, yOffset, 0);

                    ms.translate(s, 0, -c);

                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    int color = ColorUtility.getColor(i);
                    int alpha = (int) (1 * this.alpha.getOutput());
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtility.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtility.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtility.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtility.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();

                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate(-s, 0, c);
                    ms.translate(0, -yOffset, 0);
                }
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
