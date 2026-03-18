package com.scarasol.sona.event.server;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * @author Scarasol
 */
public class ChunkInfectionTreeDecoratorEvent extends Event {

    private final ServerLevel serverLevel;
    private final BlockPos blockPos;
    private final int chunkInfection;
    private final List<TreeDecorator> treeDecorators;

    public ChunkInfectionTreeDecoratorEvent(ServerLevel serverLevel, BlockPos blockPos, int chunkInfection, List<TreeDecorator> treeDecorators) {
        this.serverLevel = serverLevel;
        this.blockPos = blockPos;
        this.chunkInfection = chunkInfection;
        this.treeDecorators = treeDecorators;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getChunkInfection() {
        return chunkInfection;
    }

    public List<TreeDecorator> getTreeDecorators() {
        return treeDecorators;
    }
}
