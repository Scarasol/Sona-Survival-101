package com.scarasol.sona.accessor;

import com.scarasol.sona.accessor.mixin.IServerLevelAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.network.MapVariables;
import com.scarasol.sona.util.ExpressionParser;
import com.scarasol.sona.util.SonaPerlinNoise;
import com.scarasol.sona.util.SonaStructureFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

/**
 * @author Scarasol
 */
public interface IInfectionZoneManager {

    String INFECTION_TAG_NAME = "SonaInfectionLevel";
    String INFECTION_TAG_TIME = "SonaInfectionTime";

    default boolean canChunkInfection(Level level) {
        return CommonConfig.INFECTION_OPEN.get() && CommonConfig.INFECTED_ZONE_OPEN.get() && level.dimensionTypeId().equals(BuiltinDimensionTypes.OVERWORLD);
    }

    default BlockPos calculateZeroZone(BlockPos pos) {
        int distance = CommonConfig.INFECTED_ZONE_ZERO_DISTANCE.get();
        if (distance == 0) {
            return pos;
        } else {
            double angle = new Random().nextDouble() * Math.PI * 2;
            int dx = (int) Math.round(Math.cos(angle) * distance);
            int dz = (int) Math.round(Math.sin(angle) * distance);
            return new BlockPos(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
        }
    }

    default ChunkPos chunkPosOffset(WorldGenLevel level, ChunkPos chunkPos) {
        MapVariables mapData = MapVariables.get(level);
        BlockPos zeroBlockPos = mapData.getZeroChunk();
        if (zeroBlockPos == null) {
            LevelData levelData = level.getLevelData();
            zeroBlockPos = calculateZeroZone(new BlockPos(levelData.getXSpawn(), levelData.getYSpawn(), levelData.getZSpawn()));
            mapData.setZeroChunk(zeroBlockPos);
        }
        ChunkPos zeroChunk = new ChunkPos(zeroBlockPos);
        return new ChunkPos(chunkPos.x - zeroChunk.x, chunkPos.z - zeroChunk.z);
    }

    default int initializeInfectionZone(WorldGenLevel level, ChunkPos chunkPos) {
        int mode = CommonConfig.INFECTED_ZONE_GENERATION.get();

        chunkPos = chunkPosOffset(level, chunkPos);
        ServerLevel serverLevel = level.getLevel();
        if (mode != 0) {
            SonaPerlinNoise noise = IServerLevelAccessor.fromServerLevel(serverLevel).getSonaPerlinNoise();
            double baseX = chunkPos.x * 0.037;
            double baseY = chunkPos.z * 0.037;

            double base = noise.perlin(baseX, baseY);


            double detailX = chunkPos.x * 0.17;
            double detailY = chunkPos.z * 0.17;
            double detail = noise.perlin(detailX, detailY);

            double val = base * 0.8 + detail * 0.2;
            double fx = baseX * 0.8 + detailX * 0.2;
            double fy = baseY * 0.8 + detailY * 0.2;


            val = noise.emphasizeExtremes(val, 3);


            if (mode == 1) {
                val = noise.applyCenterSuppression(val, fx, fy, 0.15);
            } else {
                val = noise.applyDistanceRemap(val, fx, fy);
            }


            return (int) (val * 100);
        }
        String expression = CommonConfig.INFECTED_ZONE_CUSTOM_GENERATION.get();
        expression = expression.replace("T", String.valueOf(serverLevel.getDayTime() / 24000 + 1));
        double x = chunkPos.x * 16;
        double z = chunkPos.z * 16;
        expression = expression.replace("D", String.valueOf(Math.sqrt(x * x + z * z)));

        return (int) ExpressionParser.eval(expression);
    }

    int initializeInfectionZone(WorldGenLevel level, BlockPos blockPos);

    void calculateInfectionZone(ServerLevel level, ChunkPos chunkPos);

    void setZoneInfection(ServerLevel level, int chunkInfection, BlockPos blockPos);

    int getZoneInfection(Level level, BlockPos blockPos, boolean ignoreBlock);

    long getZoneInfectionTime(CompoundTag tag);

    default int getBlockInfection(Level level, BlockPos blockPos) {
        if (level.isClientSide() && !Minecraft.getInstance().isSameThread()) {
            return -1;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        String id = ForgeRegistries.BLOCKS.getKey(block).toString();
        List<String> blockInfo = CommonConfig.INFECTED_ZONE_BLOCK.get();
        int index = CommonConfig.findIndex(id, blockInfo);
        if (index != -1) {
            String[] info = blockInfo.get(index).split(",");
            if (info.length >= 2) {
                return Integer.parseInt(info[1].trim());
            }
        }


        return -1;
    }

    default int calculateStructureInfection(WorldGenLevel level, BlockPos blockPos) {
        List<String> structuresInfo = CommonConfig.INFECTED_ZONE_STRUCTURE.get();
        if (structuresInfo.isEmpty()) {
            return -1;
        }
        List<ResourceLocation> structures = SonaStructureFinder.getAllStructure(level, blockPos);
        int infection = -1;
        if (!structures.isEmpty()) {

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

        }
        return Math.min(Math.max(infection, -1), 100);
    }

    double getAveInfectionZone(ServerLevel level, BlockPos blockPos);

    default boolean canMobSpawn(ServerLevel level, Mob mob, BlockPos blockPos, MobSpawnType spawnType) {
        if (canChunkInfection(level)) {
            List<String> infectedZoneMob = CommonConfig.INFECTED_ZONE_MOB.get();
            if (!infectedZoneMob.isEmpty()) {
                switch (spawnType) {
                    case SPAWN_EGG, SPAWNER, CONVERSION, COMMAND, BUCKET, BREEDING, DISPENSER:
                        return true;
                    default:
                }
                String id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();
                int index = CommonConfig.findIndex(id, infectedZoneMob);
                if (index != -1) {
                    String[] info = infectedZoneMob.get(index).split(",");
                    if (info.length > 2) {
                        int infectionLevel = getZoneInfection(level, blockPos, false);
                        return infectionLevel >= Integer.parseInt(info[1].trim()) && infectionLevel <= Integer.parseInt(info[2].trim());
                    }
                }
            }

        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    double getAveZoneInfectionInRender(Level level, Vec3 position);

    @OnlyIn(Dist.CLIENT)
    default Vec3 getInfectionZoneColor(Vec3 oldColor, Vec3 pos, Level level, Vec3 newColor) {
        BlockPos blockPos = BlockPos.containing(pos);

        double infection = getAveZoneInfectionInRender(level, blockPos.getCenter());


        double ratio = Mth.clamp(infection / 100.0D, 0.0D, 1.0D);

        double finalR = Mth.lerp(ratio, oldColor.x, newColor.x);
        double finalG = Mth.lerp(ratio, oldColor.y, newColor.y);
        double finalB = Mth.lerp(ratio, oldColor.z, newColor.z);

        return new Vec3(finalR, finalG, finalB);
    }

}
