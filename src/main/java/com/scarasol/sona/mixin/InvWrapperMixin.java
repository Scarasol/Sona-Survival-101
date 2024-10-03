package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InvWrapper.class)
public abstract class InvWrapperMixin implements IItemHandlerModifiable {

    @Inject(method = "insertItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onInsertItem(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir, ItemStack stackInSlot, int m, ItemStack copy){
        if (CommonConfig.ROT_OPEN.get() && stack.isEdible() && RotManager.canBeRotten(stack)){
            RotManager.rotWhenStack(copy, RotManager.getRot(copy), RotManager.getRot(stackInSlot), copy.getCount(), stackInSlot.getCount(), RotManager.getRotSaveTime(stackInSlot));
        }
    }
}
