package com.scarasol.sona.mixin.tacz;

import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.client.renderer.SonaRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Pseudo
@Mixin(targets = "com.tacz.guns.client.renderer.item.GunItemRendererWrapper")
public abstract class TACZRendererMixin {

    @Inject(method = "renderByItem", at = @At("HEAD"))
    private void sona$setTaczFirstPersonAlpha(net.minecraft.world.item.ItemStack stack, ItemDisplayContext transformType, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            Player player = Minecraft.getInstance().player;
            if (player instanceof ILivingEntityAccessor accessor && accessor.getCamouflageAmplifier() > 0) {
                SonaRenderType.camoAlpha.set(accessor.getCamouflageAlpha());
            }
        }
    }

    @Inject(method = "renderByItem", at = @At("RETURN"))
    private void sona$clearTaczFirstPersonAlpha(net.minecraft.world.item.ItemStack stack, ItemDisplayContext transformType, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            SonaRenderType.camoAlpha.remove();
        }
    }
}