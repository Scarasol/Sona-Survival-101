package com.scarasol.sona.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Scarasol
 */
public class InfectionLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {


    public static final ResourceLocation TEXTURE = new ResourceLocation("sona:textures/entity/layer/spore_layer.png");

    public InfectionLayer(RenderLayerParent<T, M> layerParent) {
        super(layerParent);
    }

    @Override
    public void render(PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight,
                       T livingEntity,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {
        if (!livingEntity.isInvisible()) {
            if (livingEntity instanceof ILivingEntityAccessor livingEntityAccessor) {

                if (livingEntityAccessor.getInfectionLayer()) {

                    VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
                    this.getParentModel().renderToBuffer(
                            poseStack,
                            vertexconsumer,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            1.0F, 1.0F, 1.0F, livingEntityAccessor.getCamouflageAlpha()
                    );
                }
            }
        }





    }

}
