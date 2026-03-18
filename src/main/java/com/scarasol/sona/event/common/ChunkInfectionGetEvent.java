package com.scarasol.sona.event.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Scarasol
 */
public class ChunkInfectionGetEvent extends Event {

    private int chunkInfection;
    private final Level level;
    private final BlockPos blockPos;
    private final State state;

    public ChunkInfectionGetEvent(int chunkInfection, Level level, BlockPos blockPos, State state) {
        this.chunkInfection = chunkInfection;
        this.level = level;
        this.blockPos = blockPos;
        this.state = state;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public State getState() {
        return state;
    }

    public int getChunkInfection() {
        return chunkInfection;
    }

    public void setChunkInfection(int chunkInfection) {
        this.chunkInfection = chunkInfection;
    }



    public enum State {
        /**
         * loadedChunk
         */
        FULL,

        /**
         * InitChunk
         */
        INIT,

        /**
         * Calculate
         */
        CALCULATE
    }
}
