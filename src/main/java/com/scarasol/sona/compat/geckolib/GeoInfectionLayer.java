package com.scarasol.sona.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.client.layer.InfectionLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * @author Scarasol
 */
public class GeoInfectionLayer<T extends Entity & GeoAnimatable> extends GeoRenderLayer<T> {

    public GeoInfectionLayer(GeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!animatable.isInvisible()) {
            if (animatable instanceof ILivingEntityAccessor livingEntityAccessor) {

                if (livingEntityAccessor.getInfectionLayer()) {

                    VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucent(InfectionLayer.TEXTURE));
                    getRenderer().actuallyRender(
                            poseStack,
                            animatable,
                            bakedModel,
                            RenderType.entityTranslucent(InfectionLayer.TEXTURE),
                            bufferSource,
                            vertexconsumer,
                            false,
                            partialTick,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            1f, 1f, 1f, livingEntityAccessor.getCamouflageAlpha()
                    );
                }
            }
        }
    }
}
