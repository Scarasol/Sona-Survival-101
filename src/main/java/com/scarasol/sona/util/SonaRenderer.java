package com.scarasol.sona.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.scarasol.sona.client.renderer.SonaRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Scarasol
 */
public class SonaRenderer {

    public static void renderLineBeam(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Vec3 start,
            Vec3 end,
            float thickness,           // 线条粗细
            int segments,              // 分段数量
            int baseColor,
            int tipColor,
            float animationOffset,     // 时间偏移（随tick变化）
            float wiggleIntensity      // 扰动强度（相对 totalDist）
    ) {




        Vec3 delta = end.subtract(start);
        float deltaX = (float) delta.x;
        float deltaY = (float) delta.y;
        float deltaZ = (float) delta.z;
        float totalDist = Mth.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (totalDist <= 1e-6f) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate((float) start.x, (float) start.y, (float) start.z);

        // 朝向目标
        float horizontalDist = Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            poseStack.mulPose(Axis.YP.rotation((float)(-Math.atan2(deltaZ, deltaX)) - ((float)Math.PI / 2F)));
            poseStack.mulPose(Axis.XP.rotation((float)(-Math.atan2(horizontalDist, deltaY)) - ((float)Math.PI / 2F)));

        VertexConsumer builder = bufferSource.getBuffer(SonaRenderType.translucentLines(thickness));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        int steps = Math.max(2, segments * 8);
        steps = Math.min(steps, 512);

        float maxWiggle = totalDist * wiggleIntensity;
        Random random = new Random((long) (animationOffset * 12345.6789));

        float prevX = 0, prevY = 0, prevZ = 0;
        for (int i = 1; i <= steps; i++) {
            float t = (float) i / (float) steps;
            float z = t * totalDist;

            float x, y;
            if (Math.abs(wiggleIntensity) > 0) {

                x = (random.nextFloat() - 0.5f) * 2f * maxWiggle;
                y = (random.nextFloat() - 0.5f) * 2f * maxWiggle;
            } else {
                x = 0;
                y = 0;
            }

            // 插值颜色
            int r1 = (int) Mth.lerp(t - 1f / steps, (baseColor >> 16) & 0xFF, (tipColor >> 16) & 0xFF);
            int g1 = (int) Mth.lerp(t - 1f / steps, (baseColor >> 8) & 0xFF, (tipColor >> 8) & 0xFF);
            int b1 = (int) Mth.lerp(t - 1f / steps, baseColor & 0xFF, tipColor & 0xFF);

            int r2 = (int) Mth.lerp(t, (baseColor >> 16) & 0xFF, (tipColor >> 16) & 0xFF);
            int g2 = (int) Mth.lerp(t, (baseColor >> 8) & 0xFF, (tipColor >> 8) & 0xFF);
            int b2 = (int) Mth.lerp(t, baseColor & 0xFF, tipColor & 0xFF);

            builder.vertex(matrix, prevX, prevY, prevZ)
                    .color(r1, g1, b1, 255)
                    .normal(0f, 1f, 0f)
                    .endVertex();

            builder.vertex(matrix, x, y, z)
                    .color(r2, g2, b2, 255)
                    .normal(0f, 1f, 0f)
                    .endVertex();

            prevX = x;
            prevY = y;
            prevZ = z;
        }

        poseStack.popPose();

    }



    public static void renderParabolaLightningBeam(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float thickness,
            int segments,
            int baseColor,
            int tipColor,
            float animationOffset,
            double v,
            float xRot,
            float yRot,
            double g,
            Vec3 startPos,
            Vec3 originPos,
            Level level
            ) {

        Vec3 lastPos;

        Vec3 currentV = SonaMath.calculateVector(xRot, yRot).scale(v);

        for (int time = 0; ; time++) {
            lastPos = startPos;

            Vec3 checkV = currentV.normalize();
            for (int i = 0; i < v; i++) {
                lastPos = lastPos.add(checkV);
                BlockPos hitPos = BlockPos.containing(lastPos);
                if (!level.isLoaded(hitPos) || !level.getBlockState(hitPos).isAir()) {
                    Vec3 startRay = lastPos.subtract(checkV);
                    BlockHitResult hit = level.clip(new ClipContext(startRay, lastPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                    Direction direction = hit.getDirection();
                    poseStack.pushPose();

                    renderHalo(poseStack, bufferSource, hitPos, originPos, direction, 5, baseColor, thickness);
                    renderHalo(poseStack, bufferSource, hitPos, originPos, direction, 2, baseColor, thickness);
                    renderRotatingCross(poseStack, bufferSource, hitPos, originPos, direction, 5f, 1, baseColor, thickness);
                    poseStack.popPose();
                    return;
                }
            }


            if (time % 5 == 0) {

                Vec3 forwardDir = currentV.normalize();
                Vec3 scrollOffset = forwardDir.scale(animationOffset);

                renderLineBeam(poseStack, bufferSource, startPos.subtract(originPos).add(scrollOffset), startPos.subtract(originPos).add(checkV.scale(5)).add(scrollOffset), 4, segments, baseColor, tipColor, animationOffset, 0f);
            }
            startPos = startPos.add(currentV);
            currentV = currentV.add(0, -g, 0);
        }
    }

    public static void renderHalo(PoseStack poseStack, MultiBufferSource bufferSource, BlockPos pos, Vec3 cam, Direction dir, float radius, int color, float thickness) {
        Vec3 center = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
        Vec3 normal = Vec3.atLowerCornerOf(dir.getNormal());

        Vec3 tangent, bitangent;
        if (dir.getAxis() == Direction.Axis.Y) {
            tangent = new Vec3(1, 0, 0);
            bitangent = new Vec3(0, 0, 1);
        } else {
            tangent = new Vec3(0, 1, 0);
            bitangent = normal.cross(tangent).normalize();
        }

        List<Vec3> points = new ArrayList<>();
        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2 / segments;
            Vec3 base = center.add(tangent.scale(Math.cos(angle) * radius))
                    .add(bitangent.scale(Math.sin(angle) * radius));
            points.add(base);
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        VertexConsumer builder = bufferSource.getBuffer(SonaRenderType.translucentLines(thickness));

        for (int i = 0; i < points.size(); i++) {
            Vec3 a = points.get(i);
            Vec3 b = points.get((i + 1) % points.size());
            builder.vertex(matrix, (float)(a.x - cam.x), (float)(a.y - cam.y), (float)(a.z - cam.z))
                    .color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255)
                    .normal(0, 1, 0)
                    .endVertex();
            builder.vertex(matrix, (float)(b.x - cam.x), (float)(b.y - cam.y), (float)(b.z - cam.z))
                    .color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255)
                    .normal(0, 1, 0)
                    .endVertex();
        }
    }

    public static void renderRotatingCross(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            BlockPos pos,
            Vec3 cam,
            Direction dir,
            float armLength,        // 每条臂的长度
            float innerGap,         // 镂空（离圆心的距离）
            int color,
            float thickness
    ) {
        Vec3 center = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
        Vec3 normal = Vec3.atLowerCornerOf(dir.getNormal());

        // 坐标基（与 renderHalo 一致）
        Vec3 tangent, bitangent;
        if (dir.getAxis() == Direction.Axis.Y) {
            tangent = new Vec3(1, 0, 0);
            bitangent = new Vec3(0, 0, 1);
        } else {
            tangent = new Vec3(0, 1, 0);
            bitangent = normal.cross(tangent).normalize();
        }

        // 每帧旋转 10°
        long animationOffset = Minecraft.getInstance().level.getGameTime() % 36;
        double angle = Math.toRadians(animationOffset * 10.0);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        // 旋转后的两个基向量
        Vec3 xAxis = tangent.scale(cos).add(bitangent.scale(sin));
        Vec3 yAxis = bitangent.scale(cos).subtract(tangent.scale(sin));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer builder = bufferSource.getBuffer(SonaRenderType.translucentLines(thickness));

        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        // 四条臂：上下左右（中间空一圈）
        Vec3[] starts = new Vec3[] {
                center.add(xAxis.scale(innerGap)),    // 右
                center.add(yAxis.scale(innerGap)),    // 上
                center.add(xAxis.scale(-innerGap)),   // 左
                center.add(yAxis.scale(-innerGap))    // 下
        };

        Vec3[] ends = new Vec3[] {
                center.add(xAxis.scale(innerGap + armLength)),    // 右臂末端
                center.add(yAxis.scale(innerGap + armLength)),    // 上臂末端
                center.add(xAxis.scale(-innerGap - armLength)),   // 左臂末端
                center.add(yAxis.scale(-innerGap - armLength))    // 下臂末端
        };

        // 绘制四条线臂
        for (int i = 0; i < 4; i++) {
            Vec3 a = starts[i];
            Vec3 vec32 = ends[i];
            builder.vertex(matrix, (float)(a.x - cam.x), (float)(a.y - cam.y), (float)(a.z - cam.z))
                    .color(r, g, b, 255).normal(0, 1, 0).endVertex();
            builder.vertex(matrix, (float)(vec32.x - cam.x), (float)(vec32.y - cam.y), (float)(vec32.z - cam.z))
                    .color(r, g, b, 255).normal(0, 1, 0).endVertex();
        }
    }



}
