package com.scarasol.sona.entity.ai.goal;

import com.scarasol.sona.entity.SoundDecoy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class SoundAttractionGoal extends Goal {

    private static final int SCAN_INTERVAL = 10;
    private static final int REPATH_INTERVAL = 10;
    private static final double SEARCH_RANGE_MULTIPLIER = 4.0D;

    private final Mob mob;
    private final double speedModifier;
    private final double closeEnoughDistSqr;

    @Nullable
    private SoundDecoy targetDecoy;

    private int nextScanTick;
    private int nextRepathTick;

    public SoundAttractionGoal(Mob mob, double speedModifier, double closeEnoughDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.closeEnoughDistSqr = closeEnoughDistance * closeEnoughDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        clearSoundDecoyTarget();
        if (hasRealTarget()) {
            return false;
        }
        if (nextScanTick > 0) {
            nextScanTick--;
            return false;
        }
        nextScanTick = reducedTickDelay(SCAN_INTERVAL);
        targetDecoy = findNearestDecoy();
        return targetDecoy != null && !isCloseEnough(targetDecoy);
    }

    @Override
    public boolean canContinueToUse() {
        clearSoundDecoyTarget();
        return targetDecoy != null
                && targetDecoy.isAlive()
                && !hasRealTarget()
                && isInAttractionRange(targetDecoy)
                && !isCloseEnough(targetDecoy);
    }

    @Override
    public void start() {
        nextRepathTick = 0;
        moveToTargetDecoy();
    }

    @Override
    public void stop() {
        targetDecoy = null;
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetDecoy == null) {
            return;
        }
        mob.getLookControl().setLookAt(targetDecoy, 30.0F, 30.0F);
        if (nextRepathTick > 0) {
            nextRepathTick--;
            return;
        }
        nextRepathTick = reducedTickDelay(REPATH_INTERVAL);
        moveToTargetDecoy();
    }

    private void moveToTargetDecoy() {
        if (targetDecoy != null) {
            mob.getNavigation().moveTo(targetDecoy.getX(), targetDecoy.getY(), targetDecoy.getZ(), speedModifier);
        }
    }

    @Nullable
    private SoundDecoy findNearestDecoy() {
        double followRange = Math.max(mob.getAttributeValue(Attributes.FOLLOW_RANGE), 1.0D);
        double searchRange = Math.max(16.0D, followRange * SEARCH_RANGE_MULTIPLIER);
        List<SoundDecoy> decoys = mob.level().getEntitiesOfClass(
                SoundDecoy.class,
                mob.getBoundingBox().inflate(searchRange),
                this::isInAttractionRange
        );
        return decoys.stream()
                .min(Comparator.comparingDouble(mob::distanceToSqr))
                .orElse(null);
    }

    private boolean isInAttractionRange(SoundDecoy soundDecoy) {
        double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.3D * (soundDecoy.getAmplifier() + 1);
        return range > 0 && mob.distanceToSqr(soundDecoy) <= range * range;
    }

    private boolean isCloseEnough(SoundDecoy soundDecoy) {
        return mob.distanceToSqr(soundDecoy) <= closeEnoughDistSqr;
    }

    private boolean hasRealTarget() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && !(target instanceof SoundDecoy);
    }

    private void clearSoundDecoyTarget() {
        if (mob.getTarget() instanceof SoundDecoy) {
            mob.setTarget(null);
        }
    }
}
