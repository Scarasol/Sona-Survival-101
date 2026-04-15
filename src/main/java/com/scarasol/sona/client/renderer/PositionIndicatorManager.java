package com.scarasol.sona.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PositionIndicatorManager {

    private static final List<Indicator> INDICATORS = new ArrayList<>();
    private static final double MIN_RENDER_DISTANCE_SQR = 64.0D;

    private PositionIndicatorManager() {
    }

    public static void addIndicator(double x, double y, double z, double renderRange, int duration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        Vec3 pos = new Vec3(x, y, z);
        INDICATORS.removeIf(indicator -> indicator.pos.closerThan(pos, 0.25D));
        if (minecraft.player.distanceToSqr(pos) < MIN_RENDER_DISTANCE_SQR) {
            return;
        }

        long expireAt = minecraft.level.getGameTime() + duration;
        INDICATORS.add(new Indicator(pos, renderRange, expireAt));
    }

    public static List<Indicator> getActiveIndicators(long gameTime) {
        Iterator<Indicator> iterator = INDICATORS.iterator();
        while (iterator.hasNext()) {
            Indicator indicator = iterator.next();
            if (indicator.expireAt <= gameTime) {
                iterator.remove();
            }
        }
        return List.copyOf(INDICATORS);
    }

    public static void clear() {
        INDICATORS.clear();
    }

    public record Indicator(Vec3 pos, double renderRange, long expireAt) {
    }
}
