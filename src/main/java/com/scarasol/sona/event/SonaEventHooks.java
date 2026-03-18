package com.scarasol.sona.event;

import com.google.common.collect.Lists;
import com.scarasol.sona.event.common.ChunkInfectionGetEvent;
import com.scarasol.sona.event.server.ChunkInfectionTreeDecoratorEvent;
import com.scarasol.sona.event.server.SonaSoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
}
