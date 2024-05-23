package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.IBaseContainerBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Inject(method = "getSourceContainer", at = @At(value = "RETURN"), cancellable = true)
    private static void onGetContainerAt(Level level, Hopper hopper, CallbackInfoReturnable<Container> cir){
        if (cir.getReturnValue() != null && level.getBlockEntity(new BlockPos(hopper.getLevelX(), hopper.getLevelY() + 1.0D, hopper.getLevelZ())) instanceof IBaseContainerBlockEntityAccessor baseContainerBlockEntityAccessor && baseContainerBlockEntityAccessor.isLocked())
            cir.setReturnValue(null);

    }

}
