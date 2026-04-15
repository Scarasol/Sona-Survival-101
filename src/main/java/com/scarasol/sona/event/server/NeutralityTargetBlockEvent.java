package com.scarasol.sona.event.server;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
public class NeutralityTargetBlockEvent extends Event {
    private final Mob mob;
    private final Level level;
    @Nullable
    private final LivingEntity target;

    public NeutralityTargetBlockEvent(Mob mob, Level level, @Nullable LivingEntity target) {
        this.mob = mob;
        this.level = level;
        this.target = target;
    }

    public Mob getMob() {
        return mob;
    }

    public Level getLevel() {
        return level;
    }

    @Nullable
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
