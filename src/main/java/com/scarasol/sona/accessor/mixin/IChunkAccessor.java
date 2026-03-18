package com.scarasol.sona.accessor.mixin;

import com.scarasol.sona.network.ChunkDataSyncPacket;
import com.scarasol.sona.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.network.PacketDistributor;


/**
 * @author Scarasol
 */
public interface IChunkAccessor {

    String COMPOUND_TAG_NAME = "SonaDataCompoundTag";

    static IChunkAccessor fromLevelChunk(ChunkAccess levelChunk) {
        return (IChunkAccessor)levelChunk;
    }


    CompoundTag getSonaCompoundTag();

    void setSonaCompoundTag(CompoundTag sonaCompoundTag);

    void setNeedSync(boolean needSync);

    boolean isNeedSync();

    default void loadChunkData(CompoundTag tag) {
        if (tag.contains(COMPOUND_TAG_NAME, Tag.TAG_COMPOUND)) {
            setSonaCompoundTag(tag.getCompound(COMPOUND_TAG_NAME));
        }
    }

    default void saveChunkData(CompoundTag tag) {
        CompoundTag compoundTag = getSonaCompoundTag();
        if (!compoundTag.isEmpty()) {
            tag.put(COMPOUND_TAG_NAME, compoundTag);
        }
    }

    default void syncChunkData(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ChunkDataSyncPacket(chunkPos.x, chunkPos.z, getSonaCompoundTag()));
    }

    default void syncChunkData(ServerLevel serverLevel, ChunkPos chunkPos) {
        double distance = serverLevel.getServer().getPlayerList().getSimulationDistance() + 1;
        double distanceSqr = distance * distance * 256;
        serverLevel.getPlayers(serverPlayer -> getChunkDistanceSqr(serverPlayer, chunkPos) < distanceSqr)
                .forEach(serverPlayer -> syncChunkData(serverPlayer, chunkPos));
    }

    default double getChunkDistanceSqr(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        double x = serverPlayer.getX() - chunkPos.getMiddleBlockX();
        double z = serverPlayer.getZ() - chunkPos.getMiddleBlockZ();
        return x * x + z * z;
    }
}
