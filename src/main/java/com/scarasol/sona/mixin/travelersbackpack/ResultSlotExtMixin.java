package com.scarasol.sona.mixin.travelersbackpack;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import com.tiviacz.travelersbackpack.inventory.menu.slot.ResultSlotExt;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ResultSlotExt.class)
public abstract class ResultSlotExtMixin {
    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onOnTake(Player player, ItemStack stack, CallbackInfo ci, NonNullList list, Recipe recipe, int i, ItemStack itemstack, ItemStack itemstack1){
        if (CommonConfig.ROT_OPEN.get() && stack.isEdible() && RotManager.canBeRotten(stack))
            RotManager.rotWhenStack(itemstack1, RotManager.getRot(itemstack1), RotManager.getRot(itemstack), itemstack1.getCount(), itemstack.getCount(), RotManager.getRotSaveTime(itemstack));
    }
}
