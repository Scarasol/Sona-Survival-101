package com.scarasol.sona.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SonaMath {
    public static double parabolaAngleCalculate(LivingEntity entity, BlockPos target, double v) {
        return parabolaAngleCalculate(entity, Vec3.atCenterOf(target), v);
    }

    public static double parabolaAngleCalculate(LivingEntity entity, LivingEntity target, double v) {
        return parabolaAngleCalculate(entity, target.position().add(0, target.getEyeHeight(), 0), v);
    }

    public static double parabolaAngleCalculate(LivingEntity entity, Vec3 target, double v) {
        Vec3 position = entity.position().add(0, entity.getEyeHeight(), 0);
        double x = Math.pow(Math.pow(target.x() - position.x(), 2) + Math.pow(target.z() - position.z(), 2), 0.5);
        double y = target.y() - position.y();
        double g = 0.05;
        double delta = Math.pow(y * g - v * v, 2) - g * g * (x * x + y * y);
        if (delta < 0) {
            return -1;
        }
        double t1 = ((v * v - y * g) + Math.pow(delta, 0.5)) / (0.5 * g * g);
        double t2 = ((v * v - y * g) - Math.pow(delta, 0.5)) / (0.5 * g * g);
        double t = Math.max(t1, t2);
        if (t < 0) {
            return -1;
        }
        t = Math.pow(t, 0.5);
        double cos = x / (v * t);
        double angle = Math.acos(cos);
        return angle < Math.PI / 2 ? angle : -1;
    }

    public static double parabolaXDistanceCalculate(double angle, double v) {
        double vY = v * Math.sin(Math.toRadians(angle));
        double vX = v * Math.cos(Math.toRadians(angle));
        double g = 0.05;
        double t = 2 * vY / g;
        return vX * t;
    }

    public static Vec3 parabolaDropPointCalculate(double v, Entity entity) {
        Vec3 pos = entity.position();
        Vec3 lastPos;
        Vec3 currentV = entity.getLookAngle().scale(v);
        double g = 0.05;
        while (true) {
            lastPos = pos;
            pos = pos.add(currentV);
            Vec3 checkV = currentV.normalize();
            for (int i = 0; i < v; i++) {
                lastPos = lastPos.add(checkV);
                if (!entity.level().isLoaded(BlockPos.containing(lastPos)) || !entity.level().getBlockState(BlockPos.containing(lastPos)).isAir()) {
                    return lastPos;
                }
            }
            currentV = currentV.add(0, -g, 0);
        }
    }

    public static double vectorDegreeCalculate(Vec3 vec1, Vec3 vec2) {
        double cos = vec1.dot(vec2) / vec1.length() / vec2.length();
        return Math.toDegrees(Math.acos(cos));
    }


    public static Vec3 calculateVector(float XRot, float YRot) {
        float f = XRot * ((float)Math.PI / 180F);
        float f1 = -YRot * ((float)Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }

}
