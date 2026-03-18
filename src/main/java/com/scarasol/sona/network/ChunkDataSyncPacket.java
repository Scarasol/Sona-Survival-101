package com.scarasol.sona.network;

import com.scarasol.sona.accessor.mixin.IChunkAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChunkDataSyncPacket(int x, int z, CompoundTag tag) {

    public static ChunkDataSyncPacket decode(FriendlyByteBuf buf) {
        return new ChunkDataSyncPacket(buf.readInt(), buf.readInt(), buf.readNbt());
    }

    public static void encode(ChunkDataSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.x);
        buf.writeInt(msg.z);
        buf.writeNbt(msg.tag);
    }

    public static void handler(ChunkDataSyncPacket msg, Supplier<NetworkEvent.Context> context) {
        if (msg != null) {
            context.get().enqueueWork(() -> {
                if (!context.get().getDirection().getReceptionSide().isServer()) {
                    if (Minecraft.getInstance().level.hasChunk(msg.x, msg.z)) {
                        ChunkPos chunkPos = new ChunkPos(msg.x, msg.z);
                        IChunkAccessor chunkAccessor = IChunkAccessor.fromLevelChunk(Minecraft.getInstance().level.getChunk(msg.x, msg.z));
                        chunkAccessor.setSonaCompoundTag(msg.tag());
                        int startX = chunkPos.getMinBlockX();
                        int startZ = chunkPos.getMinBlockZ();
                        int minY = Minecraft.getInstance().level.getMinBuildHeight();
                        int maxY = Minecraft.getInstance().level.getMaxBuildHeight();
                        for (int y = minY; y < maxY; y += 16) {
                            Minecraft.getInstance().levelRenderer.setSectionDirty(
                                    (startX >> 4), (y >> 4), (startZ >> 4)
                            );
                        }
                    }

                }
            });
        }
        context.get().setPacketHandled(true);
    }

}
