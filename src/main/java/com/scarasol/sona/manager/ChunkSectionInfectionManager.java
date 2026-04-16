package com.scarasol.sona.manager;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        chunkAccess.setUnsaved(true);
        return infectionLevel;
    }

    public void initializeChunkInfectionZone(WorldGenLevel level, ChunkAccess chunkAccess, int infectionLevelInSurface, BlockPos surfacePos) {
        IChunkAccessor chunkAccessor = IChunkAccessor.fromLevelChunk(chunkAccess);
        CompoundTag tag = chunkAccessor.getSonaCompoundTag();
        long gameTime = level.getLevel().getGameTime();
        int minHeight = chunkAccess.getMinBuildHeight();
        int maxHeight = chunkAccess.getMaxBuildHeight();
        int sectionCount = (maxHeight - minHeight) / 16;
        if (sectionCount <= 0) {
            return;
        }

        int centerX = chunkAccess.getPos().getMiddleBlockX();
        int centerZ = chunkAccess.getPos().getMiddleBlockZ();
        int[] heights = new int[sectionCount];
        int[] infectionSources = getStructureInfections(level, chunkAccess, sectionCount);
        int[] nonEmptyBlockCounts = new int[sectionCount];

        for (int index = 0; index < sectionCount; index++) {
            int height = minHeight + index * 16;
            heights[index] = height;
            ILevelChunkSection chunkSection = ILevelChunkSection.fromLevelChunk(chunkAccess.getSection(chunkAccess.getSectionIndex(height)));
            nonEmptyBlockCounts[index] = chunkSection.getSonaNonEmptyBlockCount();
        }

        int surfaceSectionHeight = Math.floorDiv(surfacePos.getY(), 16) * 16;
        int surfaceIndex = Math.max(0, Math.min(sectionCount - 1, (surfaceSectionHeight - minHeight) / 16));
        infectionSources[surfaceIndex] = Math.max(infectionSources[surfaceIndex], infectionLevelInSurface);

        int[] upwardInfections = propagateInfection(infectionSources, nonEmptyBlockCounts, true);
        int[] downwardInfections = propagateInfection(infectionSources, nonEmptyBlockCounts, false);

        for (int index = 0; index < sectionCount; index++) {
            int infectionLevel = Math.max(upwardInfections[index], downwardInfections[index]);
            setZoneInfection(tag, SonaEventHooks.getInitChunkInfection(new BlockPos(centerX, heights[index], centerZ), level.getLevel(), infectionLevel), heights[index] / 16, gameTime);
        }
        chunkAccess.setUnsaved(true);
    }

    private int[] getStructureInfections(WorldGenLevel level, ChunkAccess chunkAccess, int sectionCount) {
        int[] structureInfections = new int[sectionCount];
        List<String> structuresInfo = CommonConfig.INFECTED_ZONE_STRUCTURE.get();
        if (structuresInfo.isEmpty()) {
            Arrays.fill(structureInfections, -1);
            return structureInfections;
        }

        List<ResourceLocation>[] structuresBySection = SonaStructureFinder.getAllStructureBySection(level, chunkAccess);
        for (int index = 0; index < sectionCount; index++) {
            structureInfections[index] = getStructureInfection(structuresBySection[index], structuresInfo);
        }
        return structureInfections;
    }

    private int getStructureInfection(List<ResourceLocation> structures, List<String> structuresInfo) {
        if (structuresInfo.isEmpty() || structures.isEmpty()) {
            return -1;
        }
        int infection = -1;
        for (ResourceLocation resourceLocation : structures) {
            int index = CommonConfig.findIndex(resourceLocation.toString(), structuresInfo);
            if (index == -1) {
                continue;
            }
            String[] info = structuresInfo.get(index).split(",");
            if (info.length < 2) {
                continue;
            }
            infection = Math.max(Integer.parseInt(info[1].trim()), infection);
        }
        return Math.min(infection, 100);
    }

    private int[] propagateInfection(int[] infectionSources, int[] nonEmptyBlockCounts, boolean ascending) {
        int[] infections = new int[infectionSources.length];
        if (ascending) {
            int infectionLevel = 0;
            for (int index = 0; index < infectionSources.length; index++) {
                infectionLevel = calculatePropagatedInfection(infectionLevel, infectionSources[index], nonEmptyBlockCounts[index]);
                infections[index] = infectionLevel;
            }
        } else {
            int infectionLevel = 0;
            for (int index = infectionSources.length - 1; index >= 0; index--) {
                infectionLevel = calculatePropagatedInfection(infectionLevel, infectionSources[index], nonEmptyBlockCounts[index]);
                infections[index] = infectionLevel;
            }
        }

        return infections;
    }

    private int calculatePropagatedInfection(int previousInfection, int structureInfection, int nonEmptyBlockCount) {
        if (structureInfection >= 0) {
            return structureInfection;
        }
        return (int) Math.max(0, previousInfection * (0.9 - nonEmptyBlockCount / 4096D * 0.5));
    }

    @Override
    public void calculateInfectionZone(ServerLevel level, ChunkPos chunkPos) {
        ChunkAccess chunkAccess = SonaBlockUtil.getChunk(level, chunkPos);
        if (chunkAccess != null) {
            CompoundTag tag = IChunkAccessor.fromLevelChunk(chunkAccess).getSonaCompoundTag();
            long lastUpdateTime = getZoneInfectionTime(tag);
            String expression = CommonConfig.INFECTED_ZONE_INCREASEMENT.get();
            ChunkPos chunkOffset = chunkPosOffset(level, chunkPos);
            double x = chunkOffset.x * 16;
            double z = chunkOffset.z * 16;
            double distance = Math.sqrt(x * x + z * z);
            int day = (int) ((level.getGameTime() - lastUpdateTime) / 24000);
            if (day > 0) {
                List<NeighborInfection> neighborInfections = expression.contains("A") ? getInitializedNeighborInfections(level, chunkPos) : List.of();
                for (int i = chunkAccess.getMinBuildHeight(); i < chunkAccess.getMaxBuildHeight(); i += 16) {
                    int infection = calculateInfectionZone(level, new BlockPos(chunkPos.getMiddleBlockX(), i, chunkPos.getMiddleBlockZ()), distance, lastUpdateTime, expression, neighborInfections);
                    setZoneInfection(tag, SonaEventHooks.getCalculateChunkInfection(chunkPos.getMiddleBlockPosition(i), level, getZoneInfection(tag, i / 16)) + infection, i / 16, level.getGameTime());
                }
                chunkAccess.setUnsaved(true);
            }

        }
    }


    public int calculateInfectionZone(ServerLevel level, BlockPos blockPos, double distance, long lastUpdateTime) {
        String expression = CommonConfig.INFECTED_ZONE_INCREASEMENT.get();
        List<NeighborInfection> neighborInfections = expression.contains("A") ? getInitializedNeighborInfections(level, new ChunkPos(blockPos)) : List.of();
        return calculateInfectionZone(level, blockPos, distance, lastUpdateTime, expression, neighborInfections);
    }

    private int calculateInfectionZone(ServerLevel level, BlockPos blockPos, double distance, long lastUpdateTime, String expression, List<NeighborInfection> neighborInfections) {
        int originLevel = getZoneInfection(level, blockPos, true);
        int day = (int) ((level.getGameTime() - lastUpdateTime) / 24000);
        if (!expression.isEmpty() && day > 0) {
            expression = expression.replace("T", String.valueOf(day));
            expression = expression.replace("O", String.valueOf(originLevel));
            if (expression.contains("A")) {
                expression = expression.replace("A", String.valueOf(getAveInfectionZone(level, blockPos.getY() / 16, neighborInfections)));
            }

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
            chunkAccess.setUnsaved(true);
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
        return getAveInfectionZone(level, blockPos.getY() / 16, getInitializedNeighborInfections(level, chunkPos));
    }

    private double getAveInfectionZone(ServerLevel level, int height, List<NeighborInfection> neighborInfections) {
        double aveInfection = 0;
        int count = 0;
        for (NeighborInfection neighborInfection : neighborInfections) {
            double chunkInfection = getAveInfectionZone(level, neighborInfection, height);
            if (chunkInfection >= 0) {
                aveInfection += chunkInfection;
                count++;
            }
        }
        if (count > 0) {
            return aveInfection / count;
        }
        return 0;
    }

    private List<NeighborInfection> getInitializedNeighborInfections(ServerLevel level, ChunkPos chunkPos) {
        List<NeighborInfection> neighborInfections = new ArrayList<>(9);
        for (int dx = -1; dx < 2; dx++) {
            for (int dz = -1; dz < 2; dz++) {
                ChunkPos neighborChunkPos = new ChunkPos(chunkPos.x + dx, chunkPos.z + dz);
                ChunkAccess chunkAccess = SonaBlockUtil.getChunk(level, neighborChunkPos);
                if (chunkAccess != null) {
                    CompoundTag tag = IChunkAccessor.fromLevelChunk(chunkAccess).getSonaCompoundTag();
                    if (tag.contains(INFECTION_TAG_TIME)) {
                        neighborInfections.add(new NeighborInfection(neighborChunkPos, tag));
                    }
                }
            }
        }
        return neighborInfections;
    }

    private double getAveInfectionZone(ServerLevel level, NeighborInfection neighborInfection, int height) {
        double aveInfection = 0;
        int count = 0;
        for (int i = -1; i <= 1; i += 1) {
            int heightTemp = height + i;
            aveInfection += SonaEventHooks.getCalculateChunkInfection(neighborInfection.chunkPos().getMiddleBlockPosition(height * 16), level, getZoneInfection(neighborInfection.tag(), heightTemp));
            count++;
        }
        return aveInfection / count;
    }

    private record NeighborInfection(ChunkPos chunkPos, CompoundTag tag) {
    }

    @Override
    public double getAveZoneInfectionInRender(Level level, Vec3 position) {

        BlockPos blockPos = BlockPos.containing(position);
        int blockInfection = getBlockInfection(level, blockPos);
        if (blockInfection >= 0) {
            return blockInfection;
        }

        ChunkPos centerChunk = new ChunkPos(blockPos);

        double sigma = 8.0D;
        double twoSigmaSq = 2.0D * sigma * sigma;

        double totalInf = 0.0D;
        double totalWeight = 0.0D;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos cp = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);

                double distSq = SonaBlockUtil.getMinDistanceSqrChunkToBlock(blockPos, cp);

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
