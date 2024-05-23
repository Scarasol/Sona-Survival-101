package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import com.scarasol.sona.manager.RustManager;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Mixin(LootTable.class)
public abstract class LootTableMixin {

    @Inject(method = "fill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onFill(Container container, LootContext lootContext, CallbackInfo ci, List list, Random random, List list1, Iterator var6, ItemStack itemStack){
        if (CommonConfig.ROT_OPEN.get() && container instanceof BlockEntity entity && itemStack.isEdible() && RotManager.canBeRotten(itemStack)) {
            long day = entity.getLevel().getDayTime() / 24000;
            double multiplier = 5D * day / (day + 12D);
            RotManager.putRot(itemStack, random.nextDouble() * 20 * Math.max(multiplier, 1));
        }
        if (CommonConfig.RUST_OPEN.get() && RustManager.canBeRust(itemStack)){
            if (random.nextDouble() < 0.2){
                RustManager.putWaxed(itemStack, random.nextInt(CommonConfig.WAX_TIMES.get()));
            }else {
                RustManager.putRust(itemStack, random.nextDouble() * 100);
            }
        }
    }
}
