package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean b1, boolean b2, long l1, int i1) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, b1, b2, l1, i1);
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("RETURN"))
    private void onPlaySound(Player player, double x, double y, double z, Holder<SoundEvent> holder, SoundSource soundSource, float p_8681_, float p_8682_, long p_215025_,  CallbackInfo ci){
        if (!SoundManager.isSoundOpen() || holder == null)
            return;
        int index = SoundManager.getIndex(holder.get().getLocation().toString());
        if (index != -1)
            SoundManager.spawnSoundDecoy(this, x, y, z, SoundManager.getAmplifier(index));
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("RETURN"))
    private void onPlaySound(Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundSource, float p_8693_, float p_8694_, long p_215033_, CallbackInfo ci){
        if (!SoundManager.isSoundOpen() || holder == null)
            return;
        int index = SoundManager.getIndex(holder.get().getLocation().toString());
        if (index != -1)
            SoundManager.spawnSoundDecoy(this, entity.getX(), entity.getY(), entity.getZ(), SoundManager.getAmplifier(index));
    }
}
