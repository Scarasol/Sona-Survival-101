package com.scarasol.sona.mixin.geckolib;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.sona.client.renderer.AlphaVertexConsumer;
import com.scarasol.sona.client.renderer.CamouflageRenderUtil;
import com.scarasol.sona.client.renderer.SonaRenderType;
import com.scarasol.sona.compat.ShaderCompatUtil;
import com.scarasol.sona.mixin.CompositeRenderTypeAccessor;
import com.scarasol.sona.mixin.CompositeStateAccessor;
import com.scarasol.sona.mixin.TextureStateShardAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Optional;

@Pseudo
@Mixin(value = GeoItemRenderer.class)
public abstract class GeoItemRendererMixin<T extends net.minecraft.world.item.Item & GeoAnimatable> {

    @WrapOperation(
            method = "renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/renderer/GeoItemRenderer;getRenderType(Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/MultiBufferSource;F)Lnet/minecraft/client/renderer/RenderType;",
                    remap = false
            )
    )
    private RenderType sona$useCamouflageRenderType(
            GeoItemRenderer<T> instance,
            GeoAnimatable animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick,
            Operation<RenderType> original
    ) {
        RenderType originalType = original.call(instance, animatable, texture, bufferSource, partialTick);
        return sona$selectCamouflageRenderType(originalType);
    }

    @WrapOperation(
            method = "renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer sona$wrapCamouflageItemBuffer(
            MultiBufferSource bufferSource,
            RenderType renderType,
            boolean hasCrumbling,
            boolean hasFoil,
            Operation<VertexConsumer> original
    ) {
        return sona$wrapBuffer(original, bufferSource, renderType, hasCrumbling, hasFoil);
    }

    private static RenderType sona$selectCamouflageRenderType(RenderType originalRenderType) {
        Float alpha = SonaRenderType.camoAlpha.get();
        if (alpha == null || alpha >= 1.0f) {
            return originalRenderType;
        }

        if (!(originalRenderType instanceof CompositeRenderTypeAccessor accessor)) {
            return originalRenderType;
        }

        RenderType.CompositeState state = accessor.sona$getState();
        RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor) (Object) state).sona$getTextureState();
        if (!(textureState instanceof TextureStateShardAccessor texAccessor)) {
            return originalRenderType;
        }

        Optional<ResourceLocation> texture = texAccessor.sona$getTexture();
        if (texture.isEmpty()) {
            return originalRenderType;
        }

        return ModList.get().isLoaded("oculus") && ShaderCompatUtil.isShaderActive()
                ? RenderType.entityTranslucent(texture.get())
                : SonaRenderType.entityDither(texture.get());
    }

    private static VertexConsumer sona$wrapBuffer(
            Operation<VertexConsumer> original,
            MultiBufferSource bufferSource,
            RenderType renderType,
            boolean hasCrumbling,
            boolean hasFoil
    ) {
        Float alpha = SonaRenderType.camoAlpha.get();
        if (alpha == null || alpha >= 1.0f) {
            return original.call(bufferSource, renderType, hasCrumbling, hasFoil);
        }

        SonaRenderType.camoAlpha.remove();
        try {
            VertexConsumer consumer = original.call(bufferSource, renderType, hasCrumbling, hasFoil);
            return new AlphaVertexConsumer(consumer, CamouflageRenderUtil.itemAlpha(alpha));
        } finally {
            SonaRenderType.camoAlpha.set(alpha);
        }
    }
}
