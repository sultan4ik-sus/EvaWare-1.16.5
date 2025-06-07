package dev.waveycapes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelRenderer;

public interface CapeRenderer {

    void render(final AbstractClientPlayerEntity p0, final int p1, final ModelRenderer p2, final MatrixStack p3, final IRenderTypeBuffer p4, final int p5, final int p6);

    default IVertexBuilder getVertexConsumer(final IRenderTypeBuffer multiBufferSource, final AbstractClientPlayerEntity player) {
        return null;
    }

    boolean vanillaUvValues();
    
}
