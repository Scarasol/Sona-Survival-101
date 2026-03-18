package com.scarasol.sona.event.server;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Scarasol
 */
public class SonaSoundEvent extends Event{
    private final ServerLevel serverLevel;
    private final BlockPos blockPos;
    private final int amplifier;
    private final State state;

    public SonaSoundEvent(ServerLevel serverLevel, BlockPos blockPos, int amplifier, State state) {
        this.serverLevel = serverLevel;
        this.blockPos = blockPos;
        this.amplifier = amplifier;
        this.state = state;
    }

    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }



    public enum State {
        /**
         * LivingAddEffect
         */
        LIVING,

        /**
         * SpawnDecoy
         */
        DECOY
    }
}
