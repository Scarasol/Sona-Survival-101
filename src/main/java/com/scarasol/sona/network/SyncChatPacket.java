package com.scarasol.sona.network;

import com.scarasol.sona.manager.ChatManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncChatPacket {
    private final String chatIncreaseItem;
    private final int chatRange;
    private final boolean chatLimit;

    public SyncChatPacket(String chatIncreaseItem, int chatRange, boolean chatLimit) {
        this.chatIncreaseItem = chatIncreaseItem;
        this.chatRange = chatRange;
        this.chatLimit = chatLimit;
    }

    public String getChatIncreaseItem() {
        return chatIncreaseItem;
    }

    public int getChatRange() {
        return chatRange;
    }

    public boolean isChatLimit() {
        return chatLimit;
    }

    public static SyncChatPacket decode(FriendlyByteBuf buf) {
        return new SyncChatPacket(new String(buf.readByteArray()), buf.readInt(), buf.readBoolean());
    }

    public static void encode(SyncChatPacket msg, FriendlyByteBuf buf) {
        buf.writeByteArray(msg.getChatIncreaseItem().getBytes());
        buf.writeInt(msg.getChatRange());
        buf.writeBoolean(msg.isChatLimit());
    }

    public static void handler(SyncChatPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                context.get().enqueueWork(() -> {
                    if (!context.get().getDirection().getReceptionSide().isServer()){
                        ChatManager.addRangeItem(msg.chatIncreaseItem);
                        ChatManager.setChatRange(msg.chatRange);
                        ChatManager.setChatLimit(msg.chatLimit);
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}
