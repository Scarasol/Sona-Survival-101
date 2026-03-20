package com.scarasol.sona.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

/**
 * @author Scarasol
 */
@Mixin(RenderStateShard.TextureStateShard.class)
public interface TextureStateShardAccessor {
    @Accessor("texture")
    Optional<ResourceLocation> sona$getTexture();
}