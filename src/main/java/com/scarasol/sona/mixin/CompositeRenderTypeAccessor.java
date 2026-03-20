package com.scarasol.sona.mixin;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Scarasol
 */
@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public interface CompositeRenderTypeAccessor {
    @Accessor("state")
    RenderType.CompositeState sona$getState();
}