package com.scarasol.sona.mixin;

import com.google.common.collect.Queues;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.SonaMod;
import com.scarasol.sona.accessor.mixin.IChunkAccessor;
import com.scarasol.sona.accessor.mixin.IServerLevelAccessor;
import com.scarasol.sona.manager.InfectionManager;
import com.scarasol.sona.manager.SoundManager;
import com.scarasol.sona.util.SonaPerlinNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Queue;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements IServerLevelAccessor {

    @Unique
    private SonaPerlinNoise sona$noise;

    @Unique
    private final Queue<ChunkPos> sona$loadedChunk = Queues.newArrayDeque();

    @Shadow public abstract long getSeed();

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean b1, boolean b2, long l1, int i1) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, b1, b2, l1, i1);
    }

    @Override
    @Unique
    public SonaPerlinNoise getSonaPerlinNoise() {
        if (sona$noise == null) {
            sona$noise = new SonaPerlinNoise(getSeed() ^ (dimension().location().toString().hashCode() * 31L));
        }
        return sona$noise;
    }

    @Override
    @Unique
    public Queue<ChunkPos> getSonaLoadedChunk() {
        return sona$loadedChunk;
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("RETURN"))
    private void onPlaySound(Player player, double x, double y, double z, Holder<SoundEvent> holder, SoundSource soundSource, float p_8681_, float p_8682_, long p_215025_,  CallbackInfo ci){
        if (!SoundManager.isSoundOpen() || holder == null) {
            return;
        }
        int index = SoundManager.getIndex(holder.get().getLocation().toString());
        if (index != -1) {
            SoundManager.spawnSoundDecoy(this, x, y, z, SoundManager.getAmplifier(index));
        }
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("RETURN"))
    private void onPlaySound(Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundSource, float p_8693_, float p_8694_, long p_215033_, CallbackInfo ci){
        if (!SoundManager.isSoundOpen() || holder == null) {
            return;
        }
        int index = SoundManager.getIndex(holder.get().getLocation().toString());
        if (index != -1) {
            SoundManager.spawnSoundDecoy(this, entity.getX(), entity.getY(), entity.getZ(), SoundManager.getAmplifier(index));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sona$calculateChunkInfection(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        if (InfectionManager.canChunkInfection(this) && !sona$loadedChunk.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                ChunkPos chunkPos = sona$loadedChunk.poll();
                if (chunkPos != null) {
                    ChunkAccess levelChunk = getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL);
                    InfectionManager.calculateInfectionZone((ServerLevel) ((Object)this), chunkPos);
                    sona$loadedChunk.offer(chunkPos);
                    IChunkAccessor chunkAccessor = IChunkAccessor.fromLevelChunk(levelChunk);
                    if (chunkAccessor.isNeedSync()) {
                        chunkAccessor.syncChunkData((ServerLevel) ((Object)this), chunkPos);
                        chunkAccessor.setNeedSync(false);
                    }
                }
            }
        }
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void sona$infectionBlockTick(BlockState instance, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, Operation<Void> operation) {
        if (InfectionManager.canChunkInfection(this) && instance.getBlock() instanceof IPlantable) {
            int infection = InfectionManager.getZoneInfection(this, blockPos, true);
            if (serverLevel.random.nextInt(30, 50) - infection <= 0) {
                return;
            }
        }
        operation.call(instance, serverLevel, blockPos, randomSource);
    }

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void sona$infectionBlockDestroy(LevelChunk levelChunk, int particleTick, CallbackInfo ci, ChunkPos chunkpos, boolean flag, int i, int j, ProfilerFiller profilerfiller, LevelChunkSection[] alevelchunksection, int l, LevelChunkSection levelchunksection, int j1, int k1, int l1, BlockPos blockPos3) {
        BlockPos blockPos = new BlockPos(blockPos3.getX() - i, blockPos3.getY() - k1, blockPos3.getZ() - j);
        BlockState blockState = levelchunksection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (InfectionManager.canChunkInfection(this) && blockState.getBlock() instanceof IPlantable) {
            int infection = InfectionManager.getZoneInfection(this, blockPos3, true);
            if (infection > 75) {
                destroyBlock(blockPos3, false);
            }
        }
    }
}
