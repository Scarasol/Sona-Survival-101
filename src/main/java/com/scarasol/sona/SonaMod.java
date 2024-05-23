package com.scarasol.sona;

import com.mojang.logging.LogUtils;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaEntities;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SonaMod.MODID)
public class SonaMod
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "sona";

    public SonaMod()
    {
        SonaMobEffects.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        SonaEntities.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "sona-common.toml");
        // Register the enqueueIMC method for modloading
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.addNetworkMessage();
    }
}
