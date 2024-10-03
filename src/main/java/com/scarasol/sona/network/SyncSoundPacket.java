package com.scarasol.sona.network;

import com.scarasol.sona.manager.SoundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSoundPacket {
    private final String soundWhiteList;
    private final boolean soundOpen;

    public SyncSoundPacket(String soundWhiteList, boolean soundOpen) {
        this.soundWhiteList = soundWhiteList;
        this.soundOpen = soundOpen;
    }

    public boolean isSoundOpen() {
        return soundOpen;
    }

    public String getSoundWhiteList() {
        return soundWhiteList;
    }

    public static SyncSoundPacket decode(FriendlyByteBuf buf) {
        return new SyncSoundPacket(new String(buf.readByteArray()), buf.readBoolean());
    }

    public static void encode(SyncSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeByteArray(msg.getSoundWhiteList().getBytes());
        buf.writeBoolean(msg.isSoundOpen());
    }

    public static void handler(SyncSoundPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                context.get().enqueueWork(() -> {
                    if (!context.get().getDirection().getReceptionSide().isServer()){
                        SoundManager.addSoundWhiteList(msg.getSoundWhiteList());
                        SoundManager.setSoundOpen(msg.isSoundOpen());
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }

}
