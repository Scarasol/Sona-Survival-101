package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.scarasol.sona.client.renderer.AlphaVertexConsumer;
import com.scarasol.sona.client.renderer.CamouflageRenderUtil;
import com.scarasol.sona.client.renderer.SonaRenderType;
import com.scarasol.sona.compat.ShaderCompatUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
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
            // 暂时移除，防止后续 this.getBuffer(targetType) 触发无限递归
            SonaRenderType.camoAlpha.remove();

            try {
                // 1. 安全放行非多边形渲染 (线条、碰撞箱、部分特殊激光等)，直接绕过包装
                if (renderType.mode() != VertexFormat.Mode.QUADS && renderType.mode() != VertexFormat.Mode.TRIANGLES) {
                    cir.setReturnValue(this.getBuffer(renderType));
                    return;
                }

                VertexFormat format = renderType.format();
                boolean isShaderActive = ModList.get().isLoaded("oculus") && ShaderCompatUtil.isShaderActive();
                RenderType targetType = renderType;
                boolean shouldWrap = false;

                // 2. 方块模型 (原版基础物品兜底：手持普通方块、掉落物等)
                if (format == DefaultVertexFormat.BLOCK) {
                    // affectsCrumbling 属性在基础图层(Solid, Cutout)为 true，破坏动画层为 false
                    if (renderType.affectsCrumbling()) {
                        targetType = isShaderActive ? RenderType.translucent() : SonaRenderType.itemDither();
                        shouldWrap = true;
                    }
                }

                else if (format == DefaultVertexFormat.NEW_ENTITY) {
                    if (renderType instanceof CompositeRenderTypeAccessor accessor) {
                        RenderType.CompositeState state = accessor.sona$getState();
                        RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor) (Object) state).sona$getTextureState();

                        if (textureState instanceof TextureStateShardAccessor texAccessor) {
                            Optional<ResourceLocation> optTexture = texAccessor.sona$getTexture();
                            if (optTexture.isPresent()) {
                                ResourceLocation texture = optTexture.get();
                                // 强制回退为支持 Alpha 的半透明渲染或原版抖动渲染
                                targetType = isShaderActive ? RenderType.entityTranslucent(texture) : SonaRenderType.entityDither(texture);
                                shouldWrap = true;
                            }
                        }
                    }
                }
                // 情景 C: 其他格式 (POSITION_TEX为附魔发光，POSITION_COLOR为文字/闪电等) 自动放行

                // 4. 应用包装逻辑
                if (shouldWrap) {
                    cir.setReturnValue(new AlphaVertexConsumer(this.getBuffer(targetType), CamouflageRenderUtil.itemAlpha(alpha)));
                } else {
                    cir.setReturnValue(this.getBuffer(renderType)); // 原样返回
                }

            } finally {
                // 恢复 ThreadLocal，以便调用栈中更上层的代码继续使用
                SonaRenderType.camoAlpha.set(alpha);
            }
        }
    }
}
