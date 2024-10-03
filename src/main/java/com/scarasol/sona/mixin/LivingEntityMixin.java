package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.effect.PhysicalEffect;
import com.scarasol.sona.manager.InjuryManager;
import com.scarasol.sona.manager.InfectionManager;
import com.scarasol.sona.manager.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntityAccessor {

    @Shadow public abstract boolean addEffect(MobEffectInstance p_21165_);

    @Unique
    private static final EntityDataAccessor<Float> INFECTION_LEVEL = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> INJURY_LEVEL = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BANDAGE_LEVEL = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);

    @Unique
    private final List<MobEffectInstance> physicalEffects = new ArrayList<>();

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    public void setInfectionLevel(float infectionLevel) {
        this.entityData.set(INFECTION_LEVEL, infectionLevel);
    }

    @Unique
    public float getInfectionLevel() {
        return this.entityData.get(INFECTION_LEVEL);
    }

    @Unique
    public void setInjuryLevel(float injuryLevel) {
        this.entityData.set(INJURY_LEVEL, injuryLevel);
    }

    @Unique
    public float getInjuryLevel() {
        return this.entityData.get(INJURY_LEVEL);
    }

    @Unique
    public void setBandageLevel(float bandageLevel) {
        this.entityData.set(BANDAGE_LEVEL, bandageLevel);
    }

    @Unique
    public float getBandageLevel() {
        return this.entityData.get(BANDAGE_LEVEL);
    }




    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void onDefineSynchedData(CallbackInfo ci) {
        this.entityData.define(INFECTION_LEVEL, 0F);
        this.entityData.define(INJURY_LEVEL, 100F);
        this.entityData.define(BANDAGE_LEVEL, 0F);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onReadAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.contains("InfectionLevel")) {
            float infectionLevel = compoundTag.getFloat("InfectionLevel");
            this.setInfectionLevel(infectionLevel);
        }
        if (compoundTag.contains("InjuryLevel")) {
            float injuryLevel = compoundTag.getFloat("InjuryLevel");
            this.setInjuryLevel(injuryLevel);
        }
        if (compoundTag.contains("BandageLevel")) {
            float bandageLevel = compoundTag.getFloat("BandageLevel");
            this.setBandageLevel(bandageLevel);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onAddAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putFloat("InfectionLevel", this.getInfectionLevel());
        compoundTag.putFloat("InjuryLevel", this.getInjuryLevel());
        compoundTag.putFloat("BandageLevel", this.getBandageLevel());
    }

    @Inject(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onEffectRemoved(Lnet/minecraft/world/effect/MobEffectInstance;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onRemoveAllEffects(CallbackInfoReturnable<Boolean> cir, Iterator<MobEffectInstance> iterator, boolean flag, MobEffectInstance effect) {
        if (effect.getEffect() instanceof PhysicalEffect && !CommonConfig.PHYSICAL_EFFECT_REMOVE.get()){
            physicalEffects.add(effect);
        }
    }

//    @Redirect(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"))
//    private boolean onRemoveAllEffects(IEventBus instance, Event event) {
//        if (event instanceof PotionEvent.PotionRemoveEvent potionRemoveEvent) {
//            return instance.post(event) || (potionRemoveEvent.getPotion() instanceof PhysicalEffect && !CommonConfig.PHYSICAL_EFFECT_REMOVE.get());
//        }
//        return instance.post(event);
//    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void onBaseTick(CallbackInfo ci){
        if (!physicalEffects.isEmpty()){
            for (MobEffectInstance instance : physicalEffects){
                addEffect(instance);
            }
            physicalEffects.clear();
        }
        if ((Object)this instanceof LivingEntity livingEntity) {
            boolean flag = livingEntity.isAlive() && (!(livingEntity instanceof Player player) || (!player.isCreative() && !player.isSpectator()));
            if (!this.level().isClientSide() && flag) {
                if (CommonConfig.INFECTION_OPEN.get()){
                    InfectionManager.infectionTick(livingEntity);
                }
                if (CommonConfig.INJURY_OPEN.get() && flag && livingEntity instanceof Player) {
                    InjuryManager.injuryTick(livingEntity);
                }
                if (livingEntity.isSprinting() && CommonConfig.SOUND_OPEN.get() && CommonConfig.SPRINT_SOUND.get() && level().getGameTime() % 60 == 0) {
                    SoundManager.spawnSoundDecoy(level(), getX(), getY(), getZ(), 0);
                }
            }


        }
    }
}
