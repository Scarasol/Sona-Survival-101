package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, Nameable {

    @Inject(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onAddResource(int slotNumber, ItemStack itemStack, CallbackInfoReturnable<Integer> cir, Item item, int i, ItemStack itemStack2, int j){
        if (CommonConfig.ROT_OPEN.get() && itemStack2.isEdible() && RotManager.canBeRotten(itemStack))
            RotManager.rotWhenStack(itemStack2, RotManager.getRot(itemStack2), RotManager.getRot(itemStack), itemStack2.getCount(), j, RotManager.getRotSaveTime(itemStack));
    }

}
