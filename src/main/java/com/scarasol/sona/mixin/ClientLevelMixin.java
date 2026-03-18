package com.scarasol.sona.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.manager.InfectionManager;
import com.scarasol.sona.manager.SoundManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {




    protected ClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean b1, boolean b2, long l1, int i1) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, b1, b2, l1, i1);
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

//    @Inject(method = "lambda$getSkyColor$11", cancellable = true, at = @At("RETURN"))
//    private static void sona$changeInfectionSky(BiomeManager biomemanager, int x, int y, int z, CallbackInfoReturnable<Vec3> cir) {
//        int chunkX = x >> 4;
//        int chunkZ = y >> 4;
//
//    }

    @WrapOperation(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 sona$changeInfectionSky(Vec3 centerPos, CubicSampler.Vec3Fetcher fetcher, Operation<Vec3> operation) {
        if (InfectionManager.canChunkInfection(this)) {
            Vec3 realPos = centerPos.scale(4).add(2, 2, 2);
            return InfectionManager.getInfectionChunkSkyColor(operation.call(centerPos, fetcher), realPos, this);
        }
        return operation.call(centerPos, fetcher);
    }
}
