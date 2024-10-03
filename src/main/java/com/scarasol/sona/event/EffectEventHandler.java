package com.scarasol.sona.event;

import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EffectEventHandler {

    @SubscribeEvent
    public static void entityTargeting(LivingEvent.LivingVisibilityEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Entity lookingEntity = event.getLookingEntity();
        if (livingEntity == null || lookingEntity == null)
            return;
        if (lookingEntity instanceof Mob entity_buffer){
            LivingEntity originalTarget = entity_buffer.getTarget();
            BlockPos targetPos = new BlockPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            BlockPos mobPos = new BlockPos(entity_buffer.getX(), entity_buffer.getY(), entity_buffer.getZ());
            if (livingEntity.hasEffect(SonaMobEffects.CAMOUFLAGE.get()) && !livingEntity.equals(originalTarget)) {
                double distance = targetPos.distSqr(mobPos);
                double range = Math.pow(entity_buffer.getAttributeValue(Attributes.FOLLOW_RANGE) * (1 / Math.pow(2, livingEntity.getEffect(SonaMobEffects.CAMOUFLAGE.get()).getAmplifier() + 1)), 2);
                if (distance > range) {
                    event.modifyVisibility(0);
                }
            }else if(entity_buffer instanceof Enemy && livingEntity.hasEffect(SonaMobEffects.EXPOSURE.get()) && (originalTarget == null || !originalTarget.isAlive())){
                double distance = targetPos.distSqr(mobPos);
                double range = Math.pow(entity_buffer.getAttributeValue(Attributes.FOLLOW_RANGE) * (livingEntity.getEffect(SonaMobEffects.EXPOSURE.get()).getAmplifier() + 1) * 0.3, 2);
                if (distance < range) {
                    if (livingEntity instanceof Player _plr && _plr.getAbilities().instabuild){
                        return;
                    }
                    entity_buffer.setTarget(livingEntity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void constraintPlayer(PlayerInteractEvent event) {
        if (event != null && event.getEntity() != null) {
            Player entity = event.getEntity();
            if ((entity.hasEffect(SonaMobEffects.STUN.get()) || (entity.hasEffect(SonaMobEffects.SLIMINESS.get()) && entity.hasEffect(SonaMobEffects.FROST.get()))) && event.isCancelable()) {
                event.setCanceled(true);
            }
        }
    }
}
