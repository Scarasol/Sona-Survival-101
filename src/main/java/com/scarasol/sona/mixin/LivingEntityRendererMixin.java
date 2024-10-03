package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "isShaking", cancellable = true, at = @At("RETURN"))
    protected void isShaking(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof ILivingEntityAccessor survivalEntity){
            cir.setReturnValue(livingEntity.isFullyFrozen() || (InfectionManager.getInfection(survivalEntity) > 70 && CommonConfig.INFECTION_OPEN.get()));
        }
    }

}
