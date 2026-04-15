package com.scarasol.sona.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * @author Scarasol
 */
public class SonaTags {
    public static final TagKey<Item> CORRODED_IMMUNE = ItemTags.create(new ResourceLocation("forge:corroded_immune"));
    public static final TagKey<Item> GAS_MASK = ItemTags.create(new ResourceLocation("forge:gas_mask"));
    public static final TagKey<DamageType> NO_SHAKE = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("forge:no_shake"));
    public static final TagKey<EntityType<?>> NEUTRALITY_TARGETS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("sona:neutrality_targets"));
}
