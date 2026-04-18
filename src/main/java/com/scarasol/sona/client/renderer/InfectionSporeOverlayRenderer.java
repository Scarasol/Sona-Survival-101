package com.scarasol.sona.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Scarasol
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InfectionSporeOverlayRenderer {
    private static final float SPORE_SMOOTHING_SPEED = 5.0F;
    private static float sporeWeight = 0.0F;
    private static long sporeLastUpdateNanos = 0L;

    @SubscribeEvent
    public static void onRenderInfectionSpores(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !InfectionManager.canChunkInfection(level)) {
            return;
        }

        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        double infectionLevel = InfectionManager.getAveZoneInfectionInRender(level, cameraPos);
        float weight = smoothSporeWeight(getVisualSporeWeight(infectionLevel));
        if (weight <= 0.001F) {
            return;
        }

        Vec3 color = InfectionManager.getInfectionChunkFogColor(Vec3.ZERO, cameraPos, level);
        if (color == null) {
            return;
        }

        renderInfectionSpores(event, level, color, weight);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetSporeSmoothing();
    }

    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            resetSporeSmoothing();
        }
    }

    private static float getVisualSporeWeight(double infectionLevel) {
        float weight = Mth.clamp((float) (infectionLevel / 100.0D), 0.0F, 1.0F);
        return weight * weight * (3.0F - 2.0F * weight);
    }

    private static float smoothSporeWeight(float targetWeight) {
        long now = System.nanoTime();
        if (sporeLastUpdateNanos == 0L) {
            sporeLastUpdateNanos = now;
            sporeWeight = targetWeight;
            return sporeWeight;
        }

        float deltaSeconds = Math.min((now - sporeLastUpdateNanos) / 1_000_000_000.0F, 1.0F);
        sporeLastUpdateNanos = now;

        float alpha = 1.0F - (float) Math.exp(-SPORE_SMOOTHING_SPEED * deltaSeconds);
        sporeWeight = Mth.lerp(alpha, sporeWeight, targetWeight);
        if (targetWeight <= 0.001F && sporeWeight <= 0.001F) {
            sporeWeight = 0.0F;
        }
        return sporeWeight;
    }

    private static void resetSporeSmoothing() {
        sporeWeight = 0.0F;
        sporeLastUpdateNanos = 0L;
    }

    private static void renderInfectionSpores(RenderGuiEvent.Pre event, ClientLevel level, Vec3 color, float weight) {
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        renderSporeParticles(event, color, width, height, level.getGameTime() + Minecraft.getInstance().getFrameTime(), weight);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderSporeParticles(RenderGuiEvent.Pre event, Vec3 color, int width, int height, float time, float weight) {
        int sporeCount = Mth.floor(Mth.lerp(weight, 16.0F, 54.0F));
        Vec3 sporeColor = new Vec3(
                Mth.clamp((float) color.x * 0.8F + 0.35F, 0.0F, 1.0F),
                Mth.clamp((float) color.y * 0.8F + 0.35F, 0.0F, 1.0F),
                Mth.clamp((float) color.z * 0.8F + 0.35F, 0.0F, 1.0F)
        );
        for (int i = 0; i < sporeCount; i++) {
            float baseX = hash01(i, 11);
            float baseY = hash01(i, 29);
            float speed = Mth.lerp(hash01(i, 47), 0.0014F, 0.0042F);
            float x = fract(baseX + time * speed + Mth.sin(time * 0.015F + i) * 0.012F) * width;
            float y = fract(baseY - time * speed * 0.72F + Mth.cos(time * 0.011F + i * 0.7F) * 0.010F) * height;
            int size = 1 + Mth.floor(hash01(i, 71) * 4.0F);
            int glowSize = size + 2;
            float pulse = 0.65F + 0.35F * Mth.sin(time * 0.09F + i * 1.7F);
            float alpha = Mth.clamp(weight * Mth.lerp(hash01(i, 97), 0.08F, 0.24F) * pulse, 0.0F, 0.26F);
            renderSmoothSpore(event, x, y, size, glowSize, argbFromColor(sporeColor, alpha * 0.22F), argbFromColor(sporeColor, alpha));
        }
    }

    private static void renderSmoothSpore(RenderGuiEvent.Pre event, float x, float y, int size, int glowSize, int glowColor, int coreColor) {
        int pixelX = Mth.floor(x);
        int pixelY = Mth.floor(y);
        PoseStack poseStack = event.getGuiGraphics().pose();
        poseStack.pushPose();
        poseStack.translate(x - pixelX, y - pixelY, 0.0F);
        event.getGuiGraphics().fill(pixelX - 1, pixelY - 1, pixelX + glowSize, pixelY + glowSize, glowColor);
        event.getGuiGraphics().fill(pixelX, pixelY, pixelX + size, pixelY + size, coreColor);
        poseStack.popPose();
    }

    private static int argbFromColor(Vec3 color, float alpha) {
        return ((int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24)
                | ((int) (Mth.clamp((float) color.x, 0.0F, 1.0F) * 255.0F) << 16)
                | ((int) (Mth.clamp((float) color.y, 0.0F, 1.0F) * 255.0F) << 8)
                | (int) (Mth.clamp((float) color.z, 0.0F, 1.0F) * 255.0F);
    }

    private static float hash01(int index, int salt) {
        return fract(Mth.sin(index * 12.9898F + salt * 78.233F) * 43758.547F);
    }

    private static float fract(float value) {
        return value - Mth.floor(value);
    }
}
