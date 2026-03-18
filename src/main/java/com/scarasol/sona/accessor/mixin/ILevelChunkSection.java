package com.scarasol.sona.accessor.mixin;

import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * @author Scarasol
 */
public interface ILevelChunkSection {

    static ILevelChunkSection fromLevelChunk(LevelChunkSection chunkSection) {
        return (ILevelChunkSection) chunkSection;
    }

    int getSonaNonEmptyBlockCount();
}
