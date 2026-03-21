package com.scarasol.sona.mixin.tacz;

import com.scarasol.sona.client.renderer.SonaRenderType;
import com.scarasol.sona.compat.ShaderCompatUtil;
import com.tacz.guns.client.model.functional.AttachmentRender;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AttachmentRender.class, remap = false)
public abstract class TACZAttachmentRenderMixin {

    @Redirect(
            method = "lambda$renderAttachment$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityCutout(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private static RenderType sona$useCamouflageRenderType(ResourceLocation texture) {
        Float alpha = SonaRenderType.camoAlpha.get();
        if (alpha == null || alpha >= 1.0f) {
            return RenderType.entityCutout(texture);
        }

        return ShaderCompatUtil.isShaderActive()
                ? RenderType.entityTranslucent(texture)
                : SonaRenderType.entityDither(texture);
    }
}
