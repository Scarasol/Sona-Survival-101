package com.scarasol.sona.mixin;

import com.mojang.serialization.Codec;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author Scarasol
 */
@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin extends Feature<TreeConfiguration> {
    public TreeFeatureMixin(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/BoundingBox;encapsulatingPositions(Ljava/lang/Iterable;)Ljava/util/Optional;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void sona$place(FeaturePlaceContext<TreeConfiguration> featurePlaceContext, CallbackInfoReturnable<Boolean> cir, WorldGenLevel worldgenlevel, RandomSource randomsource, BlockPos blockpos, TreeConfiguration treeconfiguration, Set set, Set set1, Set set2, Set set3, BiConsumer biconsumer, BiConsumer biconsumer1, FoliagePlacer.FoliageSetter foliageplacer$foliagesetter, BiConsumer biconsumer2, boolean flag) {
        if (worldgenlevel instanceof ServerLevel serverLevel && InfectionManager.canChunkInfection(serverLevel)) {
            List<TreeDecorator> treeDecorators = InfectionManager.addTreeDecorator(serverLevel, blockpos);
            if (!treeDecorators.isEmpty()) {
                TreeDecorator.Context context = new TreeDecorator.Context(worldgenlevel, biconsumer2, randomsource, set1, set2, set);
                treeDecorators.forEach((decorator) -> decorator.place(context));
            }
        }

    }

}
