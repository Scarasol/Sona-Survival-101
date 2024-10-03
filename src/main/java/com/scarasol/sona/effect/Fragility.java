package com.scarasol.sona.effect;

import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Fragility extends MobEffectBase{

    private static final UUID FRAGILITY_UUID = UUID.fromString("26A86654-DBE6-BC31-734E-E0E433873636");

    public Fragility() {
        super(MobEffectCategory.HARMFUL, -7864320);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        double level = Math.max(entity.hasEffect(SonaMobEffects.FRAGILITY.get()) ? entity.getEffect(SonaMobEffects.FRAGILITY.get()).getAmplifier() : 0, amplifier) + 1;
        double addition = -1 * ((0.15 * level) / (0.15 * level + 1)) * 20;
        AttributeModifier attributeModifier = new AttributeModifier(FRAGILITY_UUID, getDescriptionId(), addition, AttributeModifier.Operation.ADDITION);
        AttributeInstance attributeInstance = attributeMap.getInstance(Attributes.ARMOR);
        if (attributeInstance != null){
            attributeInstance.removeModifier(FRAGILITY_UUID);
            attributeInstance.addPermanentModifier(attributeModifier);
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        AttributeInstance attributeInstance = attributeMap.getInstance(Attributes.ARMOR);
        if (attributeInstance != null){
            attributeInstance.removeModifier(FRAGILITY_UUID);
        }
    }

}
