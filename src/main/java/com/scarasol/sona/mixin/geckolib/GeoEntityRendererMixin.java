package com.scarasol.sona.mixin.geckolib;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.compat.geckolib.GeoInfectionLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * @author Scarasol
 */
@Pseudo
@Mixin(value = GeoEntityRenderer.class)
public abstract class GeoEntityRendererMixin<T extends Entity & GeoAnimatable> extends EntityRenderer<T> implements GeoRenderer<T> {

    @Shadow public abstract GeoEntityRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer);

    protected GeoEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true, at = @At("HEAD"))
    private void sona$preRender(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {

        if (entity instanceof ILivingEntityAccessor livingEntityAccessor) {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            int amplifier = livingEntityAccessor.getCamouflageAmplifier();

            if (amplifier > 0) {
                float alpha = livingEntityAccessor.getCamouflageAlpha();
                if (alpha == 0) {
                    ci.cancel();
                } else if (alpha < 1) {
                    // 只向着色器传递颜色和 alpha，由着色器执行丢弃像素，不再开启 Blend
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
                }
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void sona$postRender(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // 上面移除了 enableBlend()，这里也移除了 disableBlend()
    }

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lsoftware/bernie/geckolib/model/GeoModel;)V", at = @At("TAIL"))
    private void sona$geoEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel model, CallbackInfo ci) {
        addRenderLayer(new GeoInfectionLayer(this));
    }
}