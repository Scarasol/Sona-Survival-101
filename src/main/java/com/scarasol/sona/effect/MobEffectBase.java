package com.scarasol.sona.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MobEffectBase extends MobEffect {
    public MobEffectBase(MobEffectCategory mobEffectCategory, int integer) {
        super(mobEffectCategory, integer);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
