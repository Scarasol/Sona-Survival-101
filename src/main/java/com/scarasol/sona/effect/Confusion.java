package com.scarasol.sona.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Confusion extends MobEffectBase{

    public Confusion() {
        super(MobEffectCategory.HARMFUL, -256);
        addAttributeModifier(Attributes.FOLLOW_RANGE, "F35874C4-67AB-5BAF-EFD2-1392D0E77874", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
