package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.client.renderer.AlphaVertexConsumer;
import com.scarasol.sona.client.renderer.CamouflageRenderUtil;
import com.scarasol.sona.client.renderer.SonaRenderType;
import com.scarasol.sona.compat.ShaderCompatUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * @author Scarasol
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private void sona$wrapMultiBufferSource(EntityRenderer instance, Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Operation<Void> original) {
        if (!(entity instanceof LivingEntity) || !(entity instanceof ILivingEntityAccessor accessor)) {
            original.call(instance, entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
            return;
        }

        float alpha = accessor.getCamouflageAlpha();
        if (alpha >= 1.0f) {
            original.call(instance, entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
            return;
        }

        boolean isShaderActive = ModList.get().isLoaded("oculus") && ShaderCompatUtil.isShaderActive();

        MultiBufferSource finalSource = renderType -> {
            // 1. 安全放行非多边形渲染 (线条、碰撞箱等)
            if (renderType.mode() != VertexFormat.Mode.QUADS && renderType.mode() != VertexFormat.Mode.TRIANGLES) {
                return bufferSource.getBuffer(renderType);
            }

            // 2. 核心：通过 VertexFormat 物理内存布局分类
            VertexFormat format = renderType.format();
            RenderType targetType = renderType;
            boolean shouldWrap = false;

            // 情景 A：方块模型 (原版手持普通物品、掉落物等)
            if (format == DefaultVertexFormat.BLOCK) {
                // affectsCrumbling 属性在基础图层(Solid, Cutout)为 true，破坏动画层为 false
                if (renderType.affectsCrumbling()) {
                    targetType = isShaderActive ? RenderType.translucent() : SonaRenderType.itemDither();
                    shouldWrap = true;
                }
            }
            // 情景 B：实体模型 (生物主模型、盔甲层、TACZ等独立模型枪械)
            else if (format == DefaultVertexFormat.NEW_ENTITY) {
                if (renderType instanceof CompositeRenderTypeAccessor accessorRender) {
                    RenderType.CompositeState state = accessorRender.sona$getState();
                    RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor) (Object) state).sona$getTextureState();

                    if (textureState instanceof TextureStateShardAccessor texAccessor) {
                        Optional<ResourceLocation> optTexture = texAccessor.sona$getTexture();
                        if (optTexture.isPresent()) {
                            ResourceLocation texture = optTexture.get();
                            targetType = isShaderActive ? RenderType.entityTranslucent(texture) : SonaRenderType.entityDither(texture);
                            shouldWrap = true;
                        }
                    }
                }
            }
            // 情景 C：其他格式 (POSITION_TEX为附魔发光，POSITION_COLOR为闪电，文字等)
            // 这些图层全部不属于 BLOCK 或 NEW_ENTITY，会直接忽略执行包装代码，杜绝渲染管线报错

            if (shouldWrap) {
                return new AlphaVertexConsumer(bufferSource.getBuffer(targetType), CamouflageRenderUtil.itemAlpha(alpha));
            }

            return bufferSource.getBuffer(renderType);
        };

        original.call(instance, entity, entityYaw, partialTicks, poseStack, finalSource, packedLight);
    }
}
