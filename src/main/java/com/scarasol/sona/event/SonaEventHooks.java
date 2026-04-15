package com.scarasol.sona.event;

import com.google.common.collect.Lists;
import com.scarasol.sona.event.common.ChunkInfectionGetEvent;
import com.scarasol.sona.event.server.ChunkInfectionTreeDecoratorEvent;
import com.scarasol.sona.event.server.NeutralityTargetBlockEvent;
import com.scarasol.sona.event.server.SonaSoundEvent;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.init.SonaTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

/**
 * @author Scarasol
 */
public class SonaEventHooks {

    public static int getFullChunkInfection(BlockPos blockPos, Level level, int chunkInfection) {
        ChunkInfectionGetEvent event = new ChunkInfectionGetEvent(chunkInfection, level, blockPos, ChunkInfectionGetEvent.State.FULL);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getChunkInfection();
    }

    public static int getCalculateChunkInfection(BlockPos blockPos, Level level, int chunkInfection) {
        ChunkInfectionGetEvent event = new ChunkInfectionGetEvent(chunkInfection, level, blockPos, ChunkInfectionGetEvent.State.CALCULATE);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getChunkInfection();
    }

    public static int getInitChunkInfection(BlockPos blockPos, Level level, int chunkInfection) {
        ChunkInfectionGetEvent event = new ChunkInfectionGetEvent(chunkInfection, level, blockPos, ChunkInfectionGetEvent.State.INIT);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getChunkInfection();
    }


    public static List<TreeDecorator> addTreeDecorator(ServerLevel serverLevel, BlockPos blockPos, int chunkInfection) {
        List<TreeDecorator> treeDecorators = Lists.newArrayList();
        ChunkInfectionTreeDecoratorEvent event = new ChunkInfectionTreeDecoratorEvent(serverLevel, blockPos, chunkInfection, treeDecorators);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getTreeDecorators();
    }

    public static boolean spawnSoundDecoy(ServerLevel serverLevel, BlockPos blockPos, int amplifier, SonaSoundEvent.State state) {
        SonaSoundEvent event = new SonaSoundEvent(serverLevel, blockPos, amplifier, state);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public static boolean shouldBlockNeutralityTargetGoal(Mob mob, Level level, @javax.annotation.Nullable LivingEntity target) {
        NeutralityTargetBlockEvent event = new NeutralityTargetBlockEvent(mob, level, target);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public static boolean shouldCheckNeutrality(Mob mob, LivingEntity target) {
        if (mob.hasEffect(SonaMobEffects.INSANE.get())) {
            return false;
        }
        boolean mobNeutrality = mob.hasEffect(SonaMobEffects.NEUTRALITY.get());
        boolean mobTagged = mob.getType().is(SonaTags.NEUTRALITY_TARGETS);
        boolean targetNeutrality = target.hasEffect(SonaMobEffects.NEUTRALITY.get());
        boolean targetTagged = target.getType().is(SonaTags.NEUTRALITY_TARGETS);
        return mobNeutrality && (targetNeutrality || targetTagged) || (mobTagged && targetNeutrality);
    }
}
