package com.scarasol.sona.mixin;

import com.mojang.authlib.GameProfile;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.init.SonaSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Shadow @Final public ClientPacketListener connection;

    @Shadow public Input input;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sona$tick(CallbackInfo ci) {
        long gameTime = level().getGameTime() + getId();
        if (hasEffect(SonaMobEffects.LACERATION.get())) {
            if (gameTime % 300 == 0) {
                level().playLocalSound(blockPosition(), SonaSounds.LACERATION_LOOP.get(), SoundSource.PLAYERS, 5, 1, false);
            }
            this.connection.send(new ServerboundPlayerInputPacket(this.xxa, this.zza, this.input.jumping, this.input.shiftKeyDown));
        }
    }
}
