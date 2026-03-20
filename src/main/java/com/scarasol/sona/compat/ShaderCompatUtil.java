package com.scarasol.sona.compat;

import net.minecraftforge.fml.ModList;

/**
 * @author Scarasol
 */
public class ShaderCompatUtil {

    // 缓存模组是否加载，避免高频检查带来的开销
    private static final boolean IS_OCULUS_LOADED = ModList.get().isLoaded("oculus");

    /**
     * 实时检测当前是否正在使用 Oculus 光影包
     */
    public static boolean isShaderActive() {
        if (!IS_OCULUS_LOADED) {
            return false;
        }

        try {
            // 动态调用 Iris API 检查光影是否处于激活状态
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = apiClass.getMethod("getInstance").invoke(null);
            return (boolean) apiClass.getMethod("isShaderPackInUse").invoke(instance);
        } catch (Exception e) {
            return false;
        }
    }
}