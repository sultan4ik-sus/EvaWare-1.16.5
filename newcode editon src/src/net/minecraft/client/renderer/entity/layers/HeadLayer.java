package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.Manager;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

public class HeadLayer<T extends LivingEntity, M extends EntityModel<T> & IHasHead> extends LayerRenderer<T, M> {
    private final float field_239402_a_;
    private final float field_239403_b_;
    private final float field_239404_c_;

    public HeadLayer(IEntityRenderer<T, M> p_i50946_1_) {
        this(p_i50946_1_, 1.0F, 1.0F, 1.0F);
    }

    public HeadLayer(IEntityRenderer<T, M> p_i232475_1_, float p_i232475_2_, float p_i232475_3_, float p_i232475_4_) {
        super(p_i232475_1_);
        this.field_239402_a_ = p_i232475_2_;
        this.field_239403_b_ = p_i232475_3_;
        this.field_239404_c_ = p_i232475_4_;
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (!itemstack.isEmpty()) {
            Item item = itemstack.getItem();
            matrixStackIn.push();
            matrixStackIn.scale(this.field_239402_a_, this.field_239403_b_, this.field_239404_c_);
            boolean flag = entitylivingbaseIn instanceof VillagerEntity || entitylivingbaseIn instanceof ZombieVillagerEntity;
            float f2;
            if (entitylivingbaseIn.isChild() && !(entitylivingbaseIn instanceof VillagerEntity)) {
                f2 = 2.0F;
                float f1 = 1.4F;
                matrixStackIn.translate(0.0, 0.03125, 0.0);
                matrixStackIn.scale(0.7F, 0.7F, 0.7F);
                matrixStackIn.translate(0.0, 1.0, 0.0);
            }

            ((IHasHead)this.getEntityModel()).getModelHead().translateRotate(matrixStackIn);
            if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
                f2 = 1.1875F;
                matrixStackIn.scale(1.1875F, -1.1875F, -1.1875F);
                if (flag) {
                    matrixStackIn.translate(0.0, 0.0625, 0.0);
                }

                GameProfile gameprofile = null;
                if (itemstack.hasTag()) {
                    CompoundNBT compoundnbt = itemstack.getTag();
                    if (compoundnbt.contains("SkullOwner", 10)) {
                        gameprofile = NBTUtil.readGameProfile(compoundnbt.getCompound("SkullOwner"));
                    } else if (compoundnbt.contains("SkullOwner", 8)) {
                        String s = compoundnbt.getString("SkullOwner");
                        if (!StringUtils.isBlank(s)) {
                            gameprofile = SkullTileEntity.updateGameProfile(new GameProfile((UUID)null, s));
                            compoundnbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
                        }
                    }
                }

                matrixStackIn.translate(-0.5, 0.0, -0.5);
                SkullTileEntityRenderer.render((Direction)null, 180.0F, ((AbstractSkullBlock)((BlockItem)item).getBlock()).getSkullType(), gameprofile, limbSwing, matrixStackIn, bufferIn, packedLightIn);
            } else if (!(item instanceof ArmorItem) || ((ArmorItem)item).getEquipmentSlot() != EquipmentSlotType.HEAD) {
                f2 = 0.625F;
                matrixStackIn.translate(0.0, -0.25, 0.0);
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
                matrixStackIn.scale(0.625F, -0.625F, -0.625F);
                if (flag) {
                    matrixStackIn.translate(0.0, 0.1875, 0.0);
                }

                Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.HEAD, false, matrixStackIn, bufferIn, packedLightIn);
            }

            matrixStackIn.pop();
        }

        if (Manager.FUNCTION_MANAGER.chinaHat.state && entitylivingbaseIn instanceof PlayerEntity player) {
            if (player instanceof ClientPlayerEntity || Manager.FRIEND_MANAGER.isFriend(TextFormatting.getTextWithoutFormattingCodes(player.getName().getString()))) {
                float width = player.getWidth();
                int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
                int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                RenderSystem.enableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                RenderSystem.shadeModel(7425);
                GL11.glEnable(2848);
                RenderSystem.lineWidth(0.5F);
                RenderSystem.color4f(-1.0F, -1.0F, -1.0F, -1.0F);
                float[] colors = new float[0];
                matrixStackIn.push();
                float offset = ((ItemStack)player.inventory.armorInventory.get(3)).isEmpty() ? -0.41F : -0.5F;
                ((IHasHead)this.getEntityModel()).getModelHead().translateRotate(matrixStackIn);
                matrixStackIn.translate(0.0, (double)offset, 0.0);
                matrixStackIn.rotate(Vector3f.ZN.rotationDegrees(180.0F));
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90.0F));
                buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                int i = 0;

                StyleManager var10000;
                short size;
                for(size = 360; i <= size; ++i) {
                    var10000 = Manager.STYLE_MANAGER;
                    colors = RenderUtils.IntColor.rgb(StyleManager.getCurrentStyle().getColor(i));
                    buffer.pos(matrixStackIn.getLast().getMatrix(), 0.0F, 0.3F, 0.0F).color(colors[0], colors[1], colors[2], 40.0F).endVertex();
                }

                i = 0;

                for(size = 360; i <= size; ++i) {
                    var10000 = Manager.STYLE_MANAGER;
                    colors = RenderUtils.IntColor.rgb(StyleManager.getCurrentStyle().getColor(i));
                    buffer.pos(matrixStackIn.getLast().getMatrix(), -MathHelper.sin((float)i * 6.2831855F / (float)size) * width, 0.0F, MathHelper.cos((float)i * 6.2831855F / (float)size) * width).color(colors[0], colors[1], colors[2], 40.0F).endVertex();
                }

                tessellator.draw();
                buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
                i = 0;

                for(size = 360; i <= size; ++i) {
                    buffer.pos(matrixStackIn.getLast().getMatrix(), -MathHelper.sin((float)i * 6.2831855F / (float)size) * width, 0.0F, MathHelper.cos((float)i * 6.2831855F / (float)size) * width).color(colors[0], colors[1], colors[2], 40.0F).endVertex();
                }

                RenderSystem.depthMask(false);
                tessellator.draw();
                RenderSystem.depthMask(true);
                matrixStackIn.pop();
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                RenderSystem.enableTexture();
                RenderSystem.shadeModel(7424);
                RenderSystem.enableCull();
            }
        }

    }
}
