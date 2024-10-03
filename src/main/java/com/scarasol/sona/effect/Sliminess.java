package com.scarasol.sona.effect;

import com.google.common.collect.Maps;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class Sliminess extends PhysicalEffect{

    private final Map<Attribute, AttributeModifier> frostWithSliminess = Maps.newHashMap();

    public Sliminess() {
        super(MobEffectCategory.HARMFUL, -6684826);
        init();
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "4C58056C-6E6A-69F6-BC86-7934D2A41BBA", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.FLYING_SPEED, "6649E638-E828-69DD-C237-2489AE193B2B", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, "5D05E967-9A81-35BF-F71A-AE39A54E3E0E", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public void init(){
        AttributeModifier attributeModifier1 = new AttributeModifier(UUID.fromString("82EF1D2B-DC2F-467D-2A96-222110ED9B28"), getDescriptionId(), -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        AttributeModifier attributeModifier2 = new AttributeModifier(UUID.fromString("21E2DD92-E71D-B4C6-5D02-ED617A8E5B23"), getDescriptionId(), -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        frostWithSliminess.put(Attributes.MOVEMENT_SPEED, attributeModifier1);
        frostWithSliminess.put(Attributes.ATTACK_DAMAGE, attributeModifier2);
    }

    public void removeFrozen(AttributeMap attributeMap, int n) {
        for (Map.Entry<Attribute, AttributeModifier> entry : this.frostWithSliminess.entrySet()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue());
        }
    }

    public void AddFrozen(AttributeMap attributeMap, int n) {
        for (Map.Entry<Attribute, AttributeModifier> entry : this.frostWithSliminess.entrySet()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            AttributeModifier attributeModifier = entry.getValue();
            attributeInstance.removeModifier(attributeModifier);
            attributeInstance.addPermanentModifier(new AttributeModifier(attributeModifier.getId(), this.getDescriptionId() + " " + n, this.getAttributeModifierValue(n, attributeModifier), attributeModifier.getOperation()));
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isInPowderSnow || entity.wasInPowderSnow || entity.hasEffect(SonaMobEffects.FROST.get())){
            AddFrozen(entity.getAttributes(), 0);
        }else {
            removeFrozen(entity.getAttributes(), 0);
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        removeFrozen(attributeMap, 0);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "effect.sona.sliminess";
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
