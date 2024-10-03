package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Inject(method = "safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onSafeInsert(ItemStack itemStack, int p_150658_, CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack2, int i){
        if (CommonConfig.ROT_OPEN.get() && itemStack2.isEdible() && RotManager.canBeRotten(itemStack2))
            RotManager.rotWhenStack(itemStack2, RotManager.getRot(itemStack2), RotManager.getRot(itemStack), itemStack2.getCount(), i, RotManager.getRotSaveTime(itemStack));
    }

}
