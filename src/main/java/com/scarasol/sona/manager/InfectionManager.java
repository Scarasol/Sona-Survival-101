package com.scarasol.sona.manager;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class InfectionManager {

    public static float getInfection(ILivingEntityAccessor livingEntity) {
        return livingEntity.getInfectionLevel();
    }

    public static void setInfection(ILivingEntityAccessor livingEntity, float infection) {
        livingEntity.setInfectionLevel(infection);
    }

    public static void addInfection(ILivingEntityAccessor livingEntity, float addition){
        float infection = addition > 0 ? Math.min(100, addition + getInfection(livingEntity)) : Math.max(0, addition + getInfection(livingEntity));
        setInfection(livingEntity, infection);
    }

    public static void init(ILivingEntityAccessor livingEntity){
        livingEntity.setInfectionLevel(0);
    }

    public static void infectionTick(LivingEntity livingEntity){
        Level level = livingEntity.getLevel();
        if (level.getGameTime() % 1600 == 0 && livingEntity instanceof ILivingEntityAccessor livingEntityAccessor){
            switch (level.getDifficulty()) {
                case PEACEFUL:
                    addInfection(livingEntityAccessor, -5);
                    break;
                case EASY:
                    if (livingEntityAccessor.getInfectionLevel() < 40)
                        addInfection(livingEntityAccessor, -2.5f);
                    break;
                case NORMAL:
                    if (livingEntityAccessor.getInfectionLevel() < 40)
                        addInfection(livingEntityAccessor, -2.5f);
                    if (livingEntityAccessor.getInfectionLevel() > 70 && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get()))
                        addInfection(livingEntityAccessor, 1);
                    break;
                case HARD:
                    if (livingEntityAccessor.getInfectionLevel() > 40 && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get()))
                        addInfection(livingEntityAccessor, 1);
                    break;
            }
        }
        infectionEffect(livingEntity);
    }

    protected static void infectionEffect(LivingEntity livingEntity){
        if (livingEntity.getLevel().isClientSide())
            return;
        if (livingEntity instanceof ILivingEntityAccessor livingEntityAccessor){
            if (livingEntityAccessor.getInfectionLevel() >= 100){
                livingEntity.hurt(new DamageSource("infection"), 999999);
            }else if (livingEntityAccessor.getInfectionLevel() > 90){
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
            }else if (livingEntityAccessor.getInfectionLevel() > 70){
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20, 0, false, false));
            }else if (livingEntityAccessor.getInfectionLevel() > 40){
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20, 0, false, false));
            }
            if (livingEntityAccessor.getInfectionLevel() > 70 && livingEntity.getLevel().getGameTime() % 400 == 0 && livingEntity.getRandom().nextDouble() < 0.3){
                livingEntity.getLevel().playSound(null, livingEntity, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft:entity.zombie.ambient")), SoundSource.NEUTRAL, 1, 1);
            }
        }
    }

    public static void turnZombie(LivingEntity livingEntity){
        double weightSum = getWeightSum();
        double currentWeight = 0;
        double random = new Random().nextDouble();
        String zombieToSpawn = "";
        if (weightSum == 0) return;
        for (String str: CommonConfig.ZOMBIE_LIST.get()){
            String[] buffer = str.split(",");
            if (buffer.length < 2) continue;
            currentWeight += Double.parseDouble(buffer[1].trim());
            if (random < currentWeight / weightSum){
                zombieToSpawn = buffer[0];
                break;
            }
        }
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(zombieToSpawn));
        if (entityType == null) return;
        Entity entityToSpawn = entityType.create(livingEntity.getLevel());
        if (entityToSpawn == null) return;
        entityToSpawn.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        livingEntity.getLevel().addFreshEntity(entityToSpawn);
    }

    protected static double getWeightSum(){
        double sum = 0;
        for (String str: CommonConfig.ZOMBIE_LIST.get()){
            String[] buffer = str.split(",");
            if (buffer.length < 2) continue;
            sum += Double.parseDouble(buffer[1].trim());
        }
        return sum;
    }

    public static void onAttacked(LivingEntity target, Entity entity){
        if (target.getLevel().isClientSide())
            return;
        boolean flag = false;
        if (entity instanceof LivingEntity livingEntity && (livingEntity.getMobType() == MobType.UNDEAD || CommonConfig.INFECTION_SOURCE_MOB.get().contains(ForgeRegistries.ENTITIES.getKey(livingEntity.getType()).toString()))) {
            if (target.hasEffect(SonaMobEffects.IMMUNITY.get())) {
                immunityEffect(livingEntity, target.getEffect(SonaMobEffects.IMMUNITY.get()).getAmplifier());
            }else {
                flag = true;
            }
        }else if (CommonConfig.INFECTION_SOURCE_PROJECTILE.get().contains(ForgeRegistries.ENTITIES.getKey(entity.getType()).toString())){
            flag = true;
        }
        if (flag){
            if (!(target instanceof Player || CommonConfig.SUSCEPTIBLE_POPULATION.get().contains(ForgeRegistries.ENTITIES.getKey(target.getType()).toString())))
                return;
            infectionCalculate(target);
        }
    }

    protected static void infectionCalculate(LivingEntity livingEntity){
        AttributeInstance attribute = livingEntity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        double resistanceChance = attribute != null ? 0.03 * attribute.getValue() : 0;
        if (Math.random() >= resistanceChance && livingEntity instanceof ILivingEntityAccessor livingEntityAccessor) {
            addInfection(livingEntityAccessor, (float) (200 / (Math.pow(livingEntity.getArmorValue(), 2) + 50) + 1));
        }
    }

    protected static void immunityEffect(LivingEntity livingEntity, int amplifier){
        if (amplifier == 1) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 140, 1));
        } else if (amplifier == 2) {
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 140, 3));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2));
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.CONFUSION.get(), 140, 0));
        }
    }

    public static void onUseItem(ILivingEntityAccessor livingEntity, ItemStack itemStack){
        int index = Math.max(CommonConfig.tagSearch(itemStack, CommonConfig.INFECTION_SOURCE_ITEM.get()), CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), CommonConfig.INFECTION_SOURCE_ITEM.get()));
        if (index == -1) return;
        String[] str = CommonConfig.INFECTION_SOURCE_ITEM.get().get(index).split(",");
        if (str.length < 4) return;
        Random random = new Random();
        if (random.nextFloat() < Float.parseFloat(str[1].trim()) / 100){
            float min = Math.min(Float.parseFloat(str[2].trim()), Float.parseFloat(str[3].trim()));
            float max = Math.max(Float.parseFloat(str[2].trim()), Float.parseFloat(str[3].trim()));
            if (min == max){
                addInfection(livingEntity, min);
            }else {
                addInfection(livingEntity, random.nextFloat(min, max));
            }
        }
    }

    public static String blurMessage(ILivingEntityAccessor player, String str){
        if (player.getInfectionLevel() > 70){
            str = "§k" + str;
        }
        return str;
    }
}
