package com.scarasol.sona.compat.lostcities;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
public class LostCitiesCompat {

    @Nullable
    public static ResourceLocation findCityStructure(WorldGenLevel level, BlockPos pos) {
        IDimensionInfo dimensionInfo;
        try {
            dimensionInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        } catch (NullPointerException e) {
            return null;
        }
        if (dimensionInfo == null) {
            return null;
        }
        ChunkCoord chunkCoord = new ChunkCoord(level.getLevel().dimension(), pos.getX() >> 4, pos.getZ() >> 4);
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkCoord, dimensionInfo);
        if (info == null) {
            return null;
        }

        ResourceLocation buildingId = info.getBuildingId();
        if (buildingId != null) {
            return buildingId;
        }

        ILostChunkInfo.MultiBuildingInfo multiBuildingInfo = info.getMultiBuildingInfo();
        if (multiBuildingInfo != null && multiBuildingInfo.buildingType() != null) {
            return multiBuildingInfo.buildingType();
        }

        String buildingType = info.getBuildingType();
        if (buildingType != null && !buildingType.isEmpty()) {
            ResourceLocation parsed = ResourceLocation.tryParse(buildingType);
            if (parsed != null) {
                return parsed;
            }
            return new ResourceLocation(LostCities.MODID, buildingType);
        }

        return null;
    }
}
