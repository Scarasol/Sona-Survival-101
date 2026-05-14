package com.scarasol.sona.client.renderer;

import com.mojang.blaze3d.shaders.FogShape;
import com.scarasol.sona.compat.ShaderCompatUtil;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Scarasol
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InfectionFogRenderer {
    private static final float INFECTION_FOG_SMOOTHING_SPEED = 5.0F;
    private static final float MAX_TERRAIN_FOG_REDUCTION = 0.82F;
    private static final float MAX_SKY_FOG_REDUCTION = 0.55F;
    private static final float MIN_TERRAIN_FAR_DISTANCE = 24.0F;
    private static final float MIN_SKY_FAR_DISTANCE = 72.0F;
    private static final float MAX_TERRAIN_NEAR_RATIO = 0.22F;
    private static final float MIN_TERRAIN_NEAR_RATIO = 0.08F;
    private static float infectionFogWeight = 0.0F;
    private static long infectionFogLastUpdateNanos = 0L;
    private static Vec3 infectionFogLastColor = Vec3.ZERO;

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !InfectionManager.canChunkInfection(level) || event.getType() != FogType.NONE || (ModList.get().isLoaded("oculus") && ShaderCompatUtil.isShaderActive())) {
            return;
        }

        Vec3 pos = event.getCamera().getPosition();
        float weight = getCurrentInfectionFogWeight(level, pos);
        if (weight <= 0.001F) {
            return;
        }

        float vanillaNear = event.getNearPlaneDistance();
        float vanillaFar = event.getFarPlaneDistance();
        if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(getSkyFogFar(vanillaFar, weight));
        } else {
            float fogStrength = getFogStrength(weight);
            float newFar = getTerrainFogFar(vanillaFar, fogStrength);
            event.setNearPlaneDistance(getTerrainFogNear(vanillaNear, newFar, fogStrength));
            event.setFarPlaneDistance(newFar);
        }
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !InfectionManager.canChunkInfection(level) || event.getCamera().getFluidInCamera() != FogType.NONE || (ModList.get().isLoaded("oculus") && ShaderCompatUtil.isShaderActive())) {
            return;
        }

        Vec3 pos = event.getCamera().getPosition();
        float weight = getCurrentInfectionFogWeight(level, pos);
        if (weight <= 0.001F) {
            return;
        }

        Vec3 color = getCurrentInfectionFogColor(level, pos);
        if (color == null) {
            return;
        }

        float colorWeight = Mth.clamp(weight * 0.94F, 0.0F, 0.94F);
        event.setRed(Mth.lerp(colorWeight, event.getRed(), (float) color.x));
        event.setGreen(Mth.lerp(colorWeight, event.getGreen(), (float) color.y));
        event.setBlue(Mth.lerp(colorWeight, event.getBlue(), (float) color.z));
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetInfectionFogSmoothing();
    }

    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            resetInfectionFogSmoothing();
        }
    }

    private static float getVisualInfectionFogWeight(double infectionLevel) {
        float infection = Mth.clamp((float) infectionLevel, 0.0F, 100.0F);
        if (infection < 30.0F) {
            float low = infection / 30.0F;
            return 0.02F * low * low;
        }

        float visible = Mth.clamp((infection - 30.0F) / 70.0F, 0.0F, 1.0F);
        float high = Mth.clamp((infection - 56.0F) / 44.0F, 0.0F, 1.0F);
        float easedVisible = visible * visible * (3.0F - 2.0F * visible);
        float easedHigh = high * high * (3.0F - 2.0F * high);
        return Mth.clamp(0.02F + 0.58F * easedVisible + 0.34F * easedVisible * easedHigh, 0.0F, 0.94F);
    }

    private static float smoothInfectionFogWeight(float targetWeight) {
        long now = System.nanoTime();
        if (infectionFogLastUpdateNanos == 0L) {
            infectionFogLastUpdateNanos = now;
            infectionFogWeight = targetWeight;
            return infectionFogWeight;
        }

        float deltaSeconds = Math.min((now - infectionFogLastUpdateNanos) / 1_000_000_000.0F, 1.0F);
        infectionFogLastUpdateNanos = now;

        float alpha = 1.0F - (float) Math.exp(-INFECTION_FOG_SMOOTHING_SPEED * deltaSeconds);
        infectionFogWeight = Mth.lerp(alpha, infectionFogWeight, targetWeight);
        if (targetWeight <= 0.001F && infectionFogWeight <= 0.001F) {
            infectionFogWeight = 0.0F;
        }
        return infectionFogWeight;
    }

    private static void resetInfectionFogSmoothing() {
        infectionFogWeight = 0.0F;
        infectionFogLastUpdateNanos = 0L;
        infectionFogLastColor = Vec3.ZERO;
    }

    private static float getCurrentInfectionFogWeight(ClientLevel level, Vec3 pos) {
        double infectionLevel = InfectionManager.getAveZoneInfectionInRender(level, pos);
        float targetWeight = getVisualInfectionFogWeight(infectionLevel);
        if (targetWeight <= 0.001F) {
            clearInfectionFogForVanillaFog();
            return 0.0F;
        }
        return smoothInfectionFogWeight(targetWeight);
    }

    private static Vec3 getCurrentInfectionFogColor(ClientLevel level, Vec3 pos) {
        Vec3 color = InfectionManager.getInfectionChunkFogColor(Vec3.ZERO, pos, level);
        if (color != null) {
            infectionFogLastColor = color;
            return color;
        }
        return infectionFogLastColor == Vec3.ZERO ? null : infectionFogLastColor;
    }

    private static void clearInfectionFogForVanillaFog() {
        infectionFogWeight = 0.0F;
        infectionFogLastUpdateNanos = System.nanoTime();
        infectionFogLastColor = Vec3.ZERO;
    }

    private static float getFogStrength(float weight) {
        return 1.0F - (float) Math.exp(-2.4F * weight);
    }

    private static float getTerrainFogFar(float vanillaFar, float fogStrength) {
        float targetFar = vanillaFar * (1.0F - MAX_TERRAIN_FOG_REDUCTION * fogStrength);
        return Math.min(vanillaFar, Math.max(MIN_TERRAIN_FAR_DISTANCE, targetFar));
    }

    private static float getTerrainFogNear(float vanillaNear, float newFar, float fogStrength) {
        float nearRatio = Mth.lerp(fogStrength, MAX_TERRAIN_NEAR_RATIO, MIN_TERRAIN_NEAR_RATIO);
        float targetNear = Math.min(vanillaNear, newFar * nearRatio);
        return Mth.lerp(fogStrength, vanillaNear, targetNear);
    }

    private static float getSkyFogFar(float vanillaFar, float weight) {
        float skyStrength = Mth.clamp(getFogStrength(weight), 0.0F, 1.0F);
        float targetFar = vanillaFar * (1.0F - MAX_SKY_FOG_REDUCTION * skyStrength);
        return Math.min(vanillaFar, Math.max(MIN_SKY_FAR_DISTANCE, targetFar));
    }
}
