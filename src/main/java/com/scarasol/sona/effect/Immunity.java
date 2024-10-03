package com.scarasol.sona.effect;

import com.scarasol.sona.init.SonaDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class Immunity extends MobEffectBase{
    public Immunity() {
        super(MobEffectCategory.BENEFICIAL, -16711732);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (amplifier >= 3){
            entity.hurt(SonaDamageTypes.damageSource(entity.level().registryAccess(), SonaDamageTypes.IMMUNITY), 999999);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
