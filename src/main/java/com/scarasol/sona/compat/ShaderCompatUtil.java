package com.scarasol.sona.compat;

import net.irisshaders.iris.api.v0.IrisApi;

/**
 * @author Scarasol
 */
public class ShaderCompatUtil {

    public static boolean isShaderActive() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
