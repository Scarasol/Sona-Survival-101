package com.scarasol.sona.mixin.travelersbackpack;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import com.tiviacz.travelersbackpack.inventory.sorter.ContainerSorter;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerSorter.class)
public abstract class ContainerSorterMixin {
    @Inject(method = "combineStacks", remap = false, at = @At("HEAD"))
    private static void onCombineStacks(ItemStack stack, ItemStack stack2, CallbackInfo ci){
        int count;
        if (stack.getMaxStackSize() >= stack.getCount() + stack2.getCount()){
            count = stack2.getCount();
        }else {
            count = stack.getMaxStackSize() - stack.getCount();
        }
        if (CommonConfig.ROT_OPEN.get() && stack.isEdible() && RotManager.canBeRotten(stack)){
            RotManager.rotWhenStack(stack, RotManager.getRot(stack), RotManager.getRot(stack2), stack.getCount(), count, RotManager.getRotSaveTime(stack2));
        }
    }
}
