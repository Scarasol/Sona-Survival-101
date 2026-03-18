package com.scarasol.sona.effect;

import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class Infection extends PhysicalEffect {

    public Infection() {
        super(MobEffectCategory.HARMFUL, -356631);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.hasEffect(SonaMobEffects.IMMUNITY.get())) {
            entity.removeEffect(SonaMobEffects.INFECTION.get());
            return;
        }
        if ((entity.level().getGameTime() + entity.getId()) % 20 == 0) {
            if (entity instanceof Mob || (entity instanceof Player player && !(player.isSpectator() || player.isCreative()))) {
                if (InfectionManager.canBeInfected(entity) && entity instanceof ILivingEntityAccessor livingEntityAccessor) {
                    InfectionManager.addInfection(livingEntityAccessor, 0.5F * (amplifier + 1));
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
