package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.event.SonaEventHooks;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(TargetGoal.class)
public abstract class TargetGoalMixin extends Goal {

    @Shadow @Nullable protected LivingEntity targetMob;

    @Shadow @Final protected Mob mob;

    @Inject(method = "canContinueToUse", cancellable = true, at = @At("RETURN"))
    private void onCanContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if (cir.getReturnValue()
                && !this.mob.hasEffect(SonaMobEffects.INSANE.get())
                && !((Object) this instanceof HurtByTargetGoal)
                && !((Object) this instanceof OwnerHurtByTargetGoal)
                && !((Object) this instanceof OwnerHurtTargetGoal)) {
            LivingEntity entity = this.mob.getTarget();
            if (entity == null) {
                entity = this.targetMob;
            }
            if (entity != null) {
                if (SonaEventHooks.shouldCheckNeutrality(this.mob, entity)
                        && SonaEventHooks.shouldBlockNeutralityTargetGoal(this.mob, this.mob.level(), entity)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        if (!CommonConfig.ENHANCED_CAMOUFLAGE.get()) {
             return;
        }
        if (cir.getReturnValue()){
            LivingEntity entity = this.targetMob;
            if (entity == null) {
                entity = this.mob.getTarget();
            }
            if (entity != null && entity.isAlive() && entity.hasEffect(SonaMobEffects.CAMOUFLAGE.get()) && !entity.hasEffect(SonaMobEffects.EXPOSURE.get())){
                if (!this.mob.getSensing().hasLineOfSight(entity)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

}
