package com.scarasol.sona.network;

import com.scarasol.sona.manager.SoundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SoundDecoyPacket {
    private final double x;
    private final double y;
    private final double z;
    private final int amplifier;

    public SoundDecoyPacket(double x, double y, double z, int amplifier) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amplifier = amplifier;
    }

    public static SoundDecoyPacket decode(FriendlyByteBuf buf) {
        return new SoundDecoyPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void encode(SoundDecoyPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeInt(msg.amplifier);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public static void handler(SoundDecoyPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                context.get().enqueueWork(() -> {
                    if (context.get().getDirection().getReceptionSide().isServer()){
                        ServerPlayer player = context.get().getSender();
                        if (player != null)
                            SoundManager.spawnSoundDecoy(player.level(), msg.getX(), msg.getY(), msg.getZ(), msg.getAmplifier());
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }


}
