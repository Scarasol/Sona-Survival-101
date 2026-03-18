package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.mixin.ILevelChunkSection;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Scarasol
 */
@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements ILevelChunkSection {

    @Shadow private short nonEmptyBlockCount;

    @Override
    @Unique
    public int getSonaNonEmptyBlockCount() {
        return nonEmptyBlockCount;
    }
}
