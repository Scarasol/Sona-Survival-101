package com.scarasol.sona.mixin.tacz;

import com.scarasol.sona.client.renderer.CamouflageRenderUtil;
import com.scarasol.sona.client.renderer.SonaRenderType;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BedrockPart.class, remap = false)
public abstract class TACZBedrockPartMixin {

    @ModifyVariable(
            method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At("HEAD"),
            ordinal = 3,
            argsOnly = true
    )
    private float sona$applyCamouflageAlphaToCompile(float alpha) {
        Float camoAlpha = SonaRenderType.camoAlpha.get();
        if (camoAlpha == null || camoAlpha >= 1.0f) {
            return alpha;
        }
        return alpha * CamouflageRenderUtil.itemAlpha(camoAlpha);
    }

    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At("HEAD"),
            ordinal = 3,
            argsOnly = true
    )
    private float sona$applyCamouflageAlpha(float alpha) {
        Float camoAlpha = SonaRenderType.camoAlpha.get();
        if (camoAlpha == null || camoAlpha >= 1.0f) {
            return alpha;
        }
        return alpha * CamouflageRenderUtil.itemAlpha(camoAlpha);
    }
}
