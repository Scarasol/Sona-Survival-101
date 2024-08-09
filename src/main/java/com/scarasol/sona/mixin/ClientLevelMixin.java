package com.scarasol.sona.mixin;

import com.scarasol.sona.manager.SoundManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean b1, boolean b2, long l1) {
        super(writableLevelData, resourceKey, holder, supplier, b1, b2, l1);
    }

    @Inject(method = "playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V", at = @At("RETURN"))
    private void onPlayLocalSound(double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float p_104605_, float p_104606_, boolean p_104607_, CallbackInfo ci){
        if (!SoundManager.isSoundOpen() || soundEvent == null)
            return;
        int index = SoundManager.getIndex(soundEvent.getLocation().toString());
        if (index != -1){
            SoundManager.spawnSoundDecoy(this, x, y, z, SoundManager.getAmplifier(index));
        }

    }
}
