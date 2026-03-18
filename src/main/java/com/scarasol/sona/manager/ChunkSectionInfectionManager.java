package com.scarasol.sona.manager;

import com.scarasol.sona.SonaMod;
import com.scarasol.sona.accessor.IInfectionZoneManager;
import com.scarasol.sona.accessor.mixin.IChunkAccessor;
import com.scarasol.sona.accessor.mixin.ILevelChunkSection;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.event.SonaEventHooks;
import com.scarasol.sona.util.ExpressionParser;
import com.scarasol.sona.util.SonaBlockUtil;
import com.scarasol.sona.util.SonaStructureFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Scarasol
 */
public class ChunkSectionInfectionManager implements IInfectionZoneManager {

    private ChunkSectionInfectionManager() {
    }

    private static final IInfectionZoneManager INSTANCE = new ChunkSectionInfectionManager();

    public static IInfectionZoneManager getInstance() {
        return INSTANCE;
    }

    @Override
    public int initializeInfectionZone(WorldGenLevel level, BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        ServerLevel serverLevel = level.getLevel();
        ChunkAccess chunkAccess = SonaBlockUtil.getChunk(serverLevel, chunkPos);
        if (chunkAccess == null) {
            return 0;
        }
        int surfaceHeight = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ());
        int infectionLevel = calculateStructureInfection(level, blockPos);
        if (infectionLevel < 0) {
            infectionLevel = initializeInfectionZone(level, chunkPos);
        }
        setZoneInfection(IChunkAccessor.fromLevelChunk(chunkAccess).getSonaCompoundTag(), infectionLevel, blockPos.getY() / 16, serverLevel.getGameTime());
        initializeChunkInfectionZone(level, chunkAccess, infectionLevel, new BlockPos(chunkPos.getMiddleBlockX(), surfaceHeight, chunkPos.getMiddleBlockZ()));
        return infectionLevel;
    }

    public void initializeChunkInfectionZone(WorldGenLevel level, ChunkAccess chunkAccess, int infectionLevelInSurface, BlockPos surfacePos) {
        int surfaceHeight = surfacePos.getY();
        int infectionLevel = infectionLevelInSurface;
        BlockPos blockPos = surfacePos;
        IChunkAccessor chunkAccessor = IChunkAccessor.fromLevelChunk(chunkAccess);
        CompoundTag tag = chunkAccessor.getSonaCompoundTag();
        long gameTime = level.getLevel().getGameTime();
        for (int i = surfaceHeight - 16; i >= chunkAccess.getMinBuildHeight(); i -= 16) {
            blockPos = blockPos.offset(0, -16, 0);
            infectionLevel = initInfectionLevel(level, chunkAccess, infectionLevel, blockPos, tag, gameTime, i);
        }
        blockPos = surfacePos;
        infectionLevel = infectionLevelInSurface;
        for (int i = surfaceHeight + 16; i < chunkAccess.getMaxBuildHeight(); i += 16) {
            blockPos = blockPos.offset(0, 16, 0);
            infectionLevel = initInfectionLevel(level, chunkAccess, infectionLevel, blockPos, tag, gameTime, i);
        }
    }

    private int initInfectionLevel(WorldGenLevel level, ChunkAccess chunkAccess, int infectionLevel, BlockPos blockPos, CompoundTag tag, long gameTime, int height) {
        ILevelChunkSection chunkSection = ILevelChunkSection.fromLevelChunk(chunkAccess.getSection(chunkAccess.getSectionIndex(height)));
        int count = chunkSection.getSonaNonEmptyBlockCount();
        int structureInfection = calculateStructureInfection(level, blockPos);
        if (structureInfection < 0) {
            infectionLevel = (int) Math.max(0, infectionLevel * (0.9 - count / 4096D * 0.5));
        } else {
            infectionLevel = structureInfection;
        }

        setZoneInfection(tag, SonaEventHooks.getInitChunkInfection(blockPos, level.getLevel(), infectionLevel), height / 16, gameTime);
        return infectionLevel;
    }

    @Override
    public void calculateInfectionZone(ServerLevel level, ChunkPos chunkPos) {
        ChunkAccess chunkAccess = SonaBlockUtil.getChunk(level, chunkPos);
        if (chunkAccess != null) {
            CompoundTag tag = IChunkAccessor.fromLevelChunk(chunkAccess).getSonaCompoundTag();
            long lastUpdateTime = getZoneInfectionTime(tag);
            ChunkPos chunkOffset = chunkPosOffset(level, chunkPos);
            double x = chunkOffset.x * 16;
            double z = chunkOffset.z * 16;
            double distance = Math.sqrt(x * x + z * z);
            int day = (int) ((level.getGameTime() - lastUpdateTime) / 24000);
            if (day > 0) {
                for (int i = chunkAccess.getMinBuildHeight(); i < chunkAccess.getMaxBuildHeight(); i += 16) {
                    int infection = calculateInfectionZone(level, new BlockPos(chunkPos.getMiddleBlockX(), i, chunkPos.getMiddleBlockZ()), distance, lastUpdateTime);
                    setZoneInfection(tag, SonaEventHooks.getCalculateChunkInfection(chunkPos.getMiddleBlockPosition(i), level, getZoneInfection(tag, i / 16)) + infection, i / 16, level.getGameTime());
                }
            }

        }
    }


    public int calculateInfectionZone(ServerLevel level, BlockPos blockPos, double distance, long lastUpdateTime) {
        int originLevel = getZoneInfection(level, blockPos, true);
        String expression = CommonConfig.INFECTED_ZONE_INCREASEMENT.get();
        int day = (int) ((level.getGameTime() - lastUpdateTime) / 24000);
        if (!expression.isEmpty() && day > 0) {
            expression = expression.replaceAll("T", String.valueOf(day));
            expression = expression.replaceAll("O", String.valueOf(originLevel));
            expression = expression.replaceAll("A", String.valueOf(getAveInfectionZone(level, blockPos)));

            expression = expression.replace("D", String.valueOf(distance));
            return (int) ExpressionParser.eval(expression);
        }
        return 0;
    }

    @Override
    public void setZoneInfection(ServerLevel level, int infectionLevel, BlockPos blockPos) {
        ChunkAccess chunkAccess = SonaBlockUtil.getChunk(level, new ChunkPos(blockPos));
        if (chunkAccess != null) {
            IChunkAccessor chunkAccessor = IChunkAccessor.fromLevelChunk(chunkAccess);
            setZoneInfection(chunkAccessor.getSonaCompoundTag(), infectionLevel, blockPos.getY() / 16, level.getGameTime());
        }
    }

    public void setZoneInfection(CompoundTag tag, int infectionLevel, int height, long gameTime) {

        tag.putInt(INFECTION_TAG_NAME + height, Math.max(0, Math.min(infectionLevel, 100)));
        tag.putLong(INFECTION_TAG_TIME, gameTime);
    }

    @Override
    public int getZoneInfection(Level level, BlockPos blockPos, boolean ignoreBlock) {
        if (canChunkInfection(level)) {
            if (!ignoreBlock) {
                int blockInfection = getBlockInfection(level, blockPos);
                if (blockInfection >= 0) {
                    return SonaEventHooks.getFullChunkInfection(blockPos, level, blockInfection);
                }
            }

            ChunkAccess chunkAccess = SonaBlockUtil.getChunk(level, new ChunkPos(blockPos));
            if (chunkAccess != null) {
                IChunkAccessor chunkAccessor = IChunkAccessor.fromLevelChunk(chunkAccess);
                CompoundTag tag = chunkAccessor.getSonaCompoundTag();
                if (!tag.contains(INFECTION_TAG_TIME)) {
                    if (level instanceof ServerLevel serverLevel) {

                        initializeInfectionZone(serverLevel, blockPos);
                    }
                }
                return SonaEventHooks.getFullChunkInfection(blockPos, level, getZoneInfection(tag, blockPos.getY() / 16));
            }
        }
        return 0;
    }

    public int getZoneInfection(CompoundTag tag, int height) {

        return tag.getInt(INFECTION_TAG_NAME + height);
    }

    @Override
    public long getZoneInfectionTime(CompoundTag tag) {
        return tag.getLong(INFECTION_TAG_TIME);
    }

    @Override
    public double getAveInfectionZone(ServerLevel level, BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        double aveInfection = 0;
        int count = 0;
        for (int dx = -1; dx < 2; dx++) {
            for (int dz = -1; dz < 2; dz++) {
                double chunkInfection = getAveInfectionZone(level, new ChunkPos(chunkPos.x + dx, chunkPos.z + dz), blockPos.getY() / 16);
                if (chunkInfection >= 0) {
                    aveInfection += chunkInfection;
                    count += 3;
                }
            }
        }
        if (count > 0) {
            return aveInfection / count;
        }
        return 0;
    }

    public double getAveInfectionZone(ServerLevel level, ChunkPos chunkPos, int height) {
        ChunkAccess chunkAccess = SonaBlockUtil.getChunk(level, chunkPos);
        double aveInfection = 0;
        if (chunkAccess != null) {
            for (int i = -1; i <= 1; i += 1) {
                int heightTemp = height + i;
                IChunkAccessor chunkAccessor = IChunkAccessor.fromLevelChunk(chunkAccess);
                CompoundTag tag = chunkAccessor.getSonaCompoundTag();
                if (!tag.contains(INFECTION_TAG_TIME)) {

                    initializeInfectionZone(level, chunkPos.getMiddleBlockPosition(height));
                }
                aveInfection += SonaEventHooks.getCalculateChunkInfection(chunkPos.getMiddleBlockPosition(height), level, getZoneInfection(tag, heightTemp));
            }
            return aveInfection / 3;
        }
        return -1;
    }

    @Override
    public double getAveZoneInfectionInRender(Level level, Vec3 position) {

        BlockPos blockPos = BlockPos.containing(position);
        int blockInfection = getBlockInfection(level, blockPos);
        if (blockInfection >= 0) {
            return blockInfection;
        }

        double x = position.x;
        double z = position.z;

        ChunkPos centerChunk = new ChunkPos(blockPos);

        double sigma = 4.0D;
        double twoSigmaSq = 2.0D * sigma * sigma;

        double totalInf = 0.0D;
        double totalWeight = 0.0D;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos cp = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);

                double cx = cp.getMiddleBlockX();
                double cz = cp.getMiddleBlockZ();

                double ddx = x - cx;
                double ddz = z - cz;
                double distSq = ddx * ddx + ddz * ddz;

                double weight = Math.exp(-distSq / twoSigmaSq);
                if (weight < 1.0E-6D) {
                    continue;
                }



                double infection = getZoneInfection(level, cp.getMiddleBlockPosition(blockPos.getY()), true);

                totalInf += infection * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0.0D) {
            return 0.0D;
        }

        return totalInf / totalWeight;
    }
}
