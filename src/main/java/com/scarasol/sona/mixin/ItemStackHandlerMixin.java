package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStackHandler.class)
public abstract class ItemStackHandlerMixin {

    @Inject(method = "insertItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onInsertItem(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir, ItemStack existing, int limit, boolean reachedLimit){
        if (CommonConfig.ROT_OPEN.get() && existing.isEdible() && RotManager.canBeRotten(existing)){
            if (reachedLimit){
                RotManager.rotWhenStack(existing, RotManager.getRot(existing), RotManager.getRot(stack), existing.getCount(), limit, RotManager.getRotSaveTime(stack));
            }else {
                RotManager.rotWhenStack(existing, RotManager.getRot(existing), RotManager.getRot(stack), existing.getCount(), stack.getCount(), RotManager.getRotSaveTime(stack));
            }
        }

    }
}
