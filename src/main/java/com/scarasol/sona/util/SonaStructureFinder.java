package com.scarasol.sona.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.scarasol.sona.SonaMod;
import com.scarasol.sona.compat.lostcities.LostCitiesCompat;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author Scarasol
 */
public class SonaStructureFinder {

    @Nullable
    public static Holder<Structure> getStructureHolder(ServerLevel level, ResourceLocation id) {
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return reg.getHolder(ResourceKey.create(Registries.STRUCTURE, id)).orElse(null);
    }


    public static List<ResourceLocation> getAllStructure(WorldGenLevel level, BlockPos pos) {
        ChunkPos cpos = new ChunkPos(pos);

        // 至少要 STRUCTURE_REFERENCES，才能通过 references 找到“起点不在本 chunk”的结构
        ChunkAccess chunk = level.getChunk(cpos.x, cpos.z, ChunkStatus.STRUCTURE_REFERENCES, false);
        if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) {
            return Lists.newArrayList();
        }

        // 1. 先构造“当前 ChunkSection”的 AABB
        int y = pos.getY();
        // 按需可以 clamp 一下，避免极端情况越界
        int minBuild = chunk.getMinBuildHeight();
        int maxBuild = chunk.getMaxBuildHeight() - 1;
        if (y < minBuild) {
            y = minBuild;
        }
        if (y > maxBuild) {
            y = maxBuild;
        }

        int sectionY = SectionPos.blockToSectionCoord(y);
        int minX = cpos.getMinBlockX();
        int minZ = cpos.getMinBlockZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        int minY = SectionPos.sectionToBlockCoord(sectionY);
        int maxY = minY + 15;

        BoundingBox sectionBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        // 2. 收集候选 StructureStart（你的原逻辑）
        Map<Structure, StructureStart> candidates = Maps.newHashMap(chunk.getAllStarts());

        for (Map.Entry<Structure, LongSet> e : chunk.getAllReferences().entrySet()) {
            Structure structure = e.getKey();
            LongSet refs = e.getValue();
            LongIterator it = refs.iterator();
            while (it.hasNext()) {
                long packed = it.nextLong();
                int sx = ChunkPos.getX(packed);
                int sz = ChunkPos.getZ(packed);
                ChunkAccess startChunk = level.getChunk(sx, sz, ChunkStatus.STRUCTURE_STARTS, false);
                if (startChunk == null || !startChunk.getStatus().isOrAfter(ChunkStatus.STRUCTURE_STARTS)) {
                    continue;
                }
                StructureStart start = startChunk.getStartForStructure(structure);
                if (start != null && start.isValid()) {
                    candidates.put(structure, start);
                }
            }
        }

        // 3. 用当前 ChunkSection 的 AABB 和每个 piece 做碰撞检测
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<ResourceLocation> result = Lists.newArrayList();

        for (Map.Entry<Structure, StructureStart> entry : candidates.entrySet()) {
            Structure structure = entry.getKey();
            StructureStart start = entry.getValue();

            if (!start.isValid()) {
                continue;
            }

            boolean hit = false;

            // 先可以粗筛：整体结构 AABB 不跟 section 相交就直接跳过
            if (!start.getBoundingBox().intersects(sectionBox)) {
                continue;
            }

            // 再对每个 piece 做精细检测
            for (var piece : start.getPieces()) {
                if (piece.getBoundingBox().intersects(sectionBox)) {
                    hit = true;
                    break;
                }
            }

            if (hit) {
                ResourceLocation id = reg.getKey(structure);
                if (id != null) {
                    result.add(id);
                }
            }
        }
        if (ModList.get().isLoaded("lostcities")) {
            ResourceLocation resourceLocation = LostCitiesCompat.findCityStructure(level, pos);
            if (resourceLocation != null) {
                result.add(resourceLocation);
            }
        }
        return result;
    }



    public static boolean structureCoversBlockPos(ServerLevel level, BlockPos blockPos, ResourceLocation id) {
        Holder<Structure> structure = getStructureHolder(level, id);
        if (structure == null) {
            return false;
        }
        ChunkPos pos = new ChunkPos(blockPos);
        ChunkAccess acc = level.getChunk(pos.x, pos.z, ChunkStatus.EMPTY, false);
        if (acc == null) {
            return false;
        }
        if (!acc.getStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) {
            return false;
        }
        StructureManager sm = level.structureManager();

        StructureStart s = sm.getStructureWithPieceAt(blockPos, structure.value());
        return s.isValid();
    }

    public static boolean structureCoversChunk(ServerLevel level, ChunkPos pos, ResourceLocation id) {

        Holder<Structure> structure = getStructureHolder(level, id);
        if (structure == null) {
            return false;
        }

        ServerChunkCache cache = level.getChunkSource();
        ChunkAccess acc = cache.getChunk(pos.x, pos.z, ChunkStatus.EMPTY, false);
        if (acc == null) {
            return false;
        }
        if (!acc.getStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) {
            return false;
        }
        LongSet refs = acc.getReferencesForStructure(structure.value());
        return !refs.isEmpty();
    }

}
