package com.scarasol.sona.mixin;

import com.scarasol.sona.configuration.CommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin implements Tag {

    @Shadow public abstract @NotNull CompoundTag copy();

    @Shadow public abstract Set<String> getAllKeys();

    @Shadow public abstract boolean contains(String p_128442_);

    @Inject(method = "equals", cancellable = true, at = @At("RETURN"))
    private void onEquals(Object object, CallbackInfoReturnable<Boolean> cir){
        if (!cir.getReturnValue()){
            if (CommonConfig.ROT_STACKABLE.get() && object instanceof CompoundTag compoundTag && (compoundTag.contains("RotValue") || compoundTag.contains("RotSaveTime") || this.contains("RotValue") || this.contains("RotSaveTime"))){
                CompoundTag tags = this.copy();
                tags.remove("RotValue");
                tags.remove("RotSaveTime");
                tags.remove("RotMultiplier");
                CompoundTag objectTags = compoundTag.copy();
                objectTags.remove("RotValue");
                objectTags.remove("RotSaveTime");
                objectTags.remove("RotMultiplier");
                if (tags.equals(objectTags)){
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
