package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.mixin.IChunkAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Scarasol
 */
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin implements BlockGetter, BiomeManager.NoiseBiomeSource, LightChunk, StructureAccess, IChunkAccessor {

    @Unique
    private CompoundTag sonaCompoundTag = new CompoundTag();

    @Unique
    private boolean sona$needSync = false;

    @Unique
    @Override
    public CompoundTag getSonaCompoundTag() {
        return sonaCompoundTag;
    }

    @Unique
    @Override
    public void setSonaCompoundTag(CompoundTag sonaCompoundTag) {
        this.sonaCompoundTag = sonaCompoundTag;
    }

    @Unique
    @Override
    public void setNeedSync(boolean needSync) {
        sona$needSync = needSync;
    }

    @Unique
    @Override
    public boolean isNeedSync() {
        return sona$needSync;
    }
}
