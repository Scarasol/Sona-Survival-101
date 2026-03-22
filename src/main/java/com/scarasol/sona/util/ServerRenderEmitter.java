package com.scarasol.sona.util;

import com.scarasol.sona.network.NetworkHandler;
import com.scarasol.sona.network.PositionIndicatorPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class ServerRenderEmitter {

    private ServerRenderEmitter() {
    }

    public static void emitPositionIndicator(Level level, Vec3 pos, double renderRange, int duration) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        double broadcastRange = Math.max(renderRange + 16.0D, 96.0D);
        NetworkHandler.PACKET_HANDLER.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, broadcastRange, serverLevel.dimension())),
                new PositionIndicatorPacket(pos.x, pos.y, pos.z, renderRange, duration)
        );
    }
}
