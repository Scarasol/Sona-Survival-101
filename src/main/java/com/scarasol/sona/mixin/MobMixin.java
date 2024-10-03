package com.scarasol.sona.mixin;

import com.scarasol.sona.init.SonaMobEffects;
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
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && target.hasEffect(SonaMobEffects.CAMOUFLAGE.get())){
            if (!this.getSensing().hasLineOfSight(target)){
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
}
