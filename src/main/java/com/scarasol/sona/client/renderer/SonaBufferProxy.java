//package com.scarasol.sona.client.renderer;
//
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.scarasol.sona.compat.ShaderCompatUtil;
//import com.scarasol.sona.mixin.CompositeRenderTypeAccessor;
//import com.scarasol.sona.mixin.CompositeStateAccessor;
//import com.scarasol.sona.mixin.TextureStateShardAccessor;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderStateShard;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.Optional;
//
//public class SonaBufferProxy implements MultiBufferSource {
//    private final MultiBufferSource delegate;
//    private final float alpha;
//
//    public SonaBufferProxy(MultiBufferSource delegate, float alpha) {
//        this.delegate = delegate;
//        this.alpha = alpha;
//    }
//
//    @Override
//    public VertexConsumer getBuffer(RenderType renderType) {
//        String typeName = renderType.toString();
//        boolean isShaderActive = ShaderCompatUtil.isShaderActive();
//        RenderType targetType = renderType;
//
//        // 1. 过滤危险类型（附魔发光层、连线、闪电、UI等），防止渲染崩溃
//        if (typeName.contains("glint") || typeName.contains("beam") || typeName.contains("lightning") || typeName.contains("lines") || typeName.contains("text")) {
//            return delegate.getBuffer(renderType);
//        }
//
//        try {
//            // 2. 原版基础物品兜底 (方块/普通手持物品)
//            if (renderType == RenderType.solid() || renderType == RenderType.cutout() ||
//                    renderType == RenderType.cutoutMipped() || renderType == RenderType.translucent()) {
//                targetType = isShaderActive ? RenderType.translucent() : SonaRenderType.itemDither();
//            }
//            // 3. 动态提取贴图的复合类型（盔甲、TACZ枪械等）
//            else if (renderType instanceof CompositeRenderTypeAccessor accessor) {
//                RenderType.CompositeState state = accessor.sona$getState();
//                RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor) (Object) state).sona$getTextureState();
//
//                if (textureState instanceof TextureStateShardAccessor texAccessor) {
//                    Optional<ResourceLocation> optTexture = texAccessor.sona$getTexture();
//                    if (optTexture.isPresent()) {
//                        ResourceLocation texture = optTexture.get();
//                        if (!texture.getPath().contains("enchanted_item_glint")) {
//                            targetType = isShaderActive ? RenderType.entityTranslucent(texture) : SonaRenderType.entityDither(texture);
//                        }
//                    }
//                }
//            }
//            // 4. 如果已经被主模型处理过，直接保留
//            else if (typeName.contains("dither")) {
//                targetType = renderType;
//            }
//        } catch (Exception ignored) {}
//
//        // 套上包装器，这步是让一切变透明的关键
//        return new AlphaVertexConsumer(delegate.getBuffer(targetType), alpha);
//    }
//}