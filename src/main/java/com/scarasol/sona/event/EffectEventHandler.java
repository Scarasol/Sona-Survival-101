package com.scarasol.sona.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.scarasol.sona.SonaMod;
import com.scarasol.sona.client.renderer.PositionIndicatorRenderer;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.entity.SoundDecoy;
import com.scarasol.sona.event.SonaEventHooks;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EffectEventHandler {

    public static float tick = 0;

    @SubscribeEvent
    public static void entityTargeting(LivingEvent.LivingVisibilityEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Entity lookingEntity = event.getLookingEntity();
        if (livingEntity == null || lookingEntity == null) {
            return;
        }
        if (lookingEntity instanceof Mob entity_buffer) {
            LivingEntity originalTarget = entity_buffer.getTarget();
            BlockPos targetPos = BlockPos.containing(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            BlockPos mobPos = BlockPos.containing(entity_buffer.getX(), entity_buffer.getY(), entity_buffer.getZ());
            if (SonaEventHooks.shouldCheckNeutrality(entity_buffer, livingEntity)) {
                return;
            }
            if (entity_buffer instanceof Enemy && !(entity_buffer instanceof NeutralMob) && !(livingEntity instanceof SoundDecoy) && livingEntity.hasEffect(SonaMobEffects.EXPOSURE.get()) && (originalTarget == null || !originalTarget.isAlive())) {
                double distance = targetPos.distSqr(mobPos);
                double range = Math.pow(entity_buffer.getAttributeValue(Attributes.FOLLOW_RANGE) * (livingEntity.getEffect(SonaMobEffects.EXPOSURE.get()).getAmplifier() + 1) * 0.3, 2);
                if (distance < range && !livingEntity.isAlliedTo(entity_buffer)) {
                    if (!(livingEntity instanceof Player player) || !player.getAbilities().instabuild) {
                        entity_buffer.setTarget(livingEntity);
                        return;
                    }
                }
            }
            if (livingEntity.hasEffect(SonaMobEffects.CAMOUFLAGE.get()) && !livingEntity.equals(originalTarget)) {
                double distance = targetPos.distSqr(mobPos);
                double range = Math.pow(entity_buffer.getAttributeValue(Attributes.FOLLOW_RANGE) * (1 / Math.pow(2, livingEntity.getEffect(SonaMobEffects.CAMOUFLAGE.get()).getAmplifier() + 1)), 2);
                if (distance > range) {
                    event.modifyVisibility(0);
                }
            }
        }
    }

    @SubscribeEvent
    public void summonAid(ZombieEvent.SummonAidEvent event) {
        LivingEntity attacker = event.getAttacker();
        Zombie zombie = event.getEntity();
        if (zombie.getLastDamageSource() != null && zombie.getLastDamageSource().isIndirect()) {
            if (attacker.hasEffect(SonaMobEffects.CAMOUFLAGE.get())) {
                double distance = zombie.position().distanceTo(attacker.position());
                if (attacker.hasEffect(SonaMobEffects.EXPOSURE.get())) {
                    double exposureRange = zombie.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.3 * (attacker.getEffect(SonaMobEffects.EXPOSURE.get()).getAmplifier() + 1);
                    if (exposureRange > distance) {
                        event.setResult(Event.Result.ALLOW);
                    }
                }
                double range = zombie.getAttributeValue(Attributes.FOLLOW_RANGE) * (1 / Math.pow(2, attacker.getEffect(SonaMobEffects.CAMOUFLAGE.get()).getAmplifier() + 1));
                if (distance > range) {
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void constraintPlayer(InputEvent.MouseButton.Pre event) {
        Player entity = Minecraft.getInstance().player;
        if (entity != null && Minecraft.getInstance().screen == null) {
            if (event.getAction() == 1 && (entity.hasEffect(SonaMobEffects.STUN.get()) || (entity.hasEffect(SonaMobEffects.SLIMINESS.get()) && entity.hasEffect(SonaMobEffects.FROST.get()))) && event.isCancelable()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onAttacked(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.hasEffect(SonaMobEffects.STUN.get())) {
            event.setAmount((float) (event.getAmount() * CommonConfig.STUN_DAMAGE_MULTIPLIER.get()));
        }
    }

//    @OnlyIn(Dist.CLIENT)
//    @SubscribeEvent
//    public static void onRenderInfectionOverlayPost(RenderLevelStageEvent event) {
//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
//            Minecraft minecraft = Minecraft.getInstance();
//            Player player = minecraft.player;
//            Vec3 camPos = minecraft.gameRenderer.getMainCamera().getPosition();
//
//            SonaRenderer.renderParabolaLightningBeam(event.getPoseStack(), minecraft.renderBuffers().bufferSource(), 2, 2, 0xFF0000, 0xFF0000, player.level().getGameTime() % 23, 3.5, player.getXRot(), player.getYHeadRot(), 0.05, player.getEyePosition(), camPos, player.level());
//        }
//    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderInfectionOverlayPost(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) {
            PositionIndicatorRenderer.renderGuiIndicators(event.getGuiGraphics(), minecraft.getFrameTime());
            if (player.hasEffect(SonaMobEffects.INFECTION.get()) || tick > 0) {
                PoseStack poseStack = event.getGuiGraphics().pose();
                Minecraft mc = Minecraft.getInstance();
                int width = mc.getWindow().getGuiScaledWidth();
                int height = mc.getWindow().getGuiScaledHeight();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1, 1, 1, tick);
                poseStack.pushPose();
                event.getGuiGraphics().blit(
                        new ResourceLocation(SonaMod.MODID, "textures/screens/effects/infection.png"),
                        0, 0, 0, 0,
                        width, height,
                        width, height
                );
                poseStack.popPose();
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            if (!player.hasEffect(SonaMobEffects.INFECTION.get())) {
                tick = Math.max(0, tick - 0.01f);
            } else {
                tick = Math.min(tick + 0.01f, 1);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderExposureHints(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        PositionIndicatorRenderer.renderWorldHalos(event.getPoseStack(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.hasEffect(SonaMobEffects.MAIM.get())) {
            float proc = Math.max(1 - 0.15f * (livingEntity.getEffect(SonaMobEffects.MAIM.get()).getAmplifier() + 1), 0);
            event.setAmount(proc * event.getAmount());
        }
    }

}
