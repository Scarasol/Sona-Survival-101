package com.scarasol.sona.mixin;

import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(FoliagePlacer.class)
public abstract class FoliagePlacerMixin {

    @Inject(method = "tryPlaceLeaf", cancellable = true, at = @At("HEAD"))
    private static void sona$infectionChunk(LevelSimulatedReader levelSimulatedReader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {

        if (levelSimulatedReader instanceof WorldGenLevel level) {
            ServerLevel serverLevel = level.getLevel();
            if (InfectionManager.canChunkInfection(serverLevel)) {
                double chance = (InfectionManager.getZoneInfection(serverLevel, blockPos, true) - 30) / 70D;
                if (randomSource.nextDouble() < chance) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
