package com.scarasol.sona.init;

import com.scarasol.sona.SonaMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.HashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SonaSounds {
    public static Map<ResourceLocation, SoundEvent> REGISTRY = new HashMap<>();
    static {
        REGISTRY.put(new ResourceLocation(SonaMod.MODID, "tinnitus"), new SoundEvent(new ResourceLocation(SonaMod.MODID, "tinnitus")));
        REGISTRY.put(new ResourceLocation(SonaMod.MODID, "crate"), new SoundEvent(new ResourceLocation(SonaMod.MODID, "crate")));
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        for (Map.Entry<ResourceLocation, SoundEvent> sound : REGISTRY.entrySet())
            event.getRegistry().register(sound.getValue().setRegistryName(sound.getKey()));
    }

}
