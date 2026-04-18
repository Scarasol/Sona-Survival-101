package com.scarasol.sona.client.renderer;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraftforge.fml.common.Mod;

/**
 * @author Scarasol
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InfectionFogRenderer {
    private static final float EXPONENTIAL_FOG_START = -512.0F;
    private static final float INFECTION_FOG_RESPONSE = 3.0F;
    private static final float INFECTION_FOG_SMOOTHING_SPEED = 5.0F;
    private static float infectionFogWeight = 0.0F;
    private static long infectionFogLastUpdateNanos = 0L;
    private static Vec3 infectionFogLastColor = Vec3.ZERO;

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !InfectionManager.canChunkInfection(level) || event.getType() != FogType.NONE) {
            return;
        }

        Vec3 pos = event.getCamera().getPosition();
        float weight = getCurrentInfectionFogWeight(level, pos);
        if (weight <= 0.001F) {
            return;
        }

        float vanillaFar = event.getFarPlaneDistance();
        if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(vanillaFar);
        } else {
            event.setNearPlaneDistance(encodeExponentialFogDensity(getInfectionFogDensity(weight)));
            event.setFarPlaneDistance(vanillaFar);
        }
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !InfectionManager.canChunkInfection(level) || event.getCamera().getFluidInCamera() != FogType.NONE) {
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
        RenderSystem.setShaderFogColor(event.getRed(), event.getGreen(), event.getBlue(), 1.0F);
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
        float weight = Mth.clamp((float) (infectionLevel / 100.0D), 0.0F, 1.0F);
        return (1.0F - (float) Math.exp(-INFECTION_FOG_RESPONSE * weight))
                / (1.0F - (float) Math.exp(-INFECTION_FOG_RESPONSE));
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

    private static float getInfectionFogDensity(float weight) {
        return 0.0008F * weight + 0.0095F * weight * weight;
    }

    private static float encodeExponentialFogDensity(float density) {
        return EXPONENTIAL_FOG_START - density;
    }
}
