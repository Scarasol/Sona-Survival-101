package com.scarasol.sona.network;

import com.scarasol.sona.accessor.IBaseContainerBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class LockPacket {

    private final BlockPos blockPos;
    private final UUID lockCode;
    private final boolean locked;

    public LockPacket(BlockPos blockPos, UUID lockCode, boolean locked) {
        this.blockPos = blockPos;
        this.lockCode = lockCode;
        this.locked = locked;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public UUID getLockCode() {
        return lockCode;
    }

    public boolean isLocked() {
        return locked;
    }

    public static LockPacket decode(FriendlyByteBuf buf) {
        return new LockPacket(buf.readBlockPos(), buf.readUUID(), buf.readBoolean());
    }

    public static void encode(LockPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.blockPos);
        buf.writeUUID(msg.lockCode);
        buf.writeBoolean(msg.locked);
    }

    public static void handler(LockPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                context.get().enqueueWork(() -> {
                    if (context.get().getDirection().getReceptionSide().isServer()){
                        ServerPlayer player = context.get().getSender();
                        if (player != null){
                            Level level = player.getLevel();
                            BlockEntity blockEntity = level.getBlockEntity(msg.getBlockPos());
                            BlockState blockState = level.getBlockState(msg.getBlockPos());
                            if (blockEntity instanceof IBaseContainerBlockEntityAccessor baseContainerBlockEntity && !blockEntity.getPersistentData().contains("flag")){
                                if (msg.isLocked())
                                    baseContainerBlockEntity.lockContainer(blockEntity.getPersistentData(), msg.getLockCode());
                                blockEntity.getPersistentData().putBoolean("flag", true);
                                level.sendBlockUpdated(msg.getBlockPos(), blockState, blockState, 3);
                            }
                        }
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}
