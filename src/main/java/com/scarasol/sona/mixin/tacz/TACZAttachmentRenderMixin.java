package com.scarasol.sona.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.client.renderer.SonaRenderType;
import com.scarasol.sona.compat.ShaderCompatUtil;
import com.scarasol.sona.mixin.CompositeRenderTypeAccessor;
import com.scarasol.sona.mixin.CompositeStateAccessor;
import com.scarasol.sona.mixin.TextureStateShardAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.functional.AttachmentRender;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = AttachmentRender.class, remap = false)
public abstract class TACZAttachmentRenderMixin {

    @WrapOperation(
            method = "lambda$renderAttachment$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/client/model/BedrockAttachmentModel;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V"
            ),
            require = 1,
            remap = false
    )
    private static void sona$useCamouflageRenderType(
            BedrockAttachmentModel instance,
            ItemStack attachment,
            ItemStack gun,
            PoseStack poseStack,
            ItemDisplayContext transformType,
            RenderType renderType,
            int light,
            int overlay,
            Operation<Void> original
    ) {
        original.call(instance, attachment, gun, poseStack, transformType, sona$selectCamouflageRenderType(renderType), light, overlay);
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
}
