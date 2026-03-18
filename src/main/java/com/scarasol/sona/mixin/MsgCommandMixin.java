package com.scarasol.sona.mixin;

import com.scarasol.sona.manager.ChatManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(MsgCommand.class)
public abstract class MsgCommandMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private static void onSendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, PlayerChatMessage playerChatMessage, CallbackInfo ci) {
        if (!ChatManager.isChatLimit())
            return;
        ServerPlayer player = commandSourceStack.getPlayer();
        if (player != null) {
            int range = ChatManager.getMaxRange(player);
            if (range > 0){
                collection.removeIf(target -> player.distanceTo(target) > range * 1.5);
            }else if (range == -1){
                collection.removeIf(target -> !player.level().equals(target.level()));
            }
            if (!collection.isEmpty()) {
                Collection<ServerPlayer> copy = new ArrayList<>(collection);
                ChatManager.sendMessage(commandSourceStack, copy, playerChatMessage, range);
                collection.clear();
            }
        }
    }


}
