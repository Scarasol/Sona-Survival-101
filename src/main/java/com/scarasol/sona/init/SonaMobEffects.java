package com.scarasol.sona.init;

import com.scarasol.sona.SonaMod;
import com.scarasol.sona.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SonaMobEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SonaMod.MODID);
    public static final RegistryObject<MobEffect> ANALGESIC = REGISTRY.register("analgesic", Analgesic::new);
    public static final RegistryObject<MobEffect> CAMOUFLAGE = REGISTRY.register("camouflage", () -> new PhysicalEffect(MobEffectCategory.BENEFICIAL, -13408768));
    public static final RegistryObject<MobEffect> CONFUSION = REGISTRY.register("confusion", Confusion::new);
    public static final RegistryObject<MobEffect> CORROSION = REGISTRY.register("corrosion", Corrosion::new);
    public static final RegistryObject<MobEffect> EXPOSURE = REGISTRY.register("exposure", Exposure::new);
    public static final RegistryObject<MobEffect> FRAGILITY = REGISTRY.register("fragility", Fragility::new);
    public static final RegistryObject<MobEffect> FROST = REGISTRY.register("frost", Frost::new);
    public static final RegistryObject<MobEffect> IGNITION = REGISTRY.register("ignition", Ignition::new);
    public static final RegistryObject<MobEffect> IMMUNITY = REGISTRY.register("immunity", Immunity::new);
    public static final RegistryObject<MobEffect> INSANE = REGISTRY.register("insane", Insane::new);
    public static final RegistryObject<MobEffect> SLIMINESS = REGISTRY.register("sliminess", Sliminess::new);
    public static final RegistryObject<MobEffect> STUN = REGISTRY.register("stun", Stun::new);

}
