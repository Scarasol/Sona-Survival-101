package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Unique
    private float sona$RenderTranslucent = 1f;


    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    private void sona$renderArmorPre(
            PoseStack poseStack,                 // 渲染矩阵堆栈
            MultiBufferSource bufferSource,      // 顶点缓冲源
            int packedLight,                     // 光照值
            T entity,                            // 当前生物实体
            float limbSwing,                     // 四肢摆动进度
            float limbSwingAmount,               // 四肢摆动幅度
            float partialTicks,                  // 渲染部分刻
            float ageInTicks,                    // 实体存在的总刻数，用于动画
            float netHeadYaw,                    // 头部水平旋转角度
            float headPitch,                     // 头部上下旋转角度
            CallbackInfo ci
    ) {
        if (entity instanceof ILivingEntityAccessor livingEntityAccessor) {

            sona$RenderTranslucent = livingEntityAccessor.getCamouflageAlpha();
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("TAIL"))
    private void sona$renderArmorPost(
            PoseStack poseStack,                 // 渲染矩阵堆栈
            MultiBufferSource bufferSource,      // 顶点缓冲源
            int packedLight,                     // 光照值
            T entity,                            // 当前生物实体
            float limbSwing,                     // 四肢摆动进度
            float limbSwingAmount,               // 四肢摆动幅度
            float partialTicks,                  // 渲染部分刻
            float ageInTicks,                    // 实体存在的总刻数，用于动画
            float netHeadYaw,                    // 头部水平旋转角度
            float headPitch,                     // 头部上下旋转角度
            CallbackInfo ci
    ) {
        sona$RenderTranslucent = 1f;
    }

    @Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V", remap = false, cancellable = true, at = @At("HEAD"))
    private void sona$renderModel(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            ArmorItem armorItem,
            Model armorModel,
            boolean withGlint,
            float red,
            float green,
            float blue,
            ResourceLocation armorTexture,
            CallbackInfo ci
    ) {
        if (sona$RenderTranslucent < 1) {
            if (armorTexture != null) {
                VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucent(armorTexture));
                armorModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, sona$RenderTranslucent);
                ci.cancel();
            }
        }
    }
}
