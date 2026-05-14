package com.scarasol.sona.init;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(modid = "sona", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SonaShaders {

    // 保存我们的核心着色器实例
    public static ShaderInstance entityDitherShader;
    public static ShaderInstance infectionShaderSkyPostShader;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sona", "rendertype_entity_dither"), DefaultVertexFormat.NEW_ENTITY),
                shaderInstance -> entityDitherShader = shaderInstance
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sona", "infection_shader_sky_post"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shaderInstance -> infectionShaderSkyPostShader = shaderInstance
        );
    }
}
