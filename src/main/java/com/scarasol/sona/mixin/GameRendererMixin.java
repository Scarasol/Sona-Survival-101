package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.sona.SonaMod;
import com.scarasol.sona.init.SonaTags;
import com.scarasol.sona.util.SonaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements AutoCloseable {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "bobHurt", cancellable = true, at = @At("HEAD"))
    private void sona$bobHurt(PoseStack poseStack, float yaw, CallbackInfo ci) {
        if (minecraft.getCameraEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity.hurtTime != 0) {
                DamageSource damageSource = livingEntity.getLastDamageSource();
                if (damageSource != null && damageSource.is(SonaTags.NO_SHAKE)) {
                    ci.cancel();
                }
            }
        }
    }

}
