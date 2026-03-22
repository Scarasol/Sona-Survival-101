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

    public static final ForgeConfigSpec.ConfigValue<Boolean> INFECTION_OPEN;
    public static final ForgeConfigSpec.ConfigValue<Double> INFECTION_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Double> INFECTION_INITIAL_VALUE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INFECTION_SOURCE_MOB;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INFECTION_SOURCE_PROJECTILE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INFECTION_SOURCE_ITEM;
    public static final ForgeConfigSpec.ConfigValue<List<String>> SUSCEPTIBLE_POPULATION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BLUR_MESSAGE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> TURN_ZOMBIE;
    public static final ForgeConfigSpec.ConfigValue<Integer> INFECTION_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<List<String>> ZOMBIE_LIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> SPECIFIC_ZOMBIE_LIST;

    public static final ForgeConfigSpec.ConfigValue<Boolean> INFECTED_ZONE_OPEN;
    public static final ForgeConfigSpec.ConfigValue<Integer> INFECTED_ZONE_ZERO_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> INFECTED_ZONE_GENERATION;
    public static final ForgeConfigSpec.ConfigValue<String> INFECTED_ZONE_CUSTOM_GENERATION;
    public static final ForgeConfigSpec.ConfigValue<String> INFECTED_ZONE_INCREASEMENT;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INFECTED_ZONE_STRUCTURE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INFECTED_ZONE_BLOCK;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INFECTED_ZONE_MOB;

    public static final ForgeConfigSpec.ConfigValue<String> INFECTED_ZONE_FOG_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> INFECTED_ZONE_SKY_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> INFECTED_ZONE_GRASS_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> INFECTED_ZONE_WATER_COLOR;

    public static final ForgeConfigSpec.ConfigValue<Integer> INFECTION_OVERLAY_PRESET;
    public static final ForgeConfigSpec.ConfigValue<Integer> INFECTION_X_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> INFECTION_Y_OFFSET;

    public static final ForgeConfigSpec.ConfigValue<Boolean> INJURY_OPEN;
    public static final ForgeConfigSpec.ConfigValue<Double> INJURY_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Double> INJURY_INITIAL_VALUE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INJURY_EXCEPT_DAMAGESOURCE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> INJURY_TREATMENT_ITEM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HEAL_WHILE_SLEEP;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEAL_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HEAL_NEED_BANDAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEAL_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Integer> INJURY_OVERLAY_PRESET;
    public static final ForgeConfigSpec.ConfigValue<Integer> INJURY_X_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> INJURY_Y_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Boolean> RISE_UNDERWATER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ROT_OPEN;
    public static final ForgeConfigSpec.ConfigValue<Double> ROT_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> ROT_RESULT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ROT_STACKABLE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ROT_EFFECT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ROT_TEMPERATURE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> ROT_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> ROT_TEMPERATURE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> ROT_DETAIL;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ROT_WARPED;
    public static final ForgeConfigSpec.ConfigValue<Double> WARPED_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Double> WARPED_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> WARPED_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<String>> ROT_CONTAINER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> RUST_OPEN;
    public static final ForgeConfigSpec.ConfigValue<Double> RUST_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<List<String>> RUST_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> RUST_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> RUST_REMOVE_ITEM;
    public static final ForgeConfigSpec.ConfigValue<List<String>> WAX_ITEM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> WAX_PERMANENT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WAX_TIMES;

    public static final ForgeConfigSpec.ConfigValue<Boolean> SOUND_OPEN;
    public static final ForgeConfigSpec.ConfigValue<List<String>> SOUND_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> SOUND_ATTRACTED_MOB_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> SOUND_ATTRACTED_MOB_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SPRINT_SOUND;
    public static final ForgeConfigSpec.ConfigValue<Integer> DECOY_LIFE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> GUN_SOUND_ATTRACT;
    public static final ForgeConfigSpec.ConfigValue<List<String>> GUN_SOUND_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<Integer> FIRE_EXPOSURE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SILENCE_EXPOSURE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> CHAT_LIMIT;
    public static final ForgeConfigSpec.ConfigValue<Integer> CHAT_RANGE;
    public static final ForgeConfigSpec.ConfigValue<List<String>> RANGE_ITEM;

    public static final ForgeConfigSpec.ConfigValue<Boolean> PHYSICAL_EFFECT_REMOVE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENHANCED_CAMOUFLAGE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EXPOSURE_INDICATOR;
    public static final ForgeConfigSpec.ConfigValue<Double> STUN_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> OVER_DOT;

    public static final ForgeConfigSpec.ConfigValue<Double> LOCK_PERCENT;
    public static final ForgeConfigSpec.ConfigValue<List<String>> LOCK_BREAKER;
    public static final ForgeConfigSpec.ConfigValue<Integer> LOCK_BREAKER_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<List<String>> BLOCK_LOCK_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LOCK_WHITELIST_OPEN;
    public static final ForgeConfigSpec.ConfigValue<List<String>> LOCK_WHITELIST;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ARMOR_KEEP;
    public static final ForgeConfigSpec.ConfigValue<List<String>> KEEP_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<Boolean> UPDATE_LOG;


    static {
        BUILDER.push("Infection System");
        INFECTION_OPEN = BUILDER.comment("Whether to turn on the infection system." +
                "\nThe Serverside controls the switching of the system and the Clientside controls the display of the HUD.")
                .define("Turn on Infection System", true);
        INFECTION_WEIGHT = BUILDER.comment("This weight is multiplied for each infection increase." +
                "\nFor example, if this value is set to 0.5, when the infection should increase by 8, the actual increase is 8 * 0.5 = 4.")
                .defineInRange("Infection Weight", 1.0, 0.0, 5.0);
        INFECTION_INITIAL_VALUE = BUILDER.comment("When a player dies, if the infection value is higher than this value, the infection value will be equal to this value upon respawn.")
                .defineInRange("Infection Initial Value", 0.0, 0.0, 100.0);
        INFECTION_SOURCE_MOB = BUILDER.comment("Mobs that cause infections and how much infection they can cause (melee attacks only), including undead by default." +
                "\nFormat: \"minecraft:zombie, 2\" (\"\" required) means that zombies will causes twice as much infection as normally infected mobs.")
                .define("Source of Infection Mobs", new ArrayList<>());
        INFECTION_SOURCE_PROJECTILE = BUILDER.comment("Projectiles that cause infections and how much infection they can cause." +
                "\nFormat: \"minecraft:arrow, 2\" (\"\" required) means that zombies will causes twice as much infection as normally infected projectiles.")
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
        ZOMBIE_LIST = BUILDER.comment("The list of zombies that susceptible livings transform into upon death when the infection value exceeds the threshold." +
                "\nFormat: \"minecraft:zombie, 80\" (\"\" required) means that each dead has a weight of 80 to turn into a zombie.")
                .define("Zombie List", Arrays.asList("minecraft:zombie, 80", "minecraft:husk, 20"), Objects::nonNull);
        SPECIFIC_ZOMBIE_LIST = BUILDER.comment("""
                Which susceptible entities transform into specific mobs upon death when the infection value exceeds the threshold.
                Format: "minecraft:villager, minecraft:zombie_villager", meaning that a villager will turn into a zombie villager upon death when the infection value exceeds the threshold.
                This setting takes priority over the Zombie List.
                """).define("Specific Zombie List", new ArrayList<>());

        BUILDER.push("Infected Zone");
        INFECTED_ZONE_OPEN = BUILDER.comment("Whether to turn on the infected zone generation." +
                "\nThe Serverside controls the switching of the generation and the Clientside controls the display.")
                .define("Turn on Infected Zone Generation", true);
        INFECTED_ZONE_ZERO_DISTANCE = BUILDER.comment("Distance from spawn to chunk zero." +
                "\nWhen the level is generated, a chunk at this distance from the spawn point will be randomly selected as chunk zero.")
                .defineInRange("Zero Chunk Distance", 0, 0, 100000);
        INFECTED_ZONE_GENERATION = BUILDER.comment("""
                Infected Zone Generation Method:
                If the value is 1, a large low-level infected zone will be generated around Chunk Zero, and the average infection level will increase extremely slowly as the distance from Chunk Zero increases.
                If the value is 2, a large high-level infected zone will be generated around Chunk Zero, and the average infection level will decrease gradually as the distance from Chunk Zero increases.
                If the value is 0, the infected zones will be generated based on the expression defined in Infected Zone Custom Generation.
                """)
                .defineInRange("Infected Zone Generation", 1, 0, 2);
        INFECTED_ZONE_CUSTOM_GENERATION = BUILDER.comment("""
                Custom generation expression of infected zones.
                You can use +, -, *, /, and ^.
                Use 'T' to represent the in-game day and 'D' to represent the distance of the chunk from Chunk Zero (case-sensitive).
                """)
                .define("Infected Zone Custom Generation", "");
        INFECTED_ZONE_INCREASEMENT = BUILDER.comment("""
                Expression for chunk infection level change over time.
                The updated chunk infection value equals the value calculated from the expression plus the original infection value.
                Updates once per in-game day. Leave blank to disable this feature.
                You can use +, -, *, /, and ^.
                Use 'T' to represent number of in-game days between updates,
                'D' to represent the distance of the chunk from Chunk Zero,
                'O' to represent the origin level of chunk infection,
                'A' to represent the the average infection level of the surrounding eight chunks.
                (case-sensitive)
                """)
                .define("Infected Zone Custom Increasement", "");
        INFECTED_ZONE_STRUCTURE = BUILDER.comment("""
                Define the infection-zone level for specific structures.
                Format: "minecraft:village_plain, 10". This means the Plains Village will have an infection value of 10.
                Lost Cities Supported.
                """)
                .define("Infected Zone Structure", new ArrayList<>());
        INFECTED_ZONE_BLOCK = BUILDER.comment("""
                Define the infection-zone level for specific blocks.
                Format: "minecraft:water, 10". This means the water will have an infection value of 10.
                """)
                .define("Infected Zone Block", new ArrayList<>());
        INFECTED_ZONE_MOB = BUILDER.comment("""
                Which mobs are only allowed to spawn in infection zones within a specific infection value range.
                The mod will not actively spawn these mobs; it only restricts them from NATURAL spawning in where infection value is outside the specified range.
                Format: "minecraft:husk, 20, 60" means that husks will only spawn in where the infection value is greater than 20 and less than 60.
                """)
                .define("Infected Zone Mob", new ArrayList<>());

        BUILDER.push("Infected Zone Client Side Customize");
        INFECTED_ZONE_FOG_COLOR = BUILDER.comment("""
                The color of the infected zone fog.
                Format: 'R, G, B'.
                Leave blank for no fog.
                """).define("Infected Zone Fog Color", "198, 170, 113");
        INFECTED_ZONE_SKY_COLOR = BUILDER.comment("""
                The color of the infected zone sky.
                Format: 'R, G, B'.
                Leave blank for origin color.
                """).define("Infected Zone Sky Color", "198, 170, 113");
        INFECTED_ZONE_GRASS_COLOR = BUILDER.comment("""
                The color of the infected zone grass.
                Format: 'R, G, B'.
                Leave blank for origin color.
                """).define("Infected Zone Grass Color", "144, 129, 77");
        INFECTED_ZONE_WATER_COLOR = BUILDER.comment("""
                The color of the infected zone water.
                Format: 'R, G, B'.
                Leave blank for origin color.
                """).define("Infected Zone Water Color", "121, 72, 36");
        BUILDER.pop();
        BUILDER.pop();

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
        INJURY_WEIGHT = BUILDER.comment("This weight is multiplied for each injury and bandage decrease." +
                "\nFor example, if this value is set to 0.5, when the injury or bandage should decrease by 8, the actual decrease is 8 * 0.5 = 4.")
                .defineInRange("Injury Weight", 1.0, 0.0, 5.0);
        INJURY_INITIAL_VALUE = BUILDER.comment("When a player dies, if the injury value is lower than this value, the injury value will be equal to this value upon respawn.")
                .defineInRange("Injury Initial Value", 100.0, 0.0, 100.0);
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
                .define("DamageSource Blacklist", Arrays.asList("immunity", "injury", "infection", "genericKill", "drown", "starve", "magic", "wither", "dryOut", "freeze", "inWall", "outOfWorld"), Objects::nonNull);
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
        ROT_WEIGHT = BUILDER.comment("This weight is multiplied for each rot increase." +
                "\nFor example, if this value is set to 0.5, when the rot should increase by 8, the actual increase is 8 * 0.5 = 4.")
                .defineInRange("Rot Weight", 1.0, 0.0, 5.0);
        ROT_RESULT = BUILDER.comment("The default output of rotting; leave empty for no output.")
                .define("Rot Output", "");
        ROT_STACKABLE = BUILDER.comment("""
                Whether to the food can be stackable with different level of the rot.
                If turned on, food's level of rot will be averaged based on quantity when stacked.
                WARNING: This feature is not compatible with the backpack module for now.
                """)
                .define("Rot Stackable", true);
        ROT_EFFECT = BUILDER.comment("Whether to consume food that is not fresh may lead to nausea or poisonings.")
                .define("Food not Fresh Causes Nausea", true);
        ROT_TEMPERATURE = BUILDER.comment("Whether the temperature of the biome in which the entity or block is located and height affect the rate of rot.")
                .define("Temperature Affects Rot", true);
        ROT_WHITELIST = BUILDER.comment("Food that doesn't rot." +
                "\nFormat: \"minecraft:golden_apple\" (\"\" required)")
                .define("Food WhiteList", Arrays.asList("minecraft:golden_carrot", "minecraft:enchanted_golden_apple", "minecraft:golden_apple", "zombiekit:canned_beef_hotpot", "zombiekit:canned_yellow_peach", "zombiekit:canned_luncheon_meat", "zombiekit:canned_fish_in_black_bean_sauce", "zombiekit:canned_bread", "zombiekit:canned_beans", "zombiekit:canned_tomatoes"), Objects::nonNull);
        ROT_TEMPERATURE_WHITELIST = BUILDER.comment("Blocks in which food rot is not affected by temperature." +
                "\nFormat: \"minecraft:ender_chest\" (\"\" required)")
                .define("Block WhiteList", new ArrayList<>());
        ROT_DETAIL = BUILDER.comment("""
                Specific parameters of the rot.
                Format: "minecraft:porkchop, 1.5, minecraft:rotten_flesh" ("" required) means porkchop will rot at 1.5 times the rate and will end up as rotten flesh.
                By default, food rots for 5 game days, and rotten food will simply disappear.
                Tag supported.""")
                .define("Rot Parameters", Arrays.asList("#forge:cooked_meat, 1.0, minecraft:rotten_flesh", "#forge:raw_meat, 1.5, minecraft:rotten_flesh", "minecraft:spider_eye, 2.0, minecraft:air", "minecraft:rotten_flesh, 2.0, minecraft:air", "zombiekit:compressed_biscuit, 0.01, minecraft:air", "zombiekit:chocolate, 0.02, minecraft:air"), Objects::nonNull);
        ROT_WARPED = BUILDER.comment("Whether to allow players to warp food (The food warpped will rot slower).")
                .define("Wrap Food", true);
        WARPED_WEIGHT = BUILDER.comment("How much does the rate of rotting of wrapped food decrease.")
                .defineInRange("Warped Food Rot Rate", 0.5, 0, 1);
        WARPED_CHANCE = BUILDER.comment("Probability of food being wrapped in loot chests.")
                .defineInRange("Warped Food Chance", 0.5, 0, 1);
        WARPED_ITEMS = BUILDER.comment("The items can warp food when in offhand." +
                "Format: \"minecraft:apple\" (\"\" required)")
                .define("Warp Items", new ArrayList<>());
        ROT_CONTAINER = BUILDER.comment("Rate of food decay in different block containers." +
                "\nFormat: \"minecraft:chest, 0.8\" (\"\" required) means food in chests will rot at 0.8 times the rate.")
                .define("Block Container Detail", Arrays.asList("minecraft:ender_chest, 0"), Objects::nonNull);
        BUILDER.pop();

        BUILDER.push("Rust System");
        RUST_OPEN = BUILDER.comment("Whether to turn on the rust system." +
                "\nThe Serverside controls the switching of the system and the Clientside controls the display of the HUD.")
                .define("Turn on Rust System", true);
        RUST_WEIGHT = BUILDER.comment("This weight is multiplied for each rust increase." +
                "\nFor example, if this value is set to 0.5, when the rust should increase by 8, the actual increase is 8 * 0.5 = 4.")
                .defineInRange("Rust Weight", 1.0, 0.0, 5.0);
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
        DECOY_LIFE = BUILDER.comment("Increased seconds to attract mobs per level of sound.")
                .defineInRange("Increased Seconds", 5, 1, 100);

        BUILDER.push("Tacz & sbw");
        GUN_SOUND_ATTRACT = BUILDER.comment("Whether the gun or vehicle in tacz or sbw will attract mobs when fire")
                .define("Gun Fire Attraction", true);
        GUN_SOUND_WHITELIST = BUILDER.comment("""
                The gun id will not attract mobs.
                Format: "tacz:ai_awp" ("" required)
                """)
                .define("Gun WhiteList", new ArrayList<>());
        FIRE_EXPOSURE = BUILDER.comment("The range of mobs that will be attracted without silencer.\n" +
                "For example, if this value is set to 4, the mobs within 4 * (0.3 * FOLLOW_RANGE) would be attracted to fire sound.")
                .defineInRange("Fire Range", 4, 0, 10);
        SILENCE_EXPOSURE = BUILDER.comment("The range of mobs that will be attracted with silencer.\n" +
                "For example, if this value is set to 1, the mobs within 1 * (0.3 * FOLLOW_RANGE) would be attracted to fire sound with silencer.")
                .defineInRange("Silencer Range", 1, 0, 10);
        BUILDER.pop();

        BUILDER.pop();

        BUILDER.push("Chat System");
        CHAT_LIMIT = BUILDER.comment("Whether or not players would only chat with each other nearby." +
                "\nEffective for /tell commands and use the /say command to send messages regardless of distance.")
                .define("Chat Range Limit", false);
        CHAT_RANGE = BUILDER.comment("The range within which a player can chat, beyond which messages will be gradually distorted until they can't be received.")
                .defineInRange("Chat Initial Range", 60, 0, 10000);
        RANGE_ITEM = BUILDER.comment("""
                Items will increase players' chat range.
                Format: "minecraft:ender_eye, 600" ("" required) means that if the player has ender eye in inventory, this player will send message to player with increase chat range items within 600.
                If the sending distance is -1, it can send message to all players with increase chat range items in that dimension.
                If the sending distance is -2, it can send message to all players with increase chat range items in that server.
                And if the sending distance is 0, it can only receive message.
                Note: Only players with items increasing chat range can receive messages beyond initial range.
                """).define("Increase Chat Range Items", new ArrayList<>());
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
        ENHANCED_CAMOUFLAGE = BUILDER.comment("Whether or not to enable enhanced camouflage, when enabled, monsters will lose their target if there is a line of sight obstacle between them when tracking a camouflaged target.")
                .define("Enhanced Camouflage", false);
        EXPOSURE_INDICATOR = BUILDER.comment("Whether entities with Exposure and Sound Decoys render position indicators for players.")
                .define("Exposure Indicator", true);
        STUN_DAMAGE_MULTIPLIER = BUILDER.comment("The damage received by a stunned creature is increased by a certain multiplier of the original damage.")
                .defineInRange("Stun Damage Multiplier", 1.2, 1.0, 10.0);
        OVER_DOT = BUILDER.comment("Whether or not the dot damage from effect such as ignition can bypass the invulnerable time.")
                .define("Bypass Invulnerable Dot", true);

        LOCK_PERCENT = BUILDER.comment("Loot containers have a chance of being locked when they are generated.")
                .defineInRange("Loot Container Locked Percent", 20, 0D, 100D);
        LOCK_BREAKER = BUILDER.comment("""
                The Items can break the lock.
                Format: "zombiekit:crowbar, 30" ("" required) means that iron_crowbar has a 30% chance of opening locked chests every time right click locked container.
                """)
                .define("Unlock Items", Arrays.asList("zombiekit:crowbar, 30", "zombiekit:netherite_crowbar, 70"), Objects::nonNull);
        LOCK_BREAKER_COOLDOWN = BUILDER.comment("The cooldown ticks of use unlock items" +
                "\n1 sec = 20 ticks")
                .defineInRange("Unlock Items Cooldown", 160, 0, 1000);
        LOCK_WHITELIST_OPEN = BUILDER.comment("Whether only loot containers with the loot table in the whitelist will be locked.")
                .define("Lock Table Whitelist Mode Open", false);
        BLOCK_LOCK_BLACKLIST = BUILDER.comment("""
                The BlockEntity will NOT be locked.
                Format: "minecraft:chest"("" required)
                """)
                .define("BlockEntity Blacklist", Arrays.asList("dyairdrop:airdroplarge"), Objects::nonNull);
        LOCK_WHITELIST = BUILDER.comment("""
                The Loot Table will be locked.
                Format: "chests/end_city_treasure" or "zombiekit:chests/tool" ("" required)
                """)
                .define("Lock Table Whitelist", new ArrayList<>());

        UPDATE_LOG = BUILDER.comment("Whether to show the update log when the player logs in.")
                .define("Update Log in 130", true);
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
