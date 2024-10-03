package com.scarasol.sona.effect;

import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class Ignition extends PhysicalEffect{

    public Ignition() {
        super(MobEffectCategory.HARMFUL, -6750208);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        if (entity.fireImmune()){
            entity.removeEffect(SonaMobEffects.IGNITION.get());
        }else {
            if (entity.hasEffect(SonaMobEffects.FROST.get())){
                int level = entity.getEffect(SonaMobEffects.FROST.get()).getAmplifier();
                int duration = entity.getEffect(SonaMobEffects.FROST.get()).getDuration();
                entity.removeEffect(SonaMobEffects.FROST.get());
                entity.setTicksFrozen(0);
                entity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), duration, (int) ((level + amplifier) / 2), false, false));
            }
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        int ignitedTime = entity.getEffect(SonaMobEffects.IGNITION.get()).getDuration();
        boolean burnUnderWater = entity.hasEffect(SonaMobEffects.SLIMINESS.get());
        if (entity.isInWaterRainOrBubble()){
            if (burnUnderWater){
                entity.setSharedFlagOnFire(false);
                if (entity.level() instanceof ServerLevel server && ignitedTime % 10 == 0){
                    server.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 20, 0, 0.8, 0, 0.01);
                }
            }else {
                entity.removeEffect(SonaMobEffects.IGNITION.get());
                return;
            }
        }else if (entity.isFreezing()){
            if (burnUnderWater){
                breakPowderSnow(entity.level(), BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()));
                breakPowderSnow(entity.level(), BlockPos.containing(entity.getX(), entity.getY() + 1, entity.getZ()));
            } else{
                entity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), ignitedTime, amplifier, false, false));
                entity.removeEffect(SonaMobEffects.IGNITION.get());
                return;
            }
        }else {
            entity.setRemainingFireTicks(5);
            entity.setSharedFlagOnFire(!entity.fireImmune());
        }
        if (ignitedTime % 20 == 0){
            entity.hurt(entity.level().damageSources().inFire(), amplifier + 1);
        }
    }

    public void breakPowderSnow(Level world, BlockPos pos){
        if (world.getBlockState(pos).getBlock() == Blocks.POWDER_SNOW){
            world.destroyBlock(pos, false);
        }
    }


    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
