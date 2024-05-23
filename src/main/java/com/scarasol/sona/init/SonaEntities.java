package com.scarasol.sona.init;

import com.scarasol.sona.SonaMod;
import com.scarasol.sona.entity.SoundDecoy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SonaEntities {

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, SonaMod.MODID);

    public static final RegistryObject<EntityType<SoundDecoy>> SOUND_DECOY = register("sound_decoy", EntityType.Builder.<SoundDecoy>of(SoundDecoy::new, MobCategory.MONSTER)
            .setShouldReceiveVelocityUpdates(true).setTrackingRange(0).setUpdateInterval(3).setCustomClientFactory(SoundDecoy::new).fireImmune().sized(0.3f, 0.3f));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryName, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryName, () -> entityTypeBuilder.build(registryName));
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SOUND_DECOY.get(), SoundDecoy.createAttributes().build());
    }

}
