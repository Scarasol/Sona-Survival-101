package com.scarasol.sona.mixin;

import com.mojang.authlib.GameProfile;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.init.SonaDamageTypes;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow public abstract boolean hurt(DamageSource p_9037_, float p_9038_);

    @Shadow public abstract boolean isCreative();

    @Unique
    private float sona$forward = 0;

    @Unique
    private float sona$strafe = 0;

    @Unique
    private boolean sona$jump = false;



    public ServerPlayerMixin(Level level, BlockPos pos, float yaw, GameProfile profile) {
        super(level, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sona$Travel(CallbackInfo ci) {
        long gameTime = level().getGameTime() + getId();
        if (gameTime % 20 == 0) {
            if (hasEffect(SonaMobEffects.LACERATION.get())) {
                float maxMove = Math.max(sona$forward, sona$strafe);
                if (maxMove > 0 || sona$jump) {
                    float damage = getEffect(SonaMobEffects.LACERATION.get()).getAmplifier() + 1;
                    if (!isSprinting()) {
                        damage /= 2f;
                    }
                    invulnerableTime = 0;
                    hurt(SonaDamageTypes.damageSource(level().registryAccess(), SonaDamageTypes.LACERATION), damage);

                }
            }
            sona$forward = 0;
            sona$strafe = 0;
            sona$jump = false;
        }

    }

    @Inject(method = "setPlayerInput", at = @At("HEAD"))
    private void sona$setPlayerInput(float forward, float strafe, boolean jump, boolean crouch, CallbackInfo ci) {
        if (!isPassenger() && this instanceof ILivingEntityAccessor livingEntityAccessor && hasEffect(SonaMobEffects.LACERATION.get())) {
            MobEffectInstance instance = getEffect(SonaMobEffects.LACERATION.get());
            if (!crouch && instance.getDuration() > 0) {
                if (forward != 0 || strafe != 0 || jump) {
                    int amount = instance.getAmplifier() + 1;
                    if (isSprinting()) {
                        amount *= 2;
                    }
                    livingEntityAccessor.setSona$laceration(amount);
                }
            }
        }
    }
}
