package com.scarasol.sona.manager;

import com.scarasol.sona.SonaMod;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.network.NetworkHandler;
import com.scarasol.sona.network.RotPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class RotManager {


    public static void rotWhenStack(ItemStack itemStack, double rotValue1, double rotValue2, int count1, int count2, long saveTime){
        if (itemStack.isEdible() && count2 > 0){
            double rotValue = (rotValue1 * count1 + rotValue2 * count2) / (count1 + count2);
            putRot(itemStack, rotValue);
            putRotSaveTime(itemStack, Math.max(Math.min(getRotSaveTime(itemStack), saveTime), 0));
        }
    }

    public static void putRot(ItemStack itemStack, double rotValue){
        itemStack.getOrCreateTag().putDouble("RotValue", rotValue);
    }

    public static double getRot(ItemStack itemStack){
        return itemStack.getOrCreateTag().getDouble("RotValue");
    }

    public static void addRot(ItemStack itemStack, double addition) {
        if (addition > 0)
            addition = addition * CommonConfig.ROT_WEIGHT.get().floatValue();
        addActualRot(itemStack, addition);
    }

    public static void addActualRot(ItemStack itemStack, double addition){
        double rot = addition > 0 ? Math.min(100, addition + getRot(itemStack)) : Math.max(0, addition + getRot(itemStack));
        putRot(itemStack, rot);
    }

    public static void putMultiplier(ItemStack itemStack, double rotMultiplier){
        itemStack.getOrCreateTag().putDouble("RotMultiplier", rotMultiplier);
    }

    public static double getMultiplier(ItemStack itemStack){
        double multiplier = itemStack.getOrCreateTag().getDouble("RotMultiplier");
        if (multiplier == 0){
            multiplier = initMultiplier(itemStack);
            putMultiplier(itemStack, multiplier);
        }
        return multiplier;
    }

    public static void putRotSaveTime(ItemStack itemStack, long gameTime){
        itemStack.getOrCreateTag().putLong("RotSaveTime", gameTime);
    }

    public static long getRotSaveTime(ItemStack itemStack){
        return itemStack.getOrCreateTag().getLong("RotSaveTime");
    }

    public static double initMultiplier(ItemStack itemStack){
        int index = Math.max(CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), CommonConfig.ROT_DETAIL.get()), CommonConfig.tagSearch(itemStack, CommonConfig.ROT_DETAIL.get()));
        double rate = 1;
        if (index == -1)
            return rate;
        String[] str = CommonConfig.ROT_DETAIL.get().get(index).split(",");
        if (str.length < 3)
            return rate;
        if (Double.parseDouble(str[1].trim()) <= 0)
            return rate;
        rate = Double.parseDouble(str[1].trim());
        return rate;
    }

    public static void eatRotFood(LivingEntity livingEntity, ItemStack itemStack){
        double rotValue = getRot(itemStack);
        if (rotValue >= 90){
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 600, 0, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0, false, false));
        }else if (rotValue >= 70 && livingEntity.getLevel().getRandom().nextDouble() > 0.75){
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 600, 0, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0, false, false));
        }else if (rotValue >= 40 && livingEntity.getLevel().getRandom().nextDouble() > 0.9){
            livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0, false, false));
        }
    }

    public static double getContainerMultiplier(String key){
        int index = CommonConfig.findIndex(key, CommonConfig.ROT_CONTAINER.get());
        if (index != -1) {
            String[] str = CommonConfig.ROT_CONTAINER.get().get(index).split(",");
            if (str.length >= 2) {
                return Double.parseDouble(str[1].trim());
            }
        }
        return 1;
    }

    public static void syncRotValue(double rotValue, int slot, boolean flag, ServerPlayer player){
        NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new RotPacket(rotValue, slot, flag));
    }

    public static void rotInContainer(ServerPlayer serverPlayer, List<Slot> slots, int size, double containerMultiplier, long gameTime, double temperature){
        for (int i = 0; i < size; i++){
            Slot slot = slots.get(i);
            ItemStack itemStack = slot.getItem().copy();
            if (itemStack.isEdible() && canBeRotten(itemStack)){
                long saveTime = getRotSaveTime(itemStack);
                if (saveTime == 0) continue;
                double rotValue = getRot(itemStack);
                int cycle = 1200;
                if (CommonConfig.ROT_TEMPERATURE.get()){
                    cycle = Math.max((int)(cycle / temperature / getMultiplier(itemStack) / containerMultiplier), 1);
                }else {
                    cycle = Math.max((int)(cycle / getMultiplier(itemStack) / containerMultiplier), 1);
                }
                double rotAddition = (gameTime - saveTime) / cycle;
                rotAddition = rotAddition * CommonConfig.ROT_WEIGHT.get();
                if (rotValue + rotAddition >= 100){
                    rotten(itemStack, null, slot);
                }else {
                    addActualRot(itemStack, rotAddition);
                    slot.set(itemStack);
                    syncRotValue(getRot(itemStack), i, false, serverPlayer);
                    slot.setChanged();
                }
            }
        }
    }

    public static void rotTimeUpdate(List<Slot> slots, int size, long gameTime){
        for (int i = 0; i < size; i++){
            Slot slot = slots.get(i);
            ItemStack itemStack = slot.getItem();
            if (itemStack.isEdible() && canBeRotten(itemStack)){
                putRotSaveTime(itemStack, gameTime);
                slot.set(itemStack);
                slot.setChanged();
            }
        }
    }

    public static void rotten(ItemStack itemStack, SlotAccess slotAccess, Slot slot){
        int index = Math.max(CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), CommonConfig.ROT_DETAIL.get()), CommonConfig.tagSearch(itemStack, CommonConfig.ROT_DETAIL.get()));
        if (index == -1){
            itemStack.setCount(0);
            return;
        }
        String[] str = CommonConfig.ROT_DETAIL.get().get(index).split(",");
        if (str.length < 3){
            itemStack.setCount(0);
            return;
        }
        int count = itemStack.getCount();
        ItemStack itemStackNew = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(str[2].trim())));
        itemStackNew.setCount(count);
        if (slot == null && slotAccess != null){
            slotAccess.set(itemStackNew);
        }else if (slot != null && slotAccess == null){
            slot.set(itemStackNew);
            slot.setChanged();
        }
    }

    public static boolean canBeRotten(ItemStack itemStack){
        return !CommonConfig.ROT_WHITELIST.get().contains(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
    }

    public static void rotTick(Object object, Entity entity, int slot, double temperature){
        if (object instanceof ItemStack itemStack){
            if (itemStack.isEdible() && canBeRotten(itemStack)){
                int cycle = 1200;
                if (CommonConfig.ROT_TEMPERATURE.get()){
                    cycle = Math.max((int)(cycle / temperature / getMultiplier(itemStack)), 1);
                }else {
                    cycle = Math.max((int)(cycle / getMultiplier(itemStack)), 1);
                }
                if (entity.getLevel().getGameTime() % cycle == 0){
                    addRot(itemStack, 1);
                    if (entity instanceof ServerPlayer serverPlayer)
                        syncRotValue(getRot(itemStack), slot, true, serverPlayer);
                }
                if (getRot(itemStack) >= 100){
                    rotten(itemStack, entity.getSlot(slot), null);
                }
            }
        }
    }

    public static void tooltipInsert(List<Component> toolTip, ItemStack itemStack) {
        double rotValue = getRot(itemStack);
        if (rotValue < 40){
            toolTip.add(Math.min(1, toolTip.size()), new TranslatableComponent("tooltip.sona.rot.fresh").withStyle(ChatFormatting.DARK_GREEN));
        }else if (rotValue < 70){
            toolTip.add(Math.min(1, toolTip.size()), new TranslatableComponent("tooltip.sona.rot.slightly_spoiled").withStyle(ChatFormatting.DARK_AQUA));
        }else if (rotValue < 90){
            toolTip.add(Math.min(1, toolTip.size()), new TranslatableComponent("tooltip.sona.rot.spoiled").withStyle(ChatFormatting.YELLOW));
        }else {
            toolTip.add(Math.min(1, toolTip.size()), new TranslatableComponent("tooltip.sona.rot.heavily_spoiled").withStyle(ChatFormatting.RED));
        }
    }


}
