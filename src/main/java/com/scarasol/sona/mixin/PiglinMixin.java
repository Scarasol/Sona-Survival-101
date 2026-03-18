package com.scarasol.sona.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Piglin.class)
public abstract class PiglinMixin extends AbstractPiglin implements CrossbowAttackMob, InventoryCarrier {
    public PiglinMixin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity warpLastHurtMob(DamageSource instance, Operation<Entity> operation) {
        if (instance.getEntity() instanceof LivingEntity livingEntity && instance.isIndirect()) {
            if (livingEntity.hasEffect(SonaMobEffects.CAMOUFLAGE.get())) {
                double distance = this.position().distanceTo(livingEntity.position());
                if (livingEntity.hasEffect(SonaMobEffects.EXPOSURE.get())) {
                    double exposureRange = this.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.3 * (livingEntity.getEffect(SonaMobEffects.EXPOSURE.get()).getAmplifier() + 1);
                    if (exposureRange > distance)
                        return operation.call(instance);
                }
                double range = this.getAttributeValue(Attributes.FOLLOW_RANGE) * (1 / Math.pow(2, livingEntity.getEffect(SonaMobEffects.CAMOUFLAGE.get()).getAmplifier() + 1));
                if (distance > range) {
                    return null;
                }
            }

        }
        return operation.call(instance);
    }
}
