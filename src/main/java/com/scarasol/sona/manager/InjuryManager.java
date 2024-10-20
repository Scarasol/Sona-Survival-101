package com.scarasol.sona.manager;

import com.scarasol.sona.SonaMod;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaDamageTypes;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;


public class InjuryManager {

    public static float getInjury(ILivingEntityAccessor livingEntity) {
        return livingEntity.getInjuryLevel();
    }

    public static void setInjury(ILivingEntityAccessor livingEntity, float injury) {
        livingEntity.setInjuryLevel(injury);
        livingEntity.setBandageLevel(Math.min(getBandage(livingEntity), 100 - injury));
    }

    public static void addInjury(ILivingEntityAccessor livingEntity, float addition) {
        if (addition < 0)
            addition = addition * CommonConfig.INJURY_WEIGHT.get().floatValue();
        addActualInjury(livingEntity, addition);
    }

    public static void addActualInjury(ILivingEntityAccessor livingEntity, float addition) {
        float injury = getInjury(livingEntity) + addition;
        if (addition > 0) {
            livingEntity.setInjuryLevel(Math.min(100, injury));
            livingEntity.setBandageLevel(Math.min(getBandage(livingEntity), 100 - injury));
        } else {
            livingEntity.setInjuryLevel(Math.max(0, injury));
        }
    }

    public static float getBandage(ILivingEntityAccessor livingEntity) {
        return livingEntity.getBandageLevel();
    }

    private static void setBandage(ILivingEntityAccessor livingEntity, float bandage) {
        livingEntity.setBandageLevel(bandage);
    }

    public static void setBandageSafe(ILivingEntityAccessor livingEntity, float bandage) {
        bandage = Math.max(0, Math.min(Math.min(bandage, 50), 100 - getInjury(livingEntity)));
        setBandage(livingEntity, bandage);
    }

    public static void addBandage(ILivingEntityAccessor livingEntity, float addition) {
        if (addition < 0)
            addition = addition * CommonConfig.INJURY_WEIGHT.get().floatValue();
        addActualBandage(livingEntity, addition);
    }

    public static void addActualBandage(ILivingEntityAccessor livingEntity, float addition){
        float injury = getInjury(livingEntity);
        float bandage = getBandage(livingEntity);
        if (addition > 0) {
            if (injury > 50) {
                bandage = Math.min(100 - injury, bandage + addition);
            } else {
                bandage = Math.min(50, bandage + addition);
            }
        } else {
            bandage = Math.max(0, bandage + addition);
        }
        setBandage(livingEntity, bandage);
    }

    public static void init(ILivingEntityAccessor newPlayer, ILivingEntityAccessor oldPlayer){
        setInjury(newPlayer, Math.max(CommonConfig.INJURY_INITIAL_VALUE.get().floatValue(), oldPlayer.getInjuryLevel()));
        setBandage(newPlayer, 0);
    }

    public static void injuryTick(LivingEntity livingEntity) {

        if (livingEntity.level().isClientSide())
            return;
        Level level = livingEntity.level();
        if (livingEntity instanceof ILivingEntityAccessor livingEntityAccessor){
            float injury = getInjury(livingEntityAccessor);
            float bandage = getBandage(livingEntityAccessor);
            if (level.getGameTime() % 600 == 0 && bandage > 0){
                if (bandage >= 5){
                    addBandage(livingEntityAccessor, -5);
                    addInjury(livingEntityAccessor, 5);
                }else {
                    float recovery = getBandage(livingEntityAccessor);
                    setBandage(livingEntityAccessor, 0);
                    addInjury(livingEntityAccessor, recovery);
                }

            }
            bandage = getBandage(livingEntityAccessor);
            switch (level.getDifficulty()) {
                case PEACEFUL:
                    if (level.getGameTime() % 600 == 0){
                        addInjury(livingEntityAccessor, 5);
                    }
                    break;
                case EASY:
                    if (level.getGameTime() % 1200 == 0){
                        if (injury > 40)
                            addInjury(livingEntityAccessor, 5);
                    }
                    break;
                case NORMAL:
                    if (level.getGameTime() % 600 == 0){
                        if (injury < 40 && bandage <= 0){
                            addInjury(livingEntityAccessor, -2);
                        }else if (injury > 60 && level.getGameTime() % 1200 == 0){
                            addInjury(livingEntityAccessor, 5);
                        }
                    }
                    break;
                case HARD:
                    if (level.getGameTime() % 200 == 0){
                        if (injury < 20 && bandage <= 0){
                            addInjury(livingEntityAccessor, -1);
                        }else if (injury < 60 && bandage <= 0 && level.getGameTime() % 600 == 0){
                            addInjury(livingEntityAccessor, -2);
                        }else if (injury > 60 && level.getGameTime() % 1200 == 0){
                            addInjury(livingEntityAccessor, 5);
                        }
                    }
                    break;
            }

            injuryEffect(livingEntity);
        }

    }

    private static void injuryEffect(LivingEntity livingEntity) {
        if (livingEntity instanceof ILivingEntityAccessor livingEntityAccessor){
            float injury = getInjury(livingEntityAccessor);
            float bandage = getBandage(livingEntityAccessor);
            if (injury <= 0) {
                livingEntity.hurt(SonaDamageTypes.damageSource(livingEntity.level().registryAccess(), SonaDamageTypes.INJURY), 999999);
            } else if (injury < 20) {
                livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 20, 5, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1, false, false));
                if (bandage <= 0)
                    livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.EXPOSURE.get(), 20, 3, false, false));
            } else if (injury < 40) {
                livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 20, 3, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1, false, false));
                if (bandage <= 0)
                    livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.EXPOSURE.get(), 20, 1, false, false));
            } else if (injury < 60) {
                livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 20, 1, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 0, false, false));
                if (bandage <= 0)
                    livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.EXPOSURE.get(), 20, 0, false, false));
            } else if (injury < 80) {
                livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 20, 0, false, false));
            }
        }

    }

    public static void onUseItem(ILivingEntityAccessor livingEntityAccessor, ItemStack itemStack){
        int index = CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), CommonConfig.INJURY_TREATMENT_ITEM.get());
        if (index == -1) return;
        String[] str = CommonConfig.INJURY_TREATMENT_ITEM.get().get(index).split(",");
        if (str.length < 3) return;
        addInjury(livingEntityAccessor, Float.parseFloat(str[1].trim()));
        addBandage(livingEntityAccessor, Float.parseFloat(str[2].trim()));
    }

    public static void onAttacked(LivingEntity target, DamageSource damageSource, double amount){
        if (target.level().isClientSide() || amount < 1)
            return;
        int index = CommonConfig.findIndex(damageSource.getMsgId(), CommonConfig.INJURY_EXCEPT_DAMAGESOURCE.get());
        if (index != -1) return;
        if (target instanceof ILivingEntityAccessor livingEntityAccessor)
            injuryCalculate(livingEntityAccessor, amount);
    }

    protected static void injuryCalculate(ILivingEntityAccessor livingEntityAccessor, double amount){
        float injuryLevel = (float) (50 / (Math.pow(Math.E, (-0.5) * amount + 4.5) + 1));
        float blockByBandage = getBandage(livingEntityAccessor);
        if (2 * injuryLevel <= blockByBandage){
            addBandage(livingEntityAccessor, -2 * injuryLevel);
        }else {
            setBandage(livingEntityAccessor, 0);
            addInjury(livingEntityAccessor, -1 * (injuryLevel - 0.5F * blockByBandage));
        }
    }

    public static void healBySleep(ILivingEntityAccessor livingEntityAccessor){
        if (CommonConfig.HEAL_NEED_BANDAGE.get()){
            if (CommonConfig.HEAL_NEED_BANDAGE.get() && getInjury(livingEntityAccessor) < CommonConfig.HEAL_THRESHOLD.get() && getBandage(livingEntityAccessor) == 0) return;
            addInjury(livingEntityAccessor, CommonConfig.HEAL_AMOUNT.get());
        }
    }
}
