package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.IBaseContainerBlockEntityAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(VanillaInventoryCodeHooks.class)
public abstract class VanillaInventoryCodeHooksMixin {
    @Inject(method = "lambda$extractHook$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/Hopper;setItem(ILnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
        private static void onExtractHook(Hopper dest, Pair itemHandlerResult, CallbackInfoReturnable<Boolean> cir, IItemHandler handler, int i, ItemStack extractItem, int j, ItemStack destStack){
            if (dest instanceof BlockEntity blockEntity && extractItem.isEdible() && CommonConfig.ROT_OPEN.get() && RotManager.canBeRotten(extractItem)){
                if (destStack.isEmpty()){
                    RotManager.putRotSaveTime(extractItem, blockEntity.getLevel().getGameTime());
                }else {
                RotManager.rotWhenStack(destStack, RotManager.getRot(destStack), RotManager.getRot(extractItem), destStack.getCount(), extractItem.getCount(), blockEntity.getLevel().getGameTime());
            }
        }
    }

    @Inject(method = "getItemHandler(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/Hopper;Lnet/minecraft/core/Direction;)Ljava/util/Optional;", remap = false, cancellable = true, at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void onGetItemHandler(Level level, Hopper hopper, Direction hopperFacing, CallbackInfoReturnable<Optional<Pair<IItemHandler, Object>>> cir, double x, double y, double z){
        if (cir.getReturnValue().isPresent()){
            if (level.getBlockEntity(new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z))) instanceof IBaseContainerBlockEntityAccessor baseContainerBlockEntityAccessor && baseContainerBlockEntityAccessor.isLocked())
                cir.setReturnValue(Optional.empty());
        }
    }
}
