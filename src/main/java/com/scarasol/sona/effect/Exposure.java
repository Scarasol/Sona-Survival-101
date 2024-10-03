package com.scarasol.sona.effect;

import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class Exposure extends PhysicalEffect{
    public Exposure() {
        super(MobEffectCategory.HARMFUL, -3407872);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.removeEffect(SonaMobEffects.CAMOUFLAGE.get());
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
