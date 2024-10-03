package com.scarasol.sona.init;

import com.scarasol.sona.SonaMod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class SonaSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SonaMod.MODID);
    public static final RegistryObject<SoundEvent> TINNITUS = REGISTRY.register("tinnitus", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SonaMod.MODID, "tinnitus")));
    public static final RegistryObject<SoundEvent> CRATE = REGISTRY.register("crate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SonaMod.MODID, "crate")));


}