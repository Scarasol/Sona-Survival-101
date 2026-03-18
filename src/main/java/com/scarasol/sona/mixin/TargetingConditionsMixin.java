package com.scarasol.sona.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsMixin {

    @WrapOperation(method = "test", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean warpIsAlliedTo(LivingEntity instance, Entity target, Operation<Boolean> operation) {
        if (instance.hasEffect(SonaMobEffects.INSANE.get())) {
            return false;
        }
        return operation.call(instance, target);
    }
}
