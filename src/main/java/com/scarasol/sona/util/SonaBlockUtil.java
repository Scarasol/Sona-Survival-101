package com.scarasol.sona.util;

import com.scarasol.sona.SonaMod;
import com.scarasol.sona.accessor.mixin.ICompoundContainerAccessor;
import com.scarasol.sona.event.SonaEventHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * @author Scarasol
 */
public class SonaBlockUtil {

    @Nullable
    public static BlockPos tryFindContainerBlockPos(AbstractContainerMenu menu) {
        Container container = menu.slots.get(0).container;
        if (container instanceof ICompoundContainerAccessor compoundContainer) {
            container = compoundContainer.getContainer1();
        }
        if (container instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockPos();
        }
        return getPosFromContainerLevelAccess(menu);
    }

    @Nullable
    public static BlockPos getPosFromContainerLevelAccess(AbstractContainerMenu menu, Class<?> cls) {
        try {
            for (Field f : cls.getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(menu);
                if (v instanceof ContainerLevelAccess acc) {
                    BlockPos blockPos = acc.evaluate((lvl, bp) -> bp, null);
                    if (blockPos != null) {
                        return blockPos;
                    }
                }
                if (v instanceof BlockEntity be) {
                    return be.getBlockPos();
                }
            }
        } catch (Throwable ignored) {
            SonaMod.LOGGER.warn("Error ContainerLevelAccess Reflect!");
        }
        return null;
    }

    @Nullable
    private static BlockPos getPosFromContainerLevelAccess(AbstractContainerMenu menu) {
        Class<?> c = menu.getClass();
        while (c != null && c != AbstractContainerMenu.class) {
            BlockPos pos = getPosFromContainerLevelAccess(menu, c);
            if (pos != null) {
                return pos;
            }
            c = c.getSuperclass();
        }
        return null;
    }

    public static double getMinDistanceSqrChunkToBlock(BlockPos blockPos, ChunkPos chunkPos) {
        int minX = chunkPos.getMinBlockX();
        int maxX = chunkPos.getMaxBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int maxZ = chunkPos.getMaxBlockZ();

        int x = blockPos.getX();
        int z = blockPos.getZ();


        int nearestX = Mth.clamp(x, minX, maxX);
        int nearestZ = Mth.clamp(z, minZ, maxZ);

        int dx = x - nearestX;
        int dz = z - nearestZ;
        return dx * dx + dz * dz;
    }


    public static double getDistanceSqToChunkCenter(BlockPos blockPos, ChunkPos chunkPos) {
        double centerX = chunkPos.getMiddleBlockX();
        double centerZ = chunkPos.getMiddleBlockZ();


        double dx = blockPos.getX() - centerX;
        double dz = blockPos.getZ() - centerZ;

        return dx * dx + dz * dz;
    }

    @Nullable
    public static ChunkAccess getChunk(Level level, ChunkPos chunkPos) {
        ChunkAccess chunkAccess = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.EMPTY, false);

        if (chunkAccess != null && chunkAccess.getStatus() == ChunkStatus.FULL) {
            if (chunkAccess instanceof ImposterProtoChunk imposterProtoChunk) {
                chunkAccess =  imposterProtoChunk.getWrapped();
            }
        }
        return chunkAccess;
    }


}
