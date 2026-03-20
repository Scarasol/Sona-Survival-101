package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.sona.client.renderer.AlphaVertexConsumer;
import com.scarasol.sona.client.renderer.SonaRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * @author Scarasol
 */
@Mixin(MultiBufferSource.BufferSource.class)
public abstract class BufferSourceMixin {

    @Shadow public abstract VertexConsumer getBuffer(RenderType p_109919_);

    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true)
    private void sona$globalBufferIntercept(RenderType renderType, CallbackInfoReturnable<VertexConsumer> cir) {
        Float alpha = SonaRenderType.camoAlpha.get();
        if (alpha != null && alpha < 1.0f) {
            SonaRenderType.camoAlpha.remove();

            VertexConsumer wrappedConsumer = null;
            String typeName = renderType.toString();

            try {
                // 1. 尝试拦截 TACZ、GeckoLib 动态生成的带有专属贴图的 RenderType
                if (renderType instanceof CompositeRenderTypeAccessor accessor) {
                    RenderType.CompositeState state = accessor.sona$getState();
                    RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor) (Object) state).sona$getTextureState();

                    if (textureState instanceof TextureStateShardAccessor texAccessor) {
                        Optional<ResourceLocation> optTexture = texAccessor.sona$getTexture();
                        if (optTexture.isPresent()) {
                            ResourceLocation texture = optTexture.get();

                            // 安全过滤 1：排除附魔发光层
                            // 安全过滤 2：排除 TACZ 的准星、激光等纯色发光材质（通常带有 "light" 或 "beam" 等关键字）
                            if (!texture.getPath().contains("enchanted_item_glint") && !typeName.contains("beam") && !typeName.contains("lightning") && !typeName.contains("lines")) {
                                RenderType ditherType = SonaRenderType.entityDither(texture);
                                wrappedConsumer = new AlphaVertexConsumer(this.getBuffer(ditherType), alpha);
                            }
                        }
                    }
                }

                if (wrappedConsumer == null) {
                    // 2. 原版基础物品兜底 (使用全局方块图集)
                    if (renderType == RenderType.solid() || renderType == RenderType.cutout() ||
                            renderType == RenderType.cutoutMipped() || renderType == RenderType.translucent()) {

                        wrappedConsumer = new AlphaVertexConsumer(this.getBuffer(SonaRenderType.itemDither()), alpha);
                    }
                    // 3. 【精确白名单过滤】只有在名字中包含实体、护甲、方块模型等核心标识时，才允许包装
                    else if (typeName.contains("entity") || typeName.contains("armor") || typeName.contains("item") || typeName.contains("cutout") || typeName.contains("translucent")) {

                        // 过滤掉极其危险的特殊渲染器（例如：闪电 lightning、连线 lines、文字 text、发光方块边框、激光 beam）
                        if (!typeName.contains("lines") && !typeName.contains("lightning") && !typeName.contains("text") && !typeName.contains("beam") && !typeName.contains("glint") && !typeName.contains("crumbling")) {
                            wrappedConsumer = new AlphaVertexConsumer(this.getBuffer(renderType), alpha);
                        }
                    }

                    // 4. 如果上面的条件全都不满足（即特殊线条、激光、UI文字等），直接原样返回，彻底绕过包装器
                    if (wrappedConsumer == null) {
                        wrappedConsumer = this.getBuffer(renderType);
                    }
                }

            } finally {
                SonaRenderType.camoAlpha.set(alpha);
            }

            cir.setReturnValue(wrappedConsumer);
        }
    }
}