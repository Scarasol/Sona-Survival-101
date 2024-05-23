package com.scarasol.sona.effect;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.RustManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;

public class Corrosion extends PhysicalEffect{

    public static final DamageSource CORRODED = new DamageSource("corroded").bypassArmor();
    public static final TagKey<Item> CORRODED_IMMUNE = ItemTags.create(new ResourceLocation("forge:corroded_immune"));

    public Corrosion() {
        super(MobEffectCategory.HARMFUL, -10066432);
    }



    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(CORRODED, (float) (equipmentCorroded(entity, amplifier) * (amplifier + 1) / (0.2 * (amplifier + 1) + 1)));
        if (entity.hasEffect(SonaMobEffects.SLIMINESS.get())){
            int level = entity.getEffect(SonaMobEffects.SLIMINESS.get()).getAmplifier() + 1;
            entity.removeEffect(SonaMobEffects.SLIMINESS.get());
            entity.removeEffect(SonaMobEffects.CORROSION.get());
            entity.getLevel().explode(null, entity.getX(), entity.getY(), entity.getZ(), (amplifier + 1 + level) / 2, Explosion.BlockInteraction.NONE);
        }
    }

    public double equipmentCorroded(LivingEntity entity, int amplifier){
        double exposed = 0;
        for (ItemStack armor : entity.getArmorSlots()){
            if (armor.isEmpty()){
                exposed += 0.25;
            }else {
                if(!armor.is(CORRODED_IMMUNE))
                    armor.hurtAndBreak((int)Math.pow(amplifier + 1, 2), entity, consumer -> {
                        consumer.broadcastBreakEvent(armor.getEquipmentSlot());
                    });
                if (CommonConfig.RUST_OPEN.get() && RustManager.canBeRust(armor))
                    RustManager.addRust(armor, -(amplifier + 1));
            }
        }
        return exposed;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return (duration % 20 == 0);
    }

}
