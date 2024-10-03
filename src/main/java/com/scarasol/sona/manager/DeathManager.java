package com.scarasol.sona.manager;

import com.scarasol.sona.configuration.CommonConfig;
import net.minecraft.core.Registry;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class DeathManager {
    private static final Map<UUID, List<Tuple<ItemStack, Integer>>> storedVanillaInventories = new HashMap<>();

    public static void KeepItem(Player player){
        Inventory inventory = player.getInventory();
        Level level = player.getLevel();
        if (!level.isClientSide()){
            List<Tuple<ItemStack, Integer>> vanillaInventoryItem = new ArrayList<>();
            for(int i = 0; i <= 40; ++i) {
                ItemStack item = inventory.getItem(i);
                if ((CommonConfig.ARMOR_KEEP.get() && i >= 36 && i <= 39) || CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(item.getItem()).toString(), CommonConfig.KEEP_WHITELIST.get()) != -1) {
                    vanillaInventoryItem.add(new Tuple<>(item, i));
                    inventory.removeItem(item);
                }
            }
            storedVanillaInventories.put(player.getUUID(), vanillaInventoryItem);
        }
    }

    public static void respawnItem(Player player){
        if (storedVanillaInventories.containsKey(player.getUUID())) {
            List<Tuple<ItemStack, Integer>> list = storedVanillaInventories.get(player.getUUID());
            for (Tuple<ItemStack, Integer> tuple : list){
                player.getInventory().setItem(tuple.getB(), tuple.getA());
                storedVanillaInventories.remove(player.getUUID());
            }
        }
    }
}
