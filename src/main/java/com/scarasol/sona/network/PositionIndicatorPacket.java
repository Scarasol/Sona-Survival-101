package com.scarasol.sona.network;

import com.scarasol.sona.client.renderer.PositionIndicatorManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PositionIndicatorPacket {
    private final double x;
    private final double y;
    private final double z;
    private final double renderRange;
    private final int duration;

    public PositionIndicatorPacket(double x, double y, double z, double renderRange, int duration) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.renderRange = renderRange;
        this.duration = duration;
    }

    public static PositionIndicatorPacket decode(FriendlyByteBuf buf) {
        return new PositionIndicatorPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void encode(PositionIndicatorPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.renderRange);
        buf.writeInt(msg.duration);
    }

    public static void handler(PositionIndicatorPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (!context.get().getDirection().getReceptionSide().isServer()) {
                PositionIndicatorManager.addIndicator(msg.x, msg.y, msg.z, msg.renderRange, msg.duration);
            }
        });
        context.get().setPacketHandled(true);
    }
}
