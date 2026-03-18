package com.scarasol.sona.mixin;


import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
    private void sona$applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager, CallbackInfo ci) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int height = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ());
        InfectionManager.initializeInfectionZone(worldGenLevel, new BlockPos(chunkPos.getMiddleBlockX(), height, chunkPos.getMiddleBlockZ()));
    }
}
