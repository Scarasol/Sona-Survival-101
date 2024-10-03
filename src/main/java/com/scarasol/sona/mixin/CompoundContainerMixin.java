package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.ICompoundContainerAccessor;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CompoundContainer.class)
public abstract class CompoundContainerMixin implements Container, ICompoundContainerAccessor {

    @Shadow @Final private Container container1;

    @Shadow @Final private Container container2;

    @Override
    @Unique
    public Container getContainer1(){
        return container1;
    }

    @Override
    @Unique
    public Container getContainer2(){
        return container2;
    }
}
