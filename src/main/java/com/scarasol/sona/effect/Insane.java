package com.scarasol.sona.effect;

import com.google.common.collect.Maps;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Insane extends MobEffectBase{

    private Map<Mob, NearestAttackableTargetGoal<LivingEntity>> crazyAttackGoals = Maps.newHashMap();

    public Insane() {
        super(MobEffectCategory.HARMFUL, -6750208);
        addAttributeModifier(Attributes.ARMOR, "075E5ECF-239F-3938-0696-DF8B9DD0103A", -0.8, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "FB1FB9B8-EE06-4D7F-864B-3FB74D9576B4", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "857C0BCD-69C7-96BB-B5EF-E6D414BFB83B", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!crazyAttackGoals.containsKey(entity) && entity instanceof Mob mob){
            NearestAttackableTargetGoal<LivingEntity> crazyAttackGoal = new NearestAttackableTargetGoal<LivingEntity>(mob, LivingEntity.class, 5, false, false, livingEntity -> !livingEntity.equals(mob));
            mob.targetSelector.addGoal(1, crazyAttackGoal);
            mob.setTarget(null);
            crazyAttackGoals.put(mob, crazyAttackGoal);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Mob mob){
            mob.targetSelector.removeGoal(crazyAttackGoals.get(mob));
            crazyAttackGoals.remove(mob);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
