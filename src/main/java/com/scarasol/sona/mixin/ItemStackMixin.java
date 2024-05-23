package com.scarasol.sona.mixin;

import com.scarasol.sona.SonaMod;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import com.scarasol.sona.manager.RustManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin extends net.minecraftforge.common.capabilities.CapabilityProvider<ItemStack> implements net.minecraftforge.common.extensions.IForgeItemStack {

    @Shadow public abstract boolean isEdible();

    @Shadow public abstract Item getItem();

    @Shadow public abstract Component getHoverName();

    protected ItemStackMixin(Class<ItemStack> baseClass) {
        super(baseClass);
    }

    protected ItemStackMixin(Class<ItemStack> baseClass, boolean isLazy) {
        super(baseClass, isLazy);
    }

    @Inject(method = "tagMatches", cancellable = true, at = @At("RETURN"))
    private static void OnTagMatches(ItemStack itemStack1, ItemStack itemStack2, CallbackInfoReturnable<Boolean> cir){
        if (!CommonConfig.ROT_STACKABLE.get() || cir.getReturnValue() || !itemStack1.isEdible() || !itemStack2.isEdible())
            return;
        if (tagMatchesIgnoreRot(itemStack1, itemStack2))
            cir.setReturnValue(true);
    }

    @Inject(method = "matches(Lnet/minecraft/world/item/ItemStack;)Z", cancellable = true, at = @At("RETURN"))
    private void onMatches(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir){
        if (!CommonConfig.ROT_STACKABLE.get() || cir.getReturnValue() || !this.isEdible() || !itemStack.isEdible())
            return;
        if (tagMatchesIgnoreRot(this, itemStack))
            cir.setReturnValue(true);
    }

    @Unique
    private static boolean tagMatchesIgnoreRot(Object itemStack11, ItemStack itemStack2){
        if (itemStack11 instanceof ItemStack itemStack1 && itemStack2.getItem().isEdible() && itemStack1.isEdible()){
            if (itemStack1.getTag() == null && itemStack2.getTag() != null) {
                return itemStack2.getTag().getAllKeys().size() == tagSize(itemStack2);
            }else if (itemStack1.getTag() != null && itemStack2.getTag() == null){
                return itemStack1.getTag().getAllKeys().size() == tagSize(itemStack1);
            }
        }
        return false;
    }

    @Unique
    private static int tagSize(ItemStack itemStack){
        int size = 0;
        if (itemStack.getTag() == null || !itemStack.isEdible()) return size;
        if (itemStack.getTag().contains("RotValue")) {
            size++;
        }
        if (itemStack.getTag().contains("RotSaveTime")) {
            size++;
        }
        if (itemStack.getTag().contains("RotMultiplier")) {
            size++;
        }
        return size;
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onInventoryTick(Level level, Entity entity, int slot, boolean selected, CallbackInfo ci){
        if (CommonConfig.ROT_OPEN.get() && !level.isClientSide())
            RotManager.rotTick(this, entity, slot, level.getBiome(entity.getOnPos()).value().getBaseTemperature() / 2 + 0.6);
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void useOn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir){
        Player player = useOnContext.getPlayer();
        if (player == null || player.isSpectator() || player.isCreative())
            return;
        if (CommonConfig.RUST_OPEN.get() && (cir.getReturnValue() == InteractionResult.CONSUME || cir.getReturnValue() == InteractionResult.SUCCESS))
            RustManager.rustItem(this, player, useOnContext.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        if (player.isSpectator() || player.isCreative())
            return;
        if (CommonConfig.RUST_OPEN.get() && (cir.getReturnValue().getResult() == InteractionResult.CONSUME || cir.getReturnValue().getResult() == InteractionResult.SUCCESS))
            RustManager.rustItem(this, player, interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    @Inject(method = "interactLivingEntity", at = @At("RETURN"))
    private void onInteractLivingEntity(Player player, LivingEntity livingEntity, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir){
        if (player.isSpectator() || player.isCreative())
            return;
        if (CommonConfig.RUST_OPEN.get() && (cir.getReturnValue() == InteractionResult.CONSUME || cir.getReturnValue() == InteractionResult.SUCCESS))
            RustManager.rustItem(this, player, interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    @Inject(method = "mineBlock", at = @At("RETURN"))
    private void onMineBlock(Level level, BlockState blockState, BlockPos blockPos, Player player, CallbackInfo ci){
        if (player.isSpectator() || player.isCreative())
            return;
        if (CommonConfig.RUST_OPEN.get())
            RustManager.rustItem(this, player, EquipmentSlot.MAINHAND);
    }

    @Inject(method = "hurtEnemy", at = @At("RETURN"))
    private void onHurtEnemy(LivingEntity livingEntity, Player player, CallbackInfo ci){
        if (player.isSpectator() || player.isCreative())
            return;
        if (CommonConfig.RUST_OPEN.get())
            RustManager.rustItem(this, player, player.getMainHandItem().equals(this) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    @Inject(method = "getDestroySpeed", cancellable = true, at = @At("RETURN"))
    private void onGetDestroySpeed(BlockState blockState, CallbackInfoReturnable<Float> cir){
        float speed = cir.getReturnValue();
        if (!CommonConfig.RUST_OPEN.get())
            return;
        if (RustManager.getRust(this) >= 70) {
            cir.setReturnValue(Math.min(1, speed * 0.8f));
        }else if (RustManager.getRust(this) >= 40) {
            cir.setReturnValue(Math.min(1, speed * 0.95f));
        }
    }
}
