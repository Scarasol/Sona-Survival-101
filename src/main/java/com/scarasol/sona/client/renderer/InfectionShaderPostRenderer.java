package com.scarasol.sona.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.scarasol.sona.SonaMod;
import com.scarasol.sona.compat.ShaderCompatUtil;
import com.scarasol.sona.init.SonaShaders;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Shader-pack-only infection grading. This runs after the world and shader pack
 * final pass, but before GUI and screen-space spores.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InfectionShaderPostRenderer {
    private static final float POST_SMOOTHING_SPEED = 4.5F;
    private static TextureTarget infectionPostTarget;
    private static float infectionPostWeight = 0.0F;
    private static long infectionPostLastUpdateNanos = 0L;
    private static Vec3 infectionPostLastColor = Vec3.ZERO;
    private static boolean infectionPostLogged;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderShaderPost(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !ModList.get().isLoaded("oculus") || !ShaderCompatUtil.isShaderActive() || !InfectionManager.canChunkInfection(level)) {
            return;
        }

        ShaderInstance shader = SonaShaders.infectionShaderSkyPostShader;
        if (shader == null) {
            return;
        }

        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        double infectionLevel = InfectionManager.getAveZoneInfectionInRender(level, cameraPos);
        float weight = getCurrentPostWeight(infectionLevel);
        if (weight <= 0.001F) {
            return;
        }

        Vec3 color = getCurrentPostColor(level, cameraPos);
        if (color == null) {
            return;
        }

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        if (mainTarget == null || mainTarget.width <= 0 || mainTarget.height <= 0) {
            return;
        }

        ensurePostTarget(mainTarget.width, mainTarget.height);
        if (infectionPostTarget == null) {
            return;
        }

        copyMainTarget(mainTarget, infectionPostTarget);
        renderShaderPost(mainTarget, infectionPostTarget, shader, color, weight, (float) infectionLevel, level.getGameTime() + minecraft.getFrameTime());
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetShaderPostState();
    }

    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            resetShaderPostState();
        }
    }

    private static float getCurrentPostWeight(double infectionLevel) {
        float targetWeight = getVisualPostWeight(infectionLevel);
        if (targetWeight <= 0.001F) {
            infectionPostWeight = 0.0F;
            infectionPostLastUpdateNanos = System.nanoTime();
            infectionPostLastColor = Vec3.ZERO;
            return 0.0F;
        }
        return smoothPostWeight(targetWeight);
    }

    private static Vec3 getCurrentPostColor(ClientLevel level, Vec3 cameraPos) {
        Vec3 color = InfectionManager.getInfectionChunkFogColor(Vec3.ZERO, cameraPos, level);
        if (color != null) {
            infectionPostLastColor = color;
            return color;
        }
        return infectionPostLastColor == Vec3.ZERO ? null : infectionPostLastColor;
    }

    private static float getVisualPostWeight(double infectionLevel) {
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

    private static float smoothPostWeight(float targetWeight) {
        long now = System.nanoTime();
        if (infectionPostLastUpdateNanos == 0L) {
            infectionPostLastUpdateNanos = now;
            infectionPostWeight = targetWeight;
            return infectionPostWeight;
        }

        float deltaSeconds = Math.min((now - infectionPostLastUpdateNanos) / 1_000_000_000.0F, 1.0F);
        infectionPostLastUpdateNanos = now;

        float alpha = 1.0F - (float) Math.exp(-POST_SMOOTHING_SPEED * deltaSeconds);
        infectionPostWeight = Mth.lerp(alpha, infectionPostWeight, targetWeight);
        if (targetWeight <= 0.001F && infectionPostWeight <= 0.001F) {
            infectionPostWeight = 0.0F;
        }
        return infectionPostWeight;
    }

    private static void ensurePostTarget(int width, int height) {
        if (infectionPostTarget != null && infectionPostTarget.width == width && infectionPostTarget.height == height) {
            return;
        }

        releasePostTarget();
        infectionPostTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        infectionPostTarget.setFilterMode(9728);
    }

    private static void copyMainTarget(RenderTarget mainTarget, TextureTarget copyTarget) {
        copyTarget.copyDepthFrom(mainTarget);

        GlStateManager._glBindFramebuffer(36008, mainTarget.frameBufferId);
        GlStateManager._bindTexture(copyTarget.getColorTextureId());
        GlStateManager._glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, copyTarget.width, copyTarget.height);
        GlStateManager._bindTexture(0);
        GlStateManager._glBindFramebuffer(36008, 0);
        mainTarget.bindWrite(true);
    }

    private static void renderShaderPost(RenderTarget mainTarget, TextureTarget copyTarget, ShaderInstance shader, Vec3 color, float weight, float infectionLevel, float time) {
        mainTarget.bindWrite(true);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.backupProjectionMatrix();

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        modelViewStack.translate(0.0F, 0.0F, -2000.0F);
        RenderSystem.applyModelViewMatrix();

        Matrix4f projectionMatrix = new Matrix4f().setOrtho(
                0.0F,
                (float) mainTarget.viewWidth,
                (float) mainTarget.viewHeight,
                0.0F,
                1000.0F,
                3000.0F
        );
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        shader.setSampler("SceneColor", copyTarget.getColorTextureId());
        shader.setSampler("SceneDepth", copyTarget.getDepthTextureId());
        setUniform(shader.getUniform("InfectionColor"), (float) color.x, (float) color.y, (float) color.z);
        setUniform(shader.getUniform("InfectionWeight"), weight);
        setUniform(shader.getUniform("InfectionLevel"), infectionLevel);
        setUniform(shader.getUniform("Time"), time);
        setUniform(shader.MODEL_VIEW_MATRIX, RenderSystem.getModelViewMatrix());
        setUniform(shader.PROJECTION_MATRIX, RenderSystem.getProjectionMatrix());
        if (shader.COLOR_MODULATOR != null) {
            float[] shaderColor = RenderSystem.getShaderColor();
            shader.COLOR_MODULATOR.set(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
        }
        if (shader.SCREEN_SIZE != null) {
            shader.SCREEN_SIZE.set((float) mainTarget.viewWidth, (float) mainTarget.viewHeight);
        }

        shader.apply();
        if (!infectionPostLogged) {
            SonaMod.LOGGER.info("Applying infection shader post effect, weight={}", weight);
            infectionPostLogged = true;
        }
        drawFullscreenQuad(mainTarget.viewWidth, mainTarget.viewHeight);
        shader.clear();

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        mainTarget.bindWrite(true);
    }

    private static void drawFullscreenQuad(int width, int height) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(0.0D, height, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        builder.vertex(width, height, 0.0D).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        builder.vertex(width, 0.0D, 0.0D).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
        builder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
        BufferUploader.draw(builder.end());
    }

    private static void setUniform(Uniform uniform, float value) {
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setUniform(Uniform uniform, float x, float y, float z) {
        if (uniform != null) {
            uniform.set(x, y, z);
        }
    }

    private static void setUniform(Uniform uniform, Matrix4f matrix) {
        if (uniform != null) {
            uniform.set(matrix);
        }
    }

    private static void resetShaderPostState() {
        infectionPostWeight = 0.0F;
        infectionPostLastUpdateNanos = 0L;
        infectionPostLastColor = Vec3.ZERO;
        infectionPostLogged = false;
        releasePostTarget();
    }

    private static void releasePostTarget() {
        TextureTarget target = infectionPostTarget;
        infectionPostTarget = null;
        if (target == null) {
            return;
        }

        if (RenderSystem.isOnRenderThreadOrInit()) {
            target.destroyBuffers();
        } else {
            RenderSystem.recordRenderCall(target::destroyBuffers);
        }
    }
}
