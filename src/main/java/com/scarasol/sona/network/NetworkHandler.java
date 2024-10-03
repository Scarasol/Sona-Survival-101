package com.scarasol.sona.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.scarasol.sona.SonaMod.MODID;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage() {
        PACKET_HANDLER.registerMessage(messageID++, RotPacket.class, RotPacket::encode, RotPacket::decode, RotPacket::handler);
        PACKET_HANDLER.registerMessage(messageID++, SoundDecoyPacket.class, SoundDecoyPacket::encode, SoundDecoyPacket::decode, SoundDecoyPacket::handler);
        PACKET_HANDLER.registerMessage(messageID++, SyncSoundPacket.class, SyncSoundPacket::encode, SyncSoundPacket::decode, SyncSoundPacket::handler);
        PACKET_HANDLER.registerMessage(messageID++, LockPacket.class, LockPacket::encode, LockPacket::decode, LockPacket::handler);
    }

}
