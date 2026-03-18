package com.scarasol.sona.mixin.sbw;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.SoundInfo;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.atsuishio.superbwarfare.perk.Perk;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GunItem.class)
public abstract class GunItemMixin {

    @Inject(method = "playFireSounds", at = @At("TAIL"), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayFireSounds(GunData data, Entity shooter, boolean zoom, CallbackInfo ci, float pitch, Perk perk, float soundRadius, SoundInfo soundInfo, boolean isSilent, SoundEvent sound3p, SoundEvent soundFar, SoundEvent soundVeryFar) {
        if (shooter instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getMainHandItem();
            if (stack.getItem() instanceof GunItem gunItem && CommonConfig.GUN_SOUND_WHITELIST.get().contains(ForgeRegistries.ITEMS.getKey(gunItem).toString())) {
                return;
            }
            int range = isSilent ? CommonConfig.SILENCE_EXPOSURE.get() : CommonConfig.FIRE_EXPOSURE.get();
            if (range == 0) {
                return;
            }
            if (!shooter.level().isClientSide()) {
                SoundManager.addSoundEffect(livingEntity, 40, --range);
            }
        }

    }
}
