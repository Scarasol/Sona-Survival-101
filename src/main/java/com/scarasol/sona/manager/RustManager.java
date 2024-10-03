package com.scarasol.sona.manager;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.item.IRustItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RustManager {

    private static final UUID MAINHAND = UUID.fromString("896D3245-0A4E-2D3B-C6F1-01FF91CBC32E");
    private static final UUID HELMET = UUID.fromString("3F118FAD-E494-B937-AC9E-7BEEEDDD1E36");
    private static final UUID CHESTPLATE = UUID.fromString("BF2875F6-0B93-3943-C365-FB58C8A26AD2");
    private static final UUID LEGGINGS = UUID.fromString("35E878C0-2E0B-34C0-DC9E-B35FBE7A85C0");
    private static final UUID BOOTS = UUID.fromString("F160DDE5-F273-FB79-BFD5-30DD470A988E");

    public static void putRust(ItemStack itemStack, double rustValue){
        itemStack.getOrCreateTag().putDouble("RustValue", rustValue);
    }

    public static double getRust(ItemStack itemStack){
         return itemStack.getOrCreateTag().getDouble("RustValue");
    }

    public static double getRust(Object object){
        if (object instanceof ItemStack itemStack)
            return getRust(itemStack);
        return -1;
    }

    public static void addRust(ItemStack itemStack, double addition) {
        double rust = addition > 0 ? Math.min(100, addition + getRust(itemStack)) : Math.max(0, addition + getRust(itemStack));
        putRust(itemStack, rust);
    }

    public static void putWaxed(ItemStack itemStack, int times){
        itemStack.getOrCreateTag().putInt("Waxed", times);
    }

    public static int getWaxed(ItemStack itemStack){
        return itemStack.getOrCreateTag().getInt("Waxed");
    }

    public static void addWaxed(ItemStack itemStack, int addition) {
        int waxed = addition > 0 ? Math.min(CommonConfig.WAX_TIMES.get(), addition + getWaxed(itemStack)) : Math.max(0, addition + getWaxed(itemStack));
        putWaxed(itemStack, waxed);
    }

    public static boolean isWaxed(ItemStack itemStack){
        return itemStack.getOrCreateTag().getInt("Waxed") > 0;
    }

    public static void rustItem(ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot){
        if (!canBeRust(itemStack))
            return;
        double rustValue = getRust(itemStack);
        if (rustValue >= 70 && new Random().nextDouble() < (rustValue - 70) / 200) {
            itemStack.hurtAndBreak(9999999, livingEntity, consumer -> consumer.broadcastBreakEvent(equipmentSlot));
        }
        if (isWaxed(itemStack)){
            if (!CommonConfig.WAX_PERMANENT.get())
                addWaxed(itemStack, -1);
            return;
        }
        rustValue = new Random().nextDouble(0.2, 1.0) + rustValue / 100;
        addRust(itemStack, rustValue);
    }

    public static void rustItem(Object object, LivingEntity livingEntity, EquipmentSlot equipmentSlot){
        if (object instanceof ItemStack itemStack)
            rustItem(itemStack, livingEntity, equipmentSlot);
    }

    public static void addRustAttributeModifier(ItemAttributeModifierEvent event){
        ItemStack itemStack = event.getItemStack();
        if (!canBeRust(itemStack))
            return;
        double rustValue = getRust(itemStack);
        if (rustValue < 40)
            return;
        UUID equipmentSlot;
        double multiplier;
        if (itemStack.getItem() instanceof TieredItem && event.getSlotType() == EquipmentSlot.MAINHAND){
            double value = 1;
            equipmentSlot = MAINHAND;
            if (rustValue >= 70){
                multiplier = -0.5;
            }else {
                multiplier = -0.25;
            }
            for (AttributeModifier attributeModifier : event.getModifiers().get(Attributes.ATTACK_DAMAGE)) {
                if (attributeModifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    value += attributeModifier.getAmount();
                }
            }
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(equipmentSlot, "Rust Modifier", multiplier * value, AttributeModifier.Operation.ADDITION));
        }else if (itemStack.getItem() instanceof ArmorItem armorItem && event.getSlotType() == armorItem.getEquipmentSlot()){
            if (rustValue >= 70){
                multiplier = -0.1;
            }else {
                multiplier = -0.05;
            }
            switch (armorItem.getEquipmentSlot()){
                case CHEST -> equipmentSlot = CHESTPLATE;
                case LEGS -> equipmentSlot = LEGGINGS;
                case FEET -> equipmentSlot = BOOTS;
                default -> equipmentSlot = HELMET;
            }
            event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(equipmentSlot, "Rust Modifier", multiplier, AttributeModifier.Operation.MULTIPLY_BASE));
        }

    }

    public static boolean canBeRust(ItemStack itemStack){
        return canBeRust(itemStack.getItem());
    }

    public static boolean canBeRust(Item item){
        if (CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(item).toString(), CommonConfig.RUST_WHITELIST.get()) != -1)
            return false;
        if (item instanceof TieredItem tieredItem && tieredItem.getTier() == Tiers.IRON){
            return true;
        }
        if (item instanceof ArmorItem armorItem && (armorItem.getMaterial() == ArmorMaterials.IRON || armorItem.getMaterial() == ArmorMaterials.CHAIN)){
            return true;
        }
        return CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(item).toString(), CommonConfig.RUST_BLACKLIST.get()) != -1;
    }

    public static void tooltipInsert(List<Component> toolTip, ItemStack itemStack) {
        double rustValue = getRust(itemStack);
        if (rustValue < 40){
            toolTip.add(1, Component.literal(Component.translatable("tooltip.sona.rust.brand_new").getString()).withStyle(ChatFormatting.DARK_GREEN));
        }else if (rustValue < 70){
            toolTip.add(1, Component.literal(Component.translatable("tooltip.sona.rust.slightly_rusted").getString()).withStyle(ChatFormatting.YELLOW));
            if (itemStack.getItem() instanceof TieredItem)
                toolTip.add(7, Component.literal("-5% " + Component.translatable("tooltip.sona.rust.tool_rust").getString()).withStyle(ChatFormatting.RED));
        }else {
            toolTip.add(1, Component.literal(Component.translatable("tooltip.sona.rust.heavily_rusted").getString()).withStyle(ChatFormatting.RED));
            if (itemStack.getItem() instanceof TieredItem)
                toolTip.add(7, Component.literal("-20% " + Component.translatable("tooltip.sona.rust.tool_rust").getString()).withStyle(ChatFormatting.RED));
        }
        if (isWaxed(itemStack)){
            if (CommonConfig.WAX_PERMANENT.get()){
                toolTip.add(2, Component.literal(Component.translatable("tooltip.sona.rust.waxed").getString()).withStyle(ChatFormatting.DARK_GREEN));
            }else {
                toolTip.add(2, Component.literal(Component.translatable("tooltip.sona.rust.waxed_remaining").getString() + getWaxed(itemStack)).withStyle(ChatFormatting.DARK_GREEN));
            }
        }
    }

    public static void onAttacked(LivingEntity livingEntity) {
        EquipmentSlot equipmentSlot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, livingEntity.getRandom().nextInt(4));
        ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
        if (canBeRust(itemStack))
            rustItem(itemStack, livingEntity, equipmentSlot);
    }

    public static boolean wax(ItemStack itemStack, ItemStack waxItem, LivingEntity livingEntity){
        if (!canBeRust(itemStack))
            return false;
        int index = CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(waxItem.getItem()).toString(), CommonConfig.WAX_ITEM.get());
        if (index == -1)
            return false;
        String[] waxInfo = CommonConfig.WAX_ITEM.get().get(index).split(",");
        if (waxInfo.length < 2)
            return false;
        int damage = Integer.parseInt(waxInfo[1].trim());
        if (consume(waxItem, damage, livingEntity)){
            putWaxed(itemStack, CommonConfig.WAX_TIMES.get());
            rustParticle(livingEntity.level(), new Random(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), true);
            return true;
        }
        return false;
    }

    public static boolean removalRust(ItemStack itemStack, ItemStack removalItem, LivingEntity livingEntity){
        if (!canBeRust(itemStack))
            return false;
        int index = CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(removalItem.getItem()).toString(), CommonConfig.RUST_REMOVE_ITEM.get());
        if (index == -1)
            return false;
        String[] removalInfo = CommonConfig.RUST_REMOVE_ITEM.get().get(index).split(",");
        if (removalInfo.length < 3)
            return false;
        int damage = Integer.parseInt(removalInfo[2].trim());
        double removal = -Double.parseDouble(removalInfo[1].trim());
        if (consume(removalItem, damage, livingEntity)) {
            addRust(itemStack, removal);
            rustParticle(livingEntity.level(), new Random(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), false);
            return true;
        }
        return false;
    }

    private static boolean consume(ItemStack itemStack, int number, LivingEntity livingEntity){
        if (livingEntity instanceof Player player && player.isCreative())
            return true;
        if (itemStack.isDamageableItem()){
            if (number > itemStack.getMaxDamage() - itemStack.getDamageValue())
                return false;
            itemStack.hurtAndBreak(number, livingEntity, consumer -> consumer.broadcastBreakEvent(EquipmentSlot.OFFHAND));
        }else {
            if (number > itemStack.getCount())
                return false;
            itemStack.shrink(1);
        }
        return true;
    }

    public static void rustParticle(Level level, Random random, double x, double y, double z, boolean wax){
        SimpleParticleType particleType;
        SoundEvent soundEvent;
        if (wax){
            particleType = ParticleTypes.WAX_ON;
            soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.honeycomb.wax_on"));
        }else {
            particleType = ParticleTypes.SCRAPE;
            soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.axe.scrape"));
        }
        for (int i = 0; i < 10; ++i) {
            double d4 = random.nextGaussian() * 0.02;
            double d5 = random.nextGaussian() * 0.02;
            double d6 = random.nextGaussian() * 0.02;
            double d = 0.95;
            level.addParticle(particleType, x + 0.13124999403953552 + 0.737500011920929 * (double)random.nextFloat(), y + d + (double)random.nextFloat() * (1.0 - d), z + 0.13124999403953552 + 0.737500011920929 * (double)random.nextFloat(), d4, d5, d6);
            level.playSound(null, x, y, z, soundEvent, SoundSource.PLAYERS, 1, 1);
        }
    }

    public static void changeRustModel(ItemStack itemStack){
        if (itemStack.getItem() instanceof IRustItem && canBeRust(itemStack)){
            if (getRust(itemStack) > 70){
                itemStack.getOrCreateTag().putInt("CustomModelData", 1);
            }else {
                itemStack.getOrCreateTag().putInt("CustomModelData", 0);
            }
        }
    }

    public static void changeRustModel(Object object){
        if (object instanceof ItemStack itemStack)
            changeRustModel(itemStack);
    }
}
