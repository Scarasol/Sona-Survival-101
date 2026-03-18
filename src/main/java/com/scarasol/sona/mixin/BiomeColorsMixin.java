package com.scarasol.sona.mixin;

import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(BiomeColors.class)
public abstract class BiomeColorsMixin {

    @Inject(method = "getAverageGrassColor", cancellable = true, at = @At("RETURN"))
    private static void sona$getAverageGrassColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        Level level = Minecraft.getInstance().level;
        if (InfectionManager.canChunkInfection(level)) {
            int oldColor = cir.getReturnValue();
            Vec3 newColor = InfectionManager.getInfectionChunkGrassColor(new Vec3((oldColor >> 16) & 0XFF, (oldColor >> 8) & 0XFF, oldColor & 0XFF), blockPos.getCenter(), level);
            if (newColor != null) {
                cir.setReturnValue(((int)newColor.x << 16) | ((int)newColor.y << 8) | (int)newColor.z);
            }
        }

    }

    @Inject(method = "getAverageFoliageColor", cancellable = true, at = @At("RETURN"))
    private static void sona$getAverageFoliageColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        Level level = Minecraft.getInstance().level;
        if (InfectionManager.canChunkInfection(level)) {
            int oldColor = cir.getReturnValue();
            Vec3 newColor = InfectionManager.getInfectionChunkGrassColor(new Vec3((oldColor >> 16) & 0XFF, (oldColor >> 8) & 0XFF, oldColor & 0XFF), blockPos.getCenter(), level);
            if (newColor != null) {
                cir.setReturnValue(((int)newColor.x << 16) | ((int)newColor.y << 8) | (int)newColor.z);
            }
        }

    }

    @Inject(method = "getAverageWaterColor", cancellable = true, at = @At("RETURN"))
    private static void sona$getAverageWaterColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        Level level = Minecraft.getInstance().level;
        if (InfectionManager.canChunkInfection(level)) {
            int oldColor = cir.getReturnValue();
            Vec3 newColor = InfectionManager.getInfectionChunkWaterColor(new Vec3((oldColor >> 16) & 0XFF, (oldColor >> 8) & 0XFF, oldColor & 0XFF), blockPos.getCenter(), level);
            if (newColor != null) {
                cir.setReturnValue(((int)newColor.x << 16) | ((int)newColor.y << 8) | (int)newColor.z);
            }
        }

    }
}
