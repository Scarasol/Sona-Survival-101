package com.scarasol.sona.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * @author Scarasol
 */
public interface IGasMask {

    boolean isGasMask(LivingEntity livingEntity, ItemStack itemStack);

}
