package com.scarasol.sona.accessor.mixin;

import com.scarasol.sona.util.SonaPerlinNoise;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.Queue;

/**
 * @author Scarasol
 */
public interface IServerLevelAccessor {

    static IServerLevelAccessor fromServerLevel(ServerLevel serverLevel) {
        return (IServerLevelAccessor)serverLevel;
    }

    SonaPerlinNoise getSonaPerlinNoise();

    Queue<ChunkPos> getSonaLoadedChunk();
}
