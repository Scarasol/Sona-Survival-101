package com.scarasol.sona.mixin;

import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author Scarasol
 */
@Mixin(NaturalSpawner.SpawnState.class)
public abstract class SpawnStateMixin {

    @ModifyVariable(method = "canSpawnForCategory", at = @At("STORE"), ordinal = 0)
    private int sona$getMaxInstancesPerChunk(int i) {
        return (int) (i * 1.2);
    }
}
