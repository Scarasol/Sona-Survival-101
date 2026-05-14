package com.scarasol.sona.compat;

import net.irisshaders.iris.api.v0.IrisApi;

/**
 * @author Scarasol
 */
public final class ShaderCompatUtil {
    private ShaderCompatUtil() {
    }

    public static boolean isShaderActive() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
