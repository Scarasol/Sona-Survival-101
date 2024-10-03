package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Inject(method = "moveItemStackTo", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onMoveItemStackTo(ItemStack itemStack1, int i1, int i2, boolean flag1, CallbackInfoReturnable<Boolean> cir, boolean flag, int i, Slot slot, ItemStack itemstack, int j){
        if (CommonConfig.ROT_OPEN.get() && itemStack1.isEdible() && RotManager.canBeRotten(slot.getItem())){
            int maxSize = Math.min(slot.getMaxStackSize(), itemStack1.getMaxStackSize());
            ItemStack itemStack = slot.getItem();
            if (j <= maxSize){
                RotManager.rotWhenStack(itemStack, RotManager.getRot(itemStack), RotManager.getRot(itemStack1), itemStack.getCount(), j - itemStack.getCount(), RotManager.getRotSaveTime(itemStack1));
            }else {
                RotManager.rotWhenStack(itemStack, RotManager.getRot(itemStack), RotManager.getRot(itemStack1), itemStack.getCount(), maxSize - itemStack.getCount(), RotManager.getRotSaveTime(itemStack1));
            }
        }
    }
}
