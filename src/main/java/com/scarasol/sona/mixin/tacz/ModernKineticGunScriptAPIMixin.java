package com.scarasol.sona.mixin.tacz;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.SoundManager;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ModernKineticGunScriptAPI.class)
public abstract class ModernKineticGunScriptAPIMixin {

    @Shadow private LivingEntity shooter;

    @Shadow private ItemStack itemStack;

    @Inject(method = "lambda$shootOnce$2", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/network/NetworkHandler;sendToTrackingEntity(Ljava/lang/Object;Lnet/minecraft/world/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onShoot(boolean consumeAmmo, GunData gunData, int bulletAmount, BulletData bulletData, IGunOperator gunOperator, float shotDamageMultiplier, float processedSpeed, float inaccuracy, int soundDistance, boolean useSilenceSound, CallbackInfoReturnable<Boolean> cir, boolean fire) {
        if (this.itemStack.hasTag() && CommonConfig.GUN_SOUND_WHITELIST.get().contains(this.itemStack.getTag().getString("GunId"))) {
            return;
        }
        if (CommonConfig.SOUND_OPEN.get() && CommonConfig.GUN_SOUND_ATTRACT.get() && soundDistance > 0){
            int range = useSilenceSound ? CommonConfig.SILENCE_EXPOSURE.get() : CommonConfig.FIRE_EXPOSURE.get();
            if (range == 0) {
                return;
            }
            if (!shooter.level().isClientSide()) {
                SoundManager.addSoundEffect(shooter, 40, range - 1);
            }
        }
    }
}
