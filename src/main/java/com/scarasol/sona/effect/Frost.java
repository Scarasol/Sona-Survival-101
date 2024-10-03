package com.scarasol.sona.effect;

import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.ItemStack;

public class Frost extends PhysicalEffect{

    public Frost() {
        super(MobEffectCategory.HARMFUL, -16711681);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)){
            entity.removeEffect(SonaMobEffects.FROST.get());
        }else {
            if (entity.hasEffect(SonaMobEffects.IGNITION.get())){
                int level = entity.getEffect(SonaMobEffects.IGNITION.get()).getAmplifier();
                int duration = entity.getEffect(SonaMobEffects.IGNITION.get()).getDuration();
                entity.removeEffect(SonaMobEffects.IGNITION.get());
                entity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), duration, (int) ((level + amplifier) / 2), false, false));
            }
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isOnFire()){
            entity.setTicksFrozen(0);
            int duration = entity.getEffect(SonaMobEffects.FROST.get()).getDuration();
            entity.removeEffect(SonaMobEffects.FROST.get());
            entity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), duration, amplifier, false, false));
            return;
        }
        float freezeImmune = equipmentFreezeImmune(entity);
        entity.setTicksFrozen(entity.getTicksFrozen() + (int) ((amplifier + 3) * freezeImmune));
        if (entity.isFullyFrozen()){
            int frozenTime = entity.getEffect(SonaMobEffects.FROST.get()).getDuration();
            if (frozenTime % 10 == 0){
                entity.hurt(DamageSource.FREEZE, (entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES) ? (5 + amplifier) / 2 : (1 + amplifier) / 2 ));
            }
        }
        if (entity.hasEffect(SonaMobEffects.SLIMINESS.get())){
            if(freezeImmune != 0 && !entity.isFullyFrozen()){
                entity.setTicksFrozen(entity.getTicksRequiredToFreeze());
            }
        }
    }

    public float equipmentFreezeImmune(LivingEntity entity){
        float exposed = 0;
        ItemStack[] armors = {entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST), entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET)};
        for (ItemStack armor : armors){
            if (armor.isEmpty()){
                exposed += 0.25;
            }else if(!armor.is(ItemTags.FREEZE_IMMUNE_WEARABLES)){
                exposed += 0.125;
            }
        }
        return exposed;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
