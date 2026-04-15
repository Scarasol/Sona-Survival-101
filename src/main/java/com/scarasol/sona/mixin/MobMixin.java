package com.scarasol.sona.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import javax.annotation.Nullable;
 

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    @Shadow @Nullable public abstract LivingEntity getTarget();

    @Shadow public abstract Sensing getSensing();

    @Shadow public abstract void setTarget(@Nullable LivingEntity p_21544_);

    @Shadow public abstract PathNavigation getNavigation();
    @Unique private boolean lostTarget;

    @Unique private double lostX;
    @Unique private double lostY;
    @Unique private double lostZ;
    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci){
        if (!CommonConfig.ENHANCED_CAMOUFLAGE.get())
            return;
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && target.hasEffect(SonaMobEffects.CAMOUFLAGE.get()) && !target.hasEffect(SonaMobEffects.EXPOSURE.get())){
            if (!this.getSensing().hasLineOfSight(target)){
                this.setLastHurtByPlayer(null);
                this.setLastHurtByMob(null);
                this.setTarget(null);
                lostTarget = true;
                lostX = target.getX();
                lostY = target.getY();
                lostZ = target.getZ();
            }
        }
        if (lostTarget && getNavigation().isDone()){
            this.getNavigation().moveTo(lostX, lostY, lostZ, 1);
            lostTarget = false;
        }
    }

    @Inject(method = "isSunBurnTick", cancellable = true, at = @At("RETURN"))
    private void sona$isSunBurnTick(CallbackInfoReturnable<Boolean> cir) {
        Level level = level();
        if (cir.getReturnValue() && InfectionManager.canChunkInfection(level) && InfectionManager.getZoneInfection(level, blockPosition(), false) > 75) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;tickRunningGoals(Z)V", ordinal = 1))
    private void sona$goalSelectorTickRunningGoals(net.minecraft.world.entity.ai.goal.GoalSelector goalSelector, boolean only, Operation<Void> voidOperation) {
        if (!hasEffect(SonaMobEffects.STUN.get())) {
            voidOperation.call(goalSelector, only);
        }
    }

    @WrapOperation(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;tick()V", ordinal = 1))
    private void sona$goalSelectorTick(net.minecraft.world.entity.ai.goal.GoalSelector goalSelector, Operation<Void> voidOperation) {
        if (!hasEffect(SonaMobEffects.STUN.get())) {
            voidOperation.call(goalSelector);
        }
    }
}
