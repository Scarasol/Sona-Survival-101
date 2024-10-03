package com.scarasol.sona.effect;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;

public class Stun extends PhysicalEffect{

    public Stun() {
        super(MobEffectCategory.HARMFUL, -10027060);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "F75BD22A-F572-7EBF-6125-5232C7A671E4", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "8C9E359C-D4C8-8600-96C2-EE1289A87C0F", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), "8C9E359C-D4C8-8600-96C2-EE1289A87C0F", 1000.0, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player){
            if (entity.level() instanceof ServerLevel serverLevel && entity.getServer() != null)
                entity.getServer().getCommands().performPrefixedCommand(
                        new CommandSourceStack(CommandSource.NULL, entity.position(),
                                entity.getRotationVector(), serverLevel, 4,
                                entity.getName().getString(),
                                entity.getDisplayName(), entity.getServer(), entity),
                        "playsound sona:tinnitus player @s");
        }
    }

}
