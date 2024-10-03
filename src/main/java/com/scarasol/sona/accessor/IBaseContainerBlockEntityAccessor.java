package com.scarasol.sona.accessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface IBaseContainerBlockEntityAccessor {

    void breakLockKey();

    boolean isLocked();

    boolean isLocked(Player player);

    void lockContainer(CompoundTag compoundTag);

    void lockContainer(CompoundTag compoundTag, UUID uuid);
}
