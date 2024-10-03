package com.scarasol.sona.init;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class SonaDamageTypes {
    public static final ResourceKey<DamageType> CORROSION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sona:corrosion"));
    public static final ResourceKey<DamageType> INJURY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sona:injury"));
    public static final ResourceKey<DamageType> INFECTION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sona:infection"));
    public static final ResourceKey<DamageType> IMMUNITY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sona:immunity"));

    public static DamageSource damageSource(RegistryAccess registryAccess, ResourceKey<DamageType> resourceKey){
        return new DamageSource(registryAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(resourceKey));
    }
}
