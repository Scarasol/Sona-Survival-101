package com.scarasol.sona.mixin.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.client.renderer.SonaRenderType;
import com.scarasol.sona.compat.geckolib.GeoInfectionLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
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

    @Shadow(remap = false) public abstract GeoEntityRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer);

    protected GeoEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true, at = @At("HEAD"))
    private void sona$preRender(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {

        if (entity instanceof ILivingEntityAccessor livingEntityAccessor) {
            int amplifier = livingEntityAccessor.getCamouflageAmplifier();

            if (amplifier > 0) {
                float alpha = livingEntityAccessor.getCamouflageAlpha();
                if (alpha <= 0) {
                    ci.cancel();
                } else if (alpha < 1) {
                    // 【修改点】：不再强行修改着色器颜色，而是把 alpha 存入线程。
                    // 因为 BufferSourceMixin 已经能够处理 GeckoLib 的复合渲染类型了！
                    SonaRenderType.camoAlpha.set(alpha);
                }
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void sona$postRender(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        // 清理线程状态，确保不污染其他渲染
        SonaRenderType.camoAlpha.remove();
    }

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lsoftware/bernie/geckolib/model/GeoModel;)V", at = @At("TAIL"), remap = false)
    private void sona$geoEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel model, CallbackInfo ci) {
        addRenderLayer(new GeoInfectionLayer<>(this));
    }
}
