package com.scarasol.sona.configuration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommonConfig {
//    private static final String REGISTRY_NAME_MATCHER = "([a-z0-9_.#-]+:[a-z0-9_/.-]+)";

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Boolean> INFECTION_OPEN;
    public static ForgeConfigSpec.ConfigValue<List<String>> INFECTION_SOURCE_MOB;
    public static ForgeConfigSpec.ConfigValue<List<String>> INFECTION_SOURCE_PROJECTILE;
    public static ForgeConfigSpec.ConfigValue<List<String>> INFECTION_SOURCE_ITEM;
    public static ForgeConfigSpec.ConfigValue<List<String>> SUSCEPTIBLE_POPULATION;
    public static ForgeConfigSpec.ConfigValue<Boolean> BLUR_MESSAGE;
    public static ForgeConfigSpec.ConfigValue<Boolean> TURN_ZOMBIE;
    public static ForgeConfigSpec.ConfigValue<Integer> INFECTION_THRESHOLD;
    public static ForgeConfigSpec.ConfigValue<List<String>> ZOMBIE_LIST;
    public static ForgeConfigSpec.ConfigValue<Integer> INFECTION_OVERLAY_PRESET;
    public static ForgeConfigSpec.ConfigValue<Integer> INFECTION_X_OFFSET;
    public static ForgeConfigSpec.ConfigValue<Integer> INFECTION_Y_OFFSET;

    public static ForgeConfigSpec.ConfigValue<Boolean> INJURY_OPEN;
    public static ForgeConfigSpec.ConfigValue<List<String>> INJURY_EXCEPT_DAMAGESOURCE;
    public static ForgeConfigSpec.ConfigValue<List<String>> INJURY_TREATMENT_ITEM;
    public static ForgeConfigSpec.ConfigValue<Boolean> HEAL_WHILE_SLEEP;
    public static ForgeConfigSpec.ConfigValue<Integer> HEAL_AMOUNT;
    public static ForgeConfigSpec.ConfigValue<Boolean> HEAL_NEED_BANDAGE;
    public static ForgeConfigSpec.ConfigValue<Integer> HEAL_THRESHOLD;
    public static ForgeConfigSpec.ConfigValue<Integer> INJURY_OVERLAY_PRESET;
    public static ForgeConfigSpec.ConfigValue<Integer> INJURY_X_OFFSET;
    public static ForgeConfigSpec.ConfigValue<Integer> INJURY_Y_OFFSET;
    public static ForgeConfigSpec.ConfigValue<Boolean> RISE_UNDERWATER;

    public static ForgeConfigSpec.ConfigValue<Boolean> ROT_OPEN;
    public static ForgeConfigSpec.ConfigValue<Boolean> ROT_STACKABLE;
    public static ForgeConfigSpec.ConfigValue<Boolean> ROT_EFFECT;
    public static ForgeConfigSpec.ConfigValue<Boolean> ROT_TEMPERATURE;
    public static ForgeConfigSpec.ConfigValue<List<String>> ROT_WHITELIST;
    public static ForgeConfigSpec.ConfigValue<List<String>> ROT_TEMPERATURE_WHITELIST;
    public static ForgeConfigSpec.ConfigValue<List<String>> ROT_DETAIL;
    public static ForgeConfigSpec.ConfigValue<List<String>> ROT_CONTAINER;

    public static ForgeConfigSpec.ConfigValue<Boolean> RUST_OPEN;
    public static ForgeConfigSpec.ConfigValue<List<String>> RUST_WHITELIST;
    public static ForgeConfigSpec.ConfigValue<List<String>> RUST_BLACKLIST;
    public static ForgeConfigSpec.ConfigValue<List<String>> RUST_REMOVE_ITEM;
    public static ForgeConfigSpec.ConfigValue<List<String>> WAX_ITEM;
    public static ForgeConfigSpec.ConfigValue<Boolean> WAX_PERMANENT;
    public static ForgeConfigSpec.ConfigValue<Integer> WAX_TIMES;

    public static ForgeConfigSpec.ConfigValue<Boolean> SOUND_OPEN;
    public static ForgeConfigSpec.ConfigValue<List<String>> SOUND_WHITELIST;
    public static ForgeConfigSpec.ConfigValue<List<String>> SOUND_ATTRACTED_MOB_WHITELIST;
    public static ForgeConfigSpec.ConfigValue<List<String>> SOUND_ATTRACTED_MOB_BLACKLIST;
    public static ForgeConfigSpec.ConfigValue<Boolean> SPRINT_SOUND;

    public static ForgeConfigSpec.ConfigValue<Boolean> PHYSICAL_EFFECT_REMOVE;
    public static ForgeConfigSpec.ConfigValue<Double> LOCK_PERCENT;
    public static ForgeConfigSpec.ConfigValue<List<String>> LOCK_BREAKER;
    public static ForgeConfigSpec.ConfigValue<Boolean> LOCK_WHITELIST_OPEN;
    public static ForgeConfigSpec.ConfigValue<List<String>> LOCK_WHITELIST;

    public static ForgeConfigSpec.ConfigValue<Boolean> ARMOR_KEEP;
    public static ForgeConfigSpec.ConfigValue<List<String>> KEEP_WHITELIST;


    static {
        BUILDER.push("Infection System");
        INFECTION_OPEN = BUILDER.comment("Whether to turn on the infection system." +
                "\nThe Serverside controls the switching of the system and the Clientside controls the display of the HUD.")
                .define("Turn on Infection System", true);
        INFECTION_SOURCE_MOB = BUILDER.comment("Mobs that cause infection (melee attacks only), including undead by default." +
                "\nFormat: \"minecraft:zombie\" (\"\" required)")
                .define("Source of Infection Mobs", new ArrayList<>());
        INFECTION_SOURCE_PROJECTILE = BUILDER.comment("Projectiles that cause infection." +
                "\nFormat: \"minecraft:arrow\" (\"\" required)")
                .define("Source of Infection Projectiles", new ArrayList<>());
        INFECTION_SOURCE_ITEM = BUILDER.comment("""
                Items that cause or cure infection.
                Format: "minecraft:rotten_flesh, 75, 8, 12" ("" required) means that using rotting flesh has a 75% chance of gaining 8-12(can be negative) infection level.
                Tag supported.""")
                .define("Source of Infection Items", Arrays.asList("minecraft:rotten_flesh, 75, 8, 12", "minecraft:enchanted_golden_apple, 100, -100, -100", "minecraft:golden_apple, 100, -30, -30"), Objects::nonNull);
        SUSCEPTIBLE_POPULATION = BUILDER.comment("Mobs that can be infected." +
                "\nFormat: \"minecraft:villager\" (\"\" required)")
                .define("Infected Mobs", new ArrayList<>());
        BLUR_MESSAGE = BUILDER.comment("Whether to taint the player's message at a high level infection.")
                .define("Taint Message", true);
        TURN_ZOMBIE = BUILDER.comment("Whether an entity with a high infection level becomes a zombie after death.")
                .define("Turn into a Zombie", true);
        INFECTION_THRESHOLD = BUILDER.comment("An entity's infection level needs to be greater than the threshold to become a zombie after death.")
                .defineInRange("Infection Level Threshold", 75, 0, 100);
        ZOMBIE_LIST = BUILDER.comment("Zombies that the dead turn into." +
                "\nFormat: \"minecraft:zombie, 80\" (\"\" required) means that each dead has a weight of 80 to turn into a zombie.")
                .define("Zombie List", Arrays.asList("minecraft:zombie, 80", "minecraft:husk, 20"), Objects::nonNull);

        BUILDER.push("Infection HUD Customize");
        INFECTION_OVERLAY_PRESET = BUILDER.comment("Select the HUD preset, 0 is customized.")
                .defineInRange("HUD Preset", 1, 0, 3);
        INFECTION_X_OFFSET = BUILDER.defineInRange("HUD X Offset (needs HUD Preset is 0)", 0, 0, 2048);
        INFECTION_Y_OFFSET = BUILDER.defineInRange("HUD Y Offset (needs HUD Preset is 0)", 0, 0, 2048);
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("Injury System");
        INJURY_OPEN = BUILDER.comment("Whether to turn on the injury system." +
                "\nThe Serverside controls the switching of the system and the Clientside controls the display of the HUD.")
                .define("Turn on Injury System", true);
        HEAL_WHILE_SLEEP = BUILDER.comment("Whether to recover the injury level by sleep.")
                .define("Heal By Sleep", true);
        HEAL_AMOUNT = BUILDER.comment("How much can injury level be recovered by once sleep.")
                .defineInRange("Sleep Heal Amount", 25, 0, 100);
        HEAL_NEED_BANDAGE = BUILDER.comment("Whether bandage level greater than 0 is required to recover from sleep when the injury level is too low.")
                .define("Heal Need Bandage", true);
        HEAL_THRESHOLD = BUILDER.comment("Injury level below threshold will require bandage level to recover by sleep.")
                .defineInRange("Injury Level Threshold", 50, 0, 100);
        INJURY_EXCEPT_DAMAGESOURCE = BUILDER.comment("DamageSources that don't cause injury." +
                "\nFormat: \"outOfWorld\" (\"\" required)")
                .define("DamageSource Blacklist", Arrays.asList("outOfWorld", "drown", "starve", "magic", "wither", "dryout", "freeze", "inWall"), Objects::nonNull);
        INJURY_TREATMENT_ITEM = BUILDER.comment("Items that cure injury." +
                "\nFormat: \"minecraft:enchanted_golden_apple, 30, 50\" (\"\" required) means that using Enchanted Golden Apple will gain 30 injury level and 50 bandage level.")
                .define("Cure Injury Items", Arrays.asList("minecraft:enchanted_golden_apple, 30, 50", "minecraft:golden_apple, 10, 20"), Objects::nonNull);

        BUILDER.push("Injury HUD Customize");
        INJURY_OVERLAY_PRESET = BUILDER.comment("Select the HUD preset, 0 is customized.")
                .defineInRange("HUD Preset", 1, 0, 3);
        INJURY_X_OFFSET = BUILDER.defineInRange("HUD X Offset (needs HUD Preset is 0)", 0, 0, 2048);
        INJURY_Y_OFFSET = BUILDER.defineInRange("HUD Y Offset (needs HUD Preset is 0)", 0, 0, 2048);
        RISE_UNDERWATER = BUILDER.comment("Whether the HUD rises when underwater. (needs HUD Preset is 0)")
                .define("Rise Underwater", true);
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("Rot System");
        ROT_OPEN = BUILDER.comment("Whether to turn on the rot system." +
                "\nThe Serverside controls the switching of the system and the Clientside controls the display of the HUD.")
                .define("Turn on Rot System", true);
        ROT_STACKABLE = BUILDER.comment("""
                Whether to the food can be stackable with different level of the rot.
                If turned on, food's level of rot will be averaged based on quantity when stacked.
                WARNING: This feature is not compatible with the backpack module for now.
                """)
                .define("Rot Stackable", true);
        ROT_EFFECT = BUILDER.comment("Whether to consume food that is not fresh may lead to nausea or poisonings.")
                .define("Food not Fresh Causes Nausea", true);
        ROT_TEMPERATURE = BUILDER.comment("Whether the temperature of the biome in which the entity or block is located affect the rate of rot.")
                .define("Temperature Affects Rot", true);
        ROT_WHITELIST = BUILDER.comment("Food that doesn't rot." +
                "\nFormat: \"minecraft:golden_apple\" (\"\" required)")
                .define("Food WhiteList", Arrays.asList("minecraft:golden_carrot", "minecraft:enchanted_golden_apple", "minecraft:golden_apple"), Objects::nonNull);
        ROT_TEMPERATURE_WHITELIST = BUILDER.comment("Blocks in which food rot is not affected by temperature." +
                "\nFormat: \"minecraft:ender_chest\" (\"\" required)")
                .define("Block WhiteList", new ArrayList<>());
        ROT_DETAIL = BUILDER.comment("""
                Specific parameters of the rot.
                Format: "minecraft:porkchop, 1.5, minecraft:rotten_flesh" ("" required) means porkchop will rot at 1.5 times the rate and will end up as rotten flesh.
                By default, food rots for 5 game days, and rotten food will simply disappear.
                Tag supported.""")
                .define("Rot Parameters", Arrays.asList("#forge:cooked_meat, 1.0, minecraft:rotten_flesh", "#forge:raw_meat, 1.5, minecraft:rotten_flesh", "minecraft:spider_eye, 2.0, minecraft:air", "minecraft:rotten_flesh, 2.0, minecraft:air"), Objects::nonNull);
        ROT_CONTAINER = BUILDER.comment("Rate of food decay in different block containers." +
                "\nFormat: \"minecraft:chest, 0.8\" (\"\" required) means food in chests will rot at 0.8 times the rate.")
                .define("Block Container Detail", Arrays.asList("minecraft:ender_chest, 0"), Objects::nonNull);
        BUILDER.pop();

        BUILDER.push("Rust System");
        RUST_OPEN = BUILDER.comment("Whether to turn on the rust system." +
                "\nThe Serverside controls the switching of the system and the Clientside controls the display of the HUD.")
                .define("Turn on Rust System", true);
        RUST_WHITELIST = BUILDER.comment("""
                Items that don't rust.
                Format: "minecraft:iron_sword" ("" required)
                By default, all armors, tools and weapons where the materials are iron will be rust.
                """)
                .define("Rust WhiteList", new ArrayList<>());
        RUST_BLACKLIST = BUILDER.comment("""
                Items will rust even if it isn't made of iron.
                Format: "minecraft:iron_sword" ("" required)
                """)
                .define("Rust BlackList", new ArrayList<>());
        RUST_REMOVE_ITEM = BUILDER.comment("""
                Items that can prevent rusting.
                Format: "minecraft:quartz, 30, 10" ("" required) means that using quartz consumes 10 of its durability (If the item isn't damageable item it will be consumed directly) and removes 30 of rust.
                You need to hold your item need to be descaled in your mainhand with rust removal item in offhand and right click with shift.
                """)
                .define("Rust Removal Item", Arrays.asList("zombiekit:sandpaper, 20, 1", "create:sand_paper, 20, 1", "create:red_sand_paper, 20, 1"), Objects::nonNull);
        WAX_ITEM = BUILDER.comment("""
                Items that can prevent rusting.
                Format: "minecraft:honeycomb, 10" ("" required) means that using honeycomb consumes 10 of its durability (If the item isn't damageable item it will be consumed directly) to wax item.
                You need to hold your item need to be waxed in your mainhand with wax item in offhand and right click with shift.
                """)
                .define("Wax Item", Arrays.asList("minecraft:honeycomb, 1"), Objects::nonNull);
        WAX_PERMANENT = BUILDER.comment("Whether the wax coating is permanent.")
                .define("Wax Coating Permanent", false);
        WAX_TIMES = BUILDER.comment("If the wax coating is not permanent the number of times it can be used.")
                .defineInRange("Wax Coating Durability", 50, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Sound System");
        SOUND_OPEN = BUILDER.comment("Whether to turn on the sound system." +
                "\nThis system will cause the mobs attracted by sound.")
                .define("Turn on Sound System", true);
        SOUND_WHITELIST = BUILDER.comment("""
                The sound will attract mobs.
                Format: "minecraft:entity.generic.explode, 3" ("" required) means that the mobs within (3 + 1) * (0.3 * FOLLOW_RANGE) are attracted to the sound.
                Format: "$chest, 1" ("" required) means that sounds whose name contain "chest" will attract the mobs.
                """)
                .define("Sound WhiteList", Arrays.asList("sona:crate, 1", "minecraft:entity.generic.explode, 3", "$chest, 0"), Objects::nonNull);
        SOUND_ATTRACTED_MOB_WHITELIST = BUILDER.comment("""
                The mobs will be attracted by sound.
                Format: "minecraft:zombie" ("" required)
                By default, all undead mobs will be attracted.
                """)
                .define("Attracted Mob WhiteList", new ArrayList<>());
        SOUND_ATTRACTED_MOB_BLACKLIST = BUILDER.comment("""
                The mobs will NOT be attracted by sound.
                Format: "minecraft:zombie" ("" required)
                """)
                .define("Attracted Mob BlackList", new ArrayList<>());
        SPRINT_SOUND = BUILDER.comment("Whether the sprint attracts the mobs.")
                .define("Sprint Sound", true);
        BUILDER.pop();

        BUILDER.push("Inventory Keep");
        ARMOR_KEEP = BUILDER.comment("Whether or not the player keeps the items in the armor slots after death.")
                .define("Armor Keep", false);
        KEEP_WHITELIST = BUILDER.comment("Items kept after death.")
                .define("Keep Whitelist", new ArrayList<>());
        BUILDER.pop();

        BUILDER.push("Misc");
        PHYSICAL_EFFECT_REMOVE = BUILDER.comment("Whether physical effects (e.g. Ignition, Frost, etc.) can be removed by items that remove all effects.")
                .define("Physical Effects Remove", false);
        LOCK_PERCENT = BUILDER.comment("Loot containers have a chance of being locked when they are generated.")
                .defineInRange("Loot Container Locked Percent", 20, 0D, 100D);
        LOCK_BREAKER = BUILDER.comment("""
                The Items can break the lock.
                Format: "zombiekit:crowbar, 30" ("" required) means that iron_crowbar has a 30% chance of opening locked chests every time right click locked container.
                """)
                .define("Unlock Items", Arrays.asList("zombiekit:crowbar, 30", "zombiekit:netherite_crowbar, 70"), Objects::nonNull);
        LOCK_WHITELIST_OPEN = BUILDER.comment("Whether only loot containers with the loot table in the whitelist will be locked.")
                .define("Lock Table Whitelist Mode Open", false);
        LOCK_WHITELIST = BUILDER.comment("""
                The Loot Table will be locked.
                Format: "chests/end_city_treasure" or "zombiekit:chests/tool" ("" required)
                """)
                .define("Lock Table Whitelist", new ArrayList<>());
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static int findIndex(String string, List<String> items){
        for (int i = 0; i < items.size(); i++){
            String[] str = items.get(i).split(",");
            if (str[0].equals(string)){
                return i;
            }
        }
        return -1;
    }

    public static int tagSearch(ItemStack item, List<String> items){
        for (int i = 0; i < items.size(); i++){
            String[] str = items.get(i).split(",");
            if (str[0].startsWith("#") && item.is(ItemTags.create(new ResourceLocation(str[0].substring(1))))){
                return i;
            }
        }
        return -1;
    }

    public static int containSearch(String string, List<String> items){
        for (int i = 0; i < items.size(); i++){
            String[] str = items.get(i).split(",");
            if (str[0].startsWith("$") && string.contains(str[0].substring(1))){
                return i;
            }
        }
        return -1;
    }

}