package com.scarasol.sona.mixin;

import com.scarasol.sona.manager.ChatManager;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.FutureChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ServerPlayerConnection, TickablePacketListener, ServerGamePacketListener {


    @Shadow protected abstract PlayerChatMessage getSignedMessage(ServerboundChatPacket p_251061_, LastSeenMessages p_250566_) throws SignedMessageChain.DecodeException;


    @Shadow protected abstract Optional<LastSeenMessages> tryHandleChat(String p_251364_, Instant p_248959_, LastSeenMessages.Update p_249613_);

    @Shadow @Final private MinecraftServer server;

    @Shadow protected abstract void handleMessageDecodeFailure(SignedMessageChain.DecodeException p_252068_);

    @Shadow protected abstract CompletableFuture<FilteredText> filterTextPacket(String p_243213_);

    @Shadow @Final private FutureChain chatMessageChain;

    @Shadow public ServerPlayer player;

    @ModifyVariable(method = "handleChat", at = @At("STORE"), ordinal = 0)
    private Optional<LastSeenMessages> emptyOptional(Optional<LastSeenMessages> optional) {
        return Optional.empty();
    }

    @Inject(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryHandleChat(Ljava/lang/String;Ljava/time/Instant;Lnet/minecraft/network/chat/LastSeenMessages$Update;)Ljava/util/Optional;", shift = At.Shift.AFTER))
    private void onHandleChat(ServerboundChatPacket p_9841_, CallbackInfo ci) {
        Optional<LastSeenMessages> optional = this.tryHandleChat(p_9841_.message(), p_9841_.timeStamp(), p_9841_.lastSeenMessages());
        optional.ifPresent(lastSeenMessages -> this.server.submit(() -> {
            PlayerChatMessage playerchatmessage;
            try {
                playerchatmessage = this.getSignedMessage(p_9841_, lastSeenMessages);
            } catch (SignedMessageChain.DecodeException signedmessagechain$decodeexception) {
                this.handleMessageDecodeFailure(signedmessagechain$decodeexception);
                return;
            }
            CompletableFuture<FilteredText> completablefuture = this.filterTextPacket(playerchatmessage.signedContent());
            CompletableFuture<Component> completablefuture1 = net.minecraftforge.common.ForgeHooks.getServerChatSubmittedDecorator().decorate(this.player, playerchatmessage.decoratedContent());
            this.chatMessageChain.append((p_248212_) -> CompletableFuture.allOf(completablefuture, completablefuture1).thenAcceptAsync((p_248218_) -> {
                Component decoratedContent = completablefuture1.join();
                if (decoratedContent == null)
                    return; // Forge: ServerChatEvent was canceled if this is null.
                PlayerChatMessage playerchatmessage1 = playerchatmessage.withUnsignedContent(decoratedContent).filter(completablefuture.join().mask());
//                Player player = server.getPlayerList().getPlayer(playerchatmessage1.sender());
                ChatManager.broadcastMessage(this.player, playerchatmessage1);
            }, p_248212_));
        }));
    }

}
