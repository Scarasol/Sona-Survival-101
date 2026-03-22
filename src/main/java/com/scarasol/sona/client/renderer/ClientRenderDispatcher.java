package com.scarasol.sona.client.renderer;

import com.scarasol.sona.network.PositionIndicatorPacket;

public final class ClientRenderDispatcher {

    private ClientRenderDispatcher() {
    }

    public static void handlePositionIndicator(PositionIndicatorPacket packet) {
        PositionIndicatorManager.addIndicator(packet.x(), packet.y(), packet.z(), packet.renderRange(), packet.duration());
    }
}
