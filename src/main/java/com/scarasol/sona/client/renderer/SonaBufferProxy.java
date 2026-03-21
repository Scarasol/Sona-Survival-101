package com.scarasol.sona.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.sona.compat.ShaderCompatUtil;
import com.scarasol.sona.mixin.CompositeRenderTypeAccessor;
import com.scarasol.sona.mixin.CompositeStateAccessor;
import com.scarasol.sona.mixin.TextureStateShardAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 为手持物等可能被其他模组替换过的 BufferSource 提供透明度代理。
 *
 * <p>Oculus 安装后，手持物渲染经常不会走 {@code MultiBufferSource.BufferSource}，
 * 因此单纯 mixin 原版 BufferSource 的 getBuffer 无法稳定拦截。</p>
 *
 * @author Scarasol
 */
public class SonaBufferProxy implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final float alpha;

    public SonaBufferProxy(MultiBufferSource delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (alpha >= 1.0f) {
            return delegate.getBuffer(renderType);
        }

        String typeName = renderType.toString();

        if (typeName.contains("glint") || typeName.contains("beam") || typeName.contains("lightning")
                || typeName.contains("lines") || typeName.contains("text") || typeName.contains("crumbling")) {
            return delegate.getBuffer(renderType);
        }

        boolean isShaderActive = ShaderCompatUtil.isShaderActive();
        RenderType targetType = renderType;

        try {
            if (renderType == RenderType.solid() || renderType == RenderType.cutout()
                    || renderType == RenderType.cutoutMipped() || renderType == RenderType.translucent()) {
                targetType = isShaderActive ? RenderType.translucent() : SonaRenderType.itemDither();
            } else if (renderType instanceof CompositeRenderTypeAccessor accessor) {
                RenderType.CompositeState state = accessor.sona$getState();
                RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor) (Object) state).sona$getTextureState();

                if (textureState instanceof TextureStateShardAccessor texAccessor) {
                    Optional<ResourceLocation> optTexture = texAccessor.sona$getTexture();
                    if (optTexture.isPresent()) {
                        ResourceLocation texture = optTexture.get();
                        if (!texture.getPath().contains("enchanted_item_glint")) {
                            targetType = isShaderActive ? RenderType.entityTranslucent(texture) : SonaRenderType.entityDither(texture);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return new AlphaVertexConsumer(delegate.getBuffer(targetType), alpha);
    }
}
