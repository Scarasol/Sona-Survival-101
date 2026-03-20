package com.scarasol.sona.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Scarasol
 */
@Mixin(RenderType.CompositeState.class)
public interface CompositeStateAccessor {
    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard sona$getTextureState();
}