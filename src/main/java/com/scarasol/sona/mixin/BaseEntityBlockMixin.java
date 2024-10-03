package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.IBaseContainerBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BaseEntityBlock.class)
public abstract class BaseEntityBlockMixin extends Block {

    public BaseEntityBlockMixin(Properties p_49795_) {
        super(p_49795_);
    }

    @Unique
    @Override
    public boolean onDestroyedByPlayer(BlockState blockstate, Level world, BlockPos pos, Player entity, boolean willHarvest, FluidState fluid) {
        if (blockstate.hasBlockEntity()) {
            if (world.getBlockEntity(pos) instanceof Container container)
                container.isEmpty();
            if (world.getBlockEntity(pos) instanceof IBaseContainerBlockEntityAccessor baseContainerBlockEntity && baseContainerBlockEntity.isLocked()){
                Clearable.tryClear(world.getBlockEntity(pos));
            }

        }
        return super.onDestroyedByPlayer(blockstate, world, pos, entity, willHarvest, fluid);
    }
}
