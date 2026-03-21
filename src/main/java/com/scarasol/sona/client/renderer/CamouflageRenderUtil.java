package com.scarasol.sona.client.renderer;

public final class CamouflageRenderUtil {

    private CamouflageRenderUtil() {
    }

    public static float itemAlpha(float alpha) {
        if (alpha >= 1.0f) {
            return 1.0f;
        }
        if (alpha <= 0.0f) {
            return 0.0f;
        }

        // Make thin held-item geometry fade out a bit faster so it visually
        // tracks the living model more closely near full invisibility.
        return alpha * alpha;
    }
}
