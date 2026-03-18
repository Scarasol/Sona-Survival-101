package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.mixin.IBaseContainerBlockEntityAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.network.LockPacket;
import com.scarasol.sona.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.UUID;

@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin extends BlockEntity implements IBaseContainerBlockEntityAccessor {

    @Shadow
    private LockCode lockKey;

    @Shadow
    public abstract Component getName();

    public BaseContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    @Unique
    public void breakLockKey() {
        if (ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(this.getType()).toString().contains("lootr"))
            return;
        this.lockKey = LockCode.NO_LOCK;
        getPersistentData().remove("Lock");
        if (this.getLevel() != null) {
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }

    }

    @Override
    @Unique
    public boolean isLocked() {
        return this.lockKey != LockCode.NO_LOCK || getPersistentData().contains("Lock");
    }

    @Override
    @Unique
    public boolean isLocked(Player player) {
        if (ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(this.getType()).toString().contains("lootr")) {
            return isLocked() && !containsPlayer(player, saveWithoutMetadata().getList("LootrOpeners", Tag.TAG_INT_ARRAY));
        } else {
            return isLocked();
        }
    }

    @Unique
    public boolean containsPlayer(Player player, ListTag list) {
        UUID uuid = player.getUUID();
        for (Tag tag : list) {
            if (uuid.equals(NbtUtils.loadUUID(tag)))
                return true;
        }
        return false;
    }

    @Override
    @Unique
    public void lockContainer(CompoundTag compoundTag) {
        if (ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(getType()).getPath().contains("airdrop"))
            return;
        if (CommonConfig.BLOCK_LOCK_BLACKLIST.get().contains(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(getType()).toString()))
            return;
        if (compoundTag.contains("LootTable", 8) && !(compoundTag.contains("ForgeData") && compoundTag.getCompound("ForgeData").contains("LockFlag"))) {
            UUID uuid = UUID.randomUUID();
            boolean isLocked = false;
            if (CommonConfig.LOCK_WHITELIST_OPEN.get() && CommonConfig.findIndex(compoundTag.getString("LootTable"), CommonConfig.LOCK_WHITELIST.get()) == -1)
                return;
            if (new Random().nextDouble() * 100 < CommonConfig.LOCK_PERCENT.get()) {
                isLocked = true;
                lockContainer(compoundTag, uuid);
            }
            if (getLevel() != null && getLevel().isClientSide() && ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(this.getType()).toString().contains("lootr"))
                NetworkHandler.PACKET_HANDLER.sendToServer(new LockPacket(getBlockPos(), uuid, isLocked));
        }
    }

    @Override
    @Unique
    public void lockContainer(CompoundTag compoundTag, UUID uuid) {
        compoundTag.putString("Lock", uuid.toString());
        this.lockKey = LockCode.fromTag(compoundTag);
    }

    @Inject(method = "canOpen", cancellable = true, at = @At("RETURN"))
    private void onCanUnlock(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(this.getType()).toString().contains("lootr"))
            cir.setReturnValue(true);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void onLoad(CompoundTag compoundTag, CallbackInfo ci) {
        if (CommonConfig.LOCK_PERCENT.get() == 0) {
            if (getPersistentData().contains("LockFlag"))
                getPersistentData().remove("LockFlag");
            this.lockKey = LockCode.NO_LOCK;
        } else {
            lockContainer(compoundTag);
            this.getPersistentData().putBoolean("LockFlag", true);
        }
    }


}
