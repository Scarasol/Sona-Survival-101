package com.scarasol.sona.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.event.SonaEventHooks;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsMixin {

    @Shadow
    private double range;

    @WrapOperation(method = "test", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean warpIsAlliedTo(LivingEntity instance, Entity target, Operation<Boolean> operation) {
        if (instance.hasEffect(SonaMobEffects.INSANE.get())) {
            return false;
        }
        return operation.call(instance, target);
    }

    @Inject(method = "test", at = @At("RETURN"), cancellable = true)
    private void sona$neutralityTargeting(@Nullable LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || this.range <= 0) {
            return;
        }
        if (!(attacker instanceof Mob mob)) {
            return;
        }
        if (!SonaEventHooks.shouldCheckNeutrality(mob, target)) {
            return;
        }
        if (SonaEventHooks.shouldBlockNeutralityTargetGoal(mob, mob.level(), target)) {
            cir.setReturnValue(false);
        }
    }
}
