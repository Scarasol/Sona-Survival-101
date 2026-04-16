package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.client.renderer.SonaRenderType;
import com.scarasol.sona.compat.ShaderCompatUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin<T extends Entity, M extends EntityModel<T>> {
    @Inject(method = "renderColoredCutoutModel", cancellable = true, at = @At("HEAD"))
    private static <T extends LivingEntity> void sona$renderColoredCutoutModel(
            EntityModel<T> model,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float red,
            float green,
            float blue,
            CallbackInfo ci
    ) {
        if (entity instanceof ILivingEntityAccessor livingEntityAccessor && livingEntityAccessor.getCamouflageAmplifier() > 0) {
            // 【修改点】：根据光影状态选择 RenderType
            RenderType targetType = ModList.get().isLoaded("oculus") && ShaderCompatUtil.isShaderActive()
                    ? RenderType.entityTranslucent(texture)
                    : SonaRenderType.entityDither(texture);

            VertexConsumer vertexconsumer = buffer.getBuffer(targetType);
            model.renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), red, green, blue, livingEntityAccessor.getCamouflageAlpha());
            ci.cancel();
        }
    }
}
