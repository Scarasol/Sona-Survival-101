package com.scarasol.sona.mixin;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.accessor.ISonaDataAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.effect.PhysicalEffect;
import com.scarasol.sona.init.SonaDamageTypes;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.InjuryManager;
import com.scarasol.sona.manager.InfectionManager;
import com.scarasol.sona.manager.SoundManager;
import com.scarasol.sona.util.ServerRenderEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntityAccessor, ISonaDataAccessor {

    @Shadow
    public abstract boolean addEffect(MobEffectInstance p_21165_);

    @Shadow
    public abstract boolean hasEffect(MobEffect p_21024_);

    @Shadow
    public abstract double getAttributeValue(Attribute p_21134_);

    @Shadow @Nullable
    public abstract AttributeInstance getAttribute(Attribute p_21051_);

    @Shadow public abstract boolean hurt(DamageSource p_21016_, float p_21017_);

    @Shadow @Nullable public abstract MobEffectInstance getEffect(MobEffect p_21125_);


    @Shadow public abstract float getMaxHealth();

    @Unique
    private final Map<String, Supplier<?>> sonaDataMap = Maps.newHashMap();

    @Unique
    private static final EntityDataAccessor<Float> INFECTION_LEVEL = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);

    @Unique
    private static final EntityDataAccessor<Float> INJURY_LEVEL = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);

    @Unique
    private static final EntityDataAccessor<Float> BANDAGE_LEVEL = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);

    @Unique
    private static final EntityDataAccessor<Integer> CAMOUFLAGE_AMPLIFIER = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Integer> EXPOSURE_AMPLIFIER = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Boolean> INFECTION_LAYER = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private final List<MobEffectInstance> physicalEffects = new ArrayList<>();

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    @Unique
    public Map<String, Supplier<?>> getSonaDataMap() {
        return sonaDataMap;
    }

    @Override
    @Unique
    public void setInfectionLevel(float infectionLevel) {
        this.entityData.set(INFECTION_LEVEL, infectionLevel);
    }

    @Override
    @Unique
    public float getInfectionLevel() {
        return this.entityData.get(INFECTION_LEVEL);
    }

    @Override
    @Unique
    public void setInjuryLevel(float injuryLevel) {
        this.entityData.set(INJURY_LEVEL, injuryLevel);
    }

    @Override
    @Unique
    public float getInjuryLevel() {
        return this.entityData.get(INJURY_LEVEL);
    }

    @Override
    @Unique
    public void setBandageLevel(float bandageLevel) {
        this.entityData.set(BANDAGE_LEVEL, bandageLevel);
    }

    @Override
    @Unique
    public float getBandageLevel() {
        return this.entityData.get(BANDAGE_LEVEL);
    }

    @Override
    @Unique
    public void setCamouflageAmplifier(int camouflageAmplifier) {
        this.entityData.set(CAMOUFLAGE_AMPLIFIER, camouflageAmplifier);
    }

    @Override
    @Unique
    public int getCamouflageAmplifier() {
        return this.entityData.get(CAMOUFLAGE_AMPLIFIER);
    }

    @Override
    @Unique
    public void setExposureAmplifier(int exposureAmplifier) {
        this.entityData.set(EXPOSURE_AMPLIFIER, exposureAmplifier);
    }

    @Override
    @Unique
    public int getExposureAmplifier() {
        return this.entityData.get(EXPOSURE_AMPLIFIER);
    }

    @Override
    @Unique
    public boolean getInfectionLayer() {
        return this.entityData.get(INFECTION_LAYER);
    }

    @Override
    @Unique
    public boolean isSona$carapace() {
        return getBooleanSonaData("Carapace");
    }

    @Override
    @Unique
    public void setSona$carapace(boolean sona$carapace) {
        if (sona$carapace) {
            putSonaData("Carapace", () -> true);
        } else {
            removeSonaData("Carapace");
        }

    }

    @Override
    @Unique
    public void setSona$laceration(float sona$laceration) {
        float laceration = getSona$laceration();
        putSonaData("Laceration", () -> Math.max(sona$laceration, laceration));
    }

    @Unique
    public void clearSona$laceration() {
        putSonaData("Laceration", () -> 0);
    }

    @Override
    @Unique
    public float getSona$laceration() {
        return getFloatSonaData("Laceration");
    }

    @Override
    @Unique
    public void setInfectionLayer(boolean infectionLayer) {
        this.entityData.set(INFECTION_LAYER, infectionLayer);
    }

    @Override
    @Unique
    @OnlyIn(Dist.CLIENT)
    public float getCamouflageAlpha() {
        int amplifier = getCamouflageAmplifier();
        if (amplifier > 0) {
            double distSqr = Minecraft.getInstance().player.distanceToSqr(this);
            double maxDist = 10 + 22D / amplifier;
            double maxDistSqr = maxDist * maxDist;
            double t = Math.min(Math.max((distSqr - 64) / (maxDistSqr - 64), 0), 1);
            return (float) ((1 - t) * (1 - t));
        }
        return 1;
    }

    //@Unique
//    @Override
//    public boolean canEnterPose(Pose pose) {
//        if (this.hasEffect(SonaMobEffects.FRAGILITY.get()))
//            return pose == Pose.SWIMMING;
//        else return super.canEnterPose(pose);
//    }


    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void onDefineSynchedData(CallbackInfo ci) {
        this.entityData.define(INFECTION_LEVEL, 0F);
        this.entityData.define(INJURY_LEVEL, 100F);
        this.entityData.define(BANDAGE_LEVEL, 0F);
        this.entityData.define(CAMOUFLAGE_AMPLIFIER, 0);
        this.entityData.define(EXPOSURE_AMPLIFIER, -1);
        this.entityData.define(INFECTION_LAYER, false);
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
        if (compoundTag.contains("Carapace")) {
            putSonaData("Carapace", () -> compoundTag.getBoolean("Carapace"));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onAddAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putFloat("InfectionLevel", this.getInfectionLevel());
        compoundTag.putFloat("InjuryLevel", this.getInjuryLevel());
        compoundTag.putFloat("BandageLevel", this.getBandageLevel());
        compoundTag.putBoolean("Carapace", getBooleanSonaData("Carapace"));
    }

    @Inject(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onEffectRemoved(Lnet/minecraft/world/effect/MobEffectInstance;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onRemoveAllEffects(CallbackInfoReturnable<Boolean> cir, Iterator<MobEffectInstance> iterator, boolean flag, MobEffectInstance effect) {
        if (effect.getEffect() instanceof PhysicalEffect && !CommonConfig.PHYSICAL_EFFECT_REMOVE.get()) {
            physicalEffects.add(effect);
        }
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity warpLastHurtMob(DamageSource instance, Operation<Entity> operation) {
        if (instance.getEntity() instanceof LivingEntity livingEntity && livingEntity.getAttribute(Attributes.FOLLOW_RANGE) != null && this.getAttribute(Attributes.FOLLOW_RANGE) != null && instance.isIndirect()) {
            if (livingEntity.hasEffect(SonaMobEffects.CAMOUFLAGE.get())) {
                double distance = this.position().distanceTo(livingEntity.position());
                if (livingEntity.hasEffect(SonaMobEffects.EXPOSURE.get())) {
                    double exposureRange = this.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.3 * (livingEntity.getEffect(SonaMobEffects.EXPOSURE.get()).getAmplifier() + 1);
                    if (exposureRange > distance) {
                        return operation.call(instance);
                    }
                }
                double range = this.getAttributeValue(Attributes.FOLLOW_RANGE) * (1 / Math.pow(2, livingEntity.getEffect(SonaMobEffects.CAMOUFLAGE.get()).getAmplifier() + 1));
                if (distance > range) {
                    return null;
                }
            }

        }
        return operation.call(instance);
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"))
    private void sona$swing(InteractionHand interactionHand, boolean send, CallbackInfo ci) {
        if (hasEffect(SonaMobEffects.LACERATION.get())) {
            int proc = (Object)this instanceof Player ? 2 : 5;
            setSona$laceration(proc * (getEffect(SonaMobEffects.LACERATION.get()).getAmplifier() + 1));
        }
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void sona$Travel(Vec3 input, CallbackInfo ci) {
        if (!level().isClientSide && !isPassenger() && hasEffect(SonaMobEffects.LACERATION.get())) {
            if (!((Object)this instanceof Player) && !input.equals(Vec3.ZERO)) {
                int amplifier = getEffect(SonaMobEffects.LACERATION.get()).getAmplifier() + 1;
                float speed = (float) (amplifier * 2.5 * (input.lengthSqr() / 0.05080516));
                setSona$laceration(Math.min(speed, 10 * amplifier));
            }
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void onBaseTick(CallbackInfo ci) {
        if (needInit()) {
            InfectionManager.infectedEntitySpawn((LivingEntity) (Object)this);
            setNeedInit(false);
        }
        if (!physicalEffects.isEmpty()) {
            for (MobEffectInstance instance : physicalEffects) {
                addEffect(instance);
            }
            physicalEffects.clear();
        }
        long gameTime = level().getGameTime() + getId();
        if (level() instanceof ServerLevel) {
            if (hasEffect(SonaMobEffects.CAMOUFLAGE.get())) {
                int amplifier = getEffect(SonaMobEffects.CAMOUFLAGE.get()).getAmplifier() + 1;
                setCamouflageAmplifier(amplifier);
            } else {
                setCamouflageAmplifier(0);
            }
            if (hasEffect(SonaMobEffects.EXPOSURE.get())) {
                setExposureAmplifier(getEffect(SonaMobEffects.EXPOSURE.get()).getAmplifier());
            } else {
                setExposureAmplifier(-1);
            }
            if (CommonConfig.EXPOSURE_INDICATOR.get() && (Object) this instanceof LivingEntity livingEntity && hasEffect(SonaMobEffects.EXPOSURE.get()) && gameTime % 10 == 0) {
                int amplifier = getEffect(SonaMobEffects.EXPOSURE.get()).getAmplifier();
                double renderRange = (amplifier + 1) * 16.0D;
                ServerRenderEmitter.emitPositionIndicator(level(), livingEntity.position().add(0, livingEntity.getBbHeight() * 0.6F, 0), renderRange, 14);
            }
            if (gameTime % 20 == 0) {
                float laceration = getSona$laceration();
                if (laceration > 0) {
                    invulnerableTime = 0;
                    float amount = getMaxHealth() * laceration / 100;
                    hurt(SonaDamageTypes.damageSource(level().registryAccess(), SonaDamageTypes.LACERATION), amount);
                }
                clearSona$laceration();
            }
            if ((Object) this instanceof LivingEntity livingEntity) {
                boolean flag = livingEntity.isAlive() && (!(livingEntity instanceof Player player) || (!player.isCreative() && !player.isSpectator()));
                if (!this.level().isClientSide() && flag) {
                    if (CommonConfig.INFECTION_OPEN.get()) {
                        if (gameTime % 20 == 0) {
                            if (hasEffect(SonaMobEffects.INFECTION.get())) {
                                setInfectionLayer(true);
                            } else if (InfectionManager.getInfection(this) > CommonConfig.INFECTION_THRESHOLD.get()) {
                                setInfectionLayer(true);
                            } else if (isSona$carapace()){
                                setInfectionLayer(true);
                            }else {
                                setInfectionLayer(false);
                            }
                        }
                        InfectionManager.infectionTick(livingEntity);
                    }
                    if (CommonConfig.INJURY_OPEN.get() && flag && livingEntity instanceof Player) {
                        InjuryManager.injuryTick(livingEntity);
                    }
                    if (livingEntity.isSprinting() && CommonConfig.SOUND_OPEN.get() && CommonConfig.SPRINT_SOUND.get() && gameTime % 60 == 0) {
                        SoundManager.spawnSoundDecoy(level(), getX(), getY(), getZ(), 0);
                    }
                }


            }
        }

    }

    @Unique
    @Override
    public void setXRot(float xRot) {
        if (!(this.hasEffect(SonaMobEffects.STUN.get()) || (this.hasEffect(SonaMobEffects.SLIMINESS.get()) && this.hasEffect(SonaMobEffects.FROST.get())))) {
            super.setXRot(xRot);
        }
    }

    @Unique
    @Override
    public void setYRot(float yRot) {
        if (!(this.hasEffect(SonaMobEffects.STUN.get()) || (this.hasEffect(SonaMobEffects.SLIMINESS.get()) && this.hasEffect(SonaMobEffects.FROST.get())))) {
            super.setYRot(yRot);
        }
    }

    @Unique
    @Override
    public boolean needInit() {
        return getBooleanSonaData("NeedInit");
    }

    @Unique
    @Override
    public void setNeedInit(boolean needInit) {
        if (needInit) {
            putSonaData("NeedInit", () -> true);
        }else {
            removeSonaData("NeedInit");
        }
    }
}
