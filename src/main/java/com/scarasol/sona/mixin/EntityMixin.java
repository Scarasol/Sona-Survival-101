package com.scarasol.sona.mixin;

import com.scarasol.sona.accessor.ISonaDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * @author Scarasol
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void sonaRestoreFrom(Entity oldEntity, CallbackInfo ci) {
        if (this instanceof ISonaDataAccessor newData && oldEntity instanceof ISonaDataAccessor oldData) {
            newData.copySonaData(oldData);
        }
    }
}
