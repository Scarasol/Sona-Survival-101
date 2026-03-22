package com.scarasol.sona.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class PositionIndicatorRenderer {

    private static final float ARROW_ALPHA_MIN = 0.18F;
    private static final float ARROW_ALPHA_MAX = 0.45F;
    private static final float HALO_ALPHA_MAX = 1;
    private static final int HALO_RIPPLE_COUNT = 4;
    private static final float HALO_LAYER_DELAY = 0.12F;
    private static final float INDICATOR_RING_RADIUS_FACTOR = 0.24F;
    private static final int RED = 0xFF3A3A;

    private PositionIndicatorRenderer() {
    }

    public static void renderGuiIndicators(GuiGraphics guiGraphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (player == null || camera == null || minecraft.options.hideGui) {
            return;
        }

        List<TargetInfo> targets = collectTargets(player, partialTick);
        if (targets.isEmpty()) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        float ripple = getRippleProgress(player.level().getGameTime(), partialTick);
        float alpha = getArrowAlpha(ripple);

        float centerX = width * 0.5F;
        float centerY = height * 0.5F;
        float radius = Math.min(width, height) * INDICATOR_RING_RADIUS_FACTOR;

        for (TargetInfo target : targets) {
            if (target.onScreen) {
                continue;
            }
            if (alpha <= 0.0F) {
                continue;
            }
            float angle = (float) Math.atan2(target.screenY, target.screenX);
            renderRingArrow(guiGraphics.pose(), centerX, centerY, radius, angle, 18.0F, alpha);
        }
    }

    public static void renderWorldHalos(PoseStack poseStack, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (player == null || camera == null) {
            return;
        }

        List<TargetInfo> targets = collectTargets(player, partialTick);
        if (targets.isEmpty()) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        float ripple = getRippleProgress(player.level().getGameTime(), partialTick);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        for (TargetInfo target : targets) {
            if (!target.onScreen) {
                continue;
            }

            double distance = target.worldPos.distanceTo(camPos);
            float thicknessScale = Mth.clamp(1.0F + (float) distance * 0.032F, 1.0F, 4.25F);
            poseStack.pushPose();
            poseStack.translate(target.worldPos.x, target.worldPos.y, target.worldPos.z);
            poseStack.mulPose(camera.rotation());
            for (int i = 0; i < HALO_RIPPLE_COUNT; i++) {
                float delayedProgress = (ripple - i * HALO_LAYER_DELAY) / (1.0F - (HALO_RIPPLE_COUNT - 1) * HALO_LAYER_DELAY);
                if (delayedProgress <= 0.0F) {
                    continue;
                }
                delayedProgress = Mth.clamp(delayedProgress, 0.0F, 1.0F);
                float radius = 0.18F + delayedProgress * 1.7F;
                float layerFade = (1.0F - delayedProgress) * (1.0F - delayedProgress) * (1.0F - i * 0.10F);
                int alpha = Mth.floor(layerFade * HALO_ALPHA_MAX * 255.0F);
                if (alpha <= 0) {
                    continue;
                }
                renderBillboardHalo(builder, poseStack.last().pose(), radius, 0.035F * thicknessScale, 48, RED, alpha);
                renderBillboardHalo(builder, poseStack.last().pose(), Math.max(0.12F, radius - 0.10F), 0.018F * thicknessScale, 42, RED, alpha / 3);
            }
            poseStack.popPose();
        }

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static List<TargetInfo> collectTargets(Player player, float partialTick) {
        List<TargetInfo> targets = new ArrayList<>();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera == null) {
            return targets;
        }

        Vec3 camPos = camera.getPosition();
        Vec3 forward = player.getViewVector(partialTick).normalize();
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(worldUp);
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();

        float fov = (float) Math.toRadians(Minecraft.getInstance().options.fov().get());
        float tanHalfFov = (float) Math.tan(fov * 0.5F);
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        float aspect = (float) width / (float) height;
        long gameTime = player.level().getGameTime();

        for (PositionIndicatorManager.Indicator indicator : PositionIndicatorManager.getActiveIndicators(gameTime)) {
            if (player.distanceToSqr(indicator.pos()) > indicator.renderRange() * indicator.renderRange()) {
                continue;
            }

            Vec3 worldPos = indicator.pos();
            Vec3 relative = worldPos.subtract(camPos);
            float cameraX = (float) relative.dot(right);
            float cameraY = (float) relative.dot(up);
            float cameraZ = (float) relative.dot(forward);

            boolean inFront = cameraZ > 0.05F;
            float projectedX = 0.0F;
            float projectedY = 0.0F;
            boolean onScreen = false;
            if (inFront) {
                float ndcX = cameraX / (cameraZ * tanHalfFov * aspect);
                float ndcY = cameraY / (cameraZ * tanHalfFov);
                projectedX = ndcX * width * 0.5F;
                projectedY = ndcY * height * 0.5F;
                onScreen = Math.abs(ndcX) <= 1.0F && Math.abs(ndcY) <= 1.0F;
            } else {
                Vec3 flatForward = new Vec3(forward.x, 0, forward.z).normalize();
                Vec3 flatRight = new Vec3(right.x, 0, right.z).normalize();
                Vec3 flatRelative = new Vec3(relative.x, 0, relative.z).normalize();
                projectedX = (float) flatRelative.dot(flatRight) * width * 0.5F;
                projectedY = (float) flatRelative.dot(flatForward) * height * 0.25F;
            }

            targets.add(new TargetInfo(worldPos, projectedX, projectedY, onScreen));
        }
        return targets;
    }

    private static float getRippleProgress(long gameTime, float partialTick) {
        float cycle = (gameTime + partialTick) * 0.02F;
        return cycle - Mth.floor(cycle);
    }

    private static float getArrowAlpha(float progress) {
        if (progress >= 0.76F) {
            return 0.0F;
        }
        float breath = Mth.sin(progress / 0.76F * Mth.PI);
        return Mth.lerp(Mth.clamp(breath, 0.0F, 1.0F), ARROW_ALPHA_MIN, ARROW_ALPHA_MAX);
    }

    private static void renderRingArrow(PoseStack poseStack, float centerX, float centerY, float radius, float angle, float arrowLength, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 400.0F);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        float dirX = Mth.cos(angle);
        float dirY = -Mth.sin(angle);
        float tangentX = Mth.sin(angle);
        float tangentY = Mth.cos(angle);

        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        renderCurvedArrow(builder, matrix, centerX, centerY, radius, arrowLength, dirX, dirY, tangentX, tangentY, alpha);
        BufferUploader.drawWithShader(builder.end());

        poseStack.popPose();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderCurvedArrow(BufferBuilder builder, Matrix4f matrix, float centerX, float centerY, float radius, float arrowLength, float dirX, float dirY, float tangentX, float tangentY, float alpha) {
        int curveSegments = 14;
        float halfHeight = 20.0F;
        float tipLocalX = arrowLength - 2.8F;
        float backLocalX = 0.0F;
        float centerLocalX = tipLocalX * 0.26F;

        List<float[]> points = new ArrayList<>();

        for (int i = 0; i <= curveSegments; i++) {
            float t = (float) i / curveSegments;
            float x = quadratic(backLocalX, tipLocalX * 0.52F, tipLocalX, t);
            float y = quadratic(-halfHeight, -halfHeight * 0.26F, 0.0F, t);
            points.add(toScreenPoint(centerX, centerY, radius, x, y, dirX, dirY, tangentX, tangentY));
        }

        for (int i = 1; i <= curveSegments; i++) {
            float t = (float) i / curveSegments;
            float x = quadratic(tipLocalX, tipLocalX * 0.52F, backLocalX, t);
            float y = quadratic(0.0F, halfHeight * 0.26F, halfHeight, t);
            points.add(toScreenPoint(centerX, centerY, radius, x, y, dirX, dirY, tangentX, tangentY));
        }

        for (int i = 1; i < curveSegments; i++) {
            float t = (float) i / curveSegments;
            float x = quadratic(backLocalX, tipLocalX * 0.22F, backLocalX, t);
            float y = Mth.lerp(t, halfHeight, -halfHeight);
            points.add(toScreenPoint(centerX, centerY, radius, x, y, dirX, dirY, tangentX, tangentY));
        }

        float[] center = toScreenPoint(centerX, centerY, radius, centerLocalX, 0.0F, dirX, dirY, tangentX, tangentY);
        for (int i = 0; i < points.size(); i++) {
            float[] p1 = points.get(i);
            float[] p2 = points.get((i + 1) % points.size());
            putTriangle(builder, matrix, center[0], center[1], p1[0], p1[1], p2[0], p2[1], alpha);
        }
    }

    private static float quadratic(float p0, float p1, float p2, float t) {
        float inv = 1.0F - t;
        return inv * inv * p0 + 2.0F * inv * t * p1 + t * t * p2;
    }

    private static float[] toScreenPoint(float centerX, float centerY, float radius, float localX, float localY, float dirX, float dirY, float tangentX, float tangentY) {
        float x = centerX + dirX * (radius + localX) + tangentX * localY;
        float y = centerY + dirY * (radius + localX) + tangentY * localY;
        return new float[] {x, y};
    }

    private static void putTriangle(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int r = (RED >> 16) & 255;
        int g = (RED >> 8) & 255;
        int b = RED & 255;
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x3, y3, 0).color(r, g, b, a).endVertex();
    }

    private static void renderBillboardHalo(BufferBuilder builder, Matrix4f matrix, float radius, float thickness, int segments, int color, int alpha) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        float outerRadius = radius + thickness * 0.5F;
        float innerRadius = Math.max(0.0F, radius - thickness * 0.5F);

        for (int i = 0; i < segments; i++) {
            float a0 = (float) (Math.PI * 2.0D * i / segments);
            float a1 = (float) (Math.PI * 2.0D * (i + 1) / segments);
            float outerX0 = Mth.cos(a0) * outerRadius;
            float outerY0 = Mth.sin(a0) * outerRadius;
            float outerX1 = Mth.cos(a1) * outerRadius;
            float outerY1 = Mth.sin(a1) * outerRadius;
            float innerX0 = Mth.cos(a0) * innerRadius;
            float innerY0 = Mth.sin(a0) * innerRadius;
            float innerX1 = Mth.cos(a1) * innerRadius;
            float innerY1 = Mth.sin(a1) * innerRadius;

            builder.vertex(matrix, outerX0, outerY0, 0.0F).color(r, g, b, alpha).endVertex();
            builder.vertex(matrix, outerX1, outerY1, 0.0F).color(r, g, b, alpha).endVertex();
            builder.vertex(matrix, innerX1, innerY1, 0.0F).color(r, g, b, alpha).endVertex();

            builder.vertex(matrix, outerX0, outerY0, 0.0F).color(r, g, b, alpha).endVertex();
            builder.vertex(matrix, innerX1, innerY1, 0.0F).color(r, g, b, alpha).endVertex();
            builder.vertex(matrix, innerX0, innerY0, 0.0F).color(r, g, b, alpha).endVertex();
        }
    }

    private record TargetInfo(Vec3 worldPos, float screenX, float screenY, boolean onScreen) {
    }
}
