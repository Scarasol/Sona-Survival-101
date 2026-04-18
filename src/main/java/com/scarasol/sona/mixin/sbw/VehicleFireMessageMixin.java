package com.scarasol.sona.mixin.sbw;


import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.network.message.send.VehicleFireMessage;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.SoundManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Scarasol
 */
@Mixin(VehicleFireMessage.class)
public abstract class VehicleFireMessageMixin {

    @Inject(method = "lambda$handler$0", at = @At(value = "INVOKE", target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;vehicleShoot(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/UUID;Lnet/minecraft/world/phys/Vec3;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void onHandler(NetworkEvent.Context context, VehicleFireMessage message, CallbackInfo ci, ServerPlayer player, VehicleEntity vehicle, Entity patt1891$temp) {
        if (CommonConfig.GUN_SOUND_ATTRACT.get()) {
            SoundManager.addSoundEffect(player, 40, CommonConfig.FIRE_EXPOSURE.get() - 1);
        }
    }
}
