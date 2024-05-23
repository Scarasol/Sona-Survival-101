package com.scarasol.sona.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PhysicalEffect extends MobEffectBase{
    public PhysicalEffect(MobEffectCategory mobEffectCategory, int integer) {
        super(mobEffectCategory, integer);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList();
    }
}
