package com.scarasol.sona.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.scarasol.sona.accessor.IGasMask;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.event.SonaEventHooks;
import com.scarasol.sona.init.SonaDamageTypes;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.init.SonaTags;
import com.scarasol.sona.network.MapVariables;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLeavesDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class InfectionManager {

    public static final Map<TreeConfiguration, TreeConfiguration> TREE_FEATURE = Maps.newHashMap();
    public static final Map<ColorType, Vec3> COLOR_MAP = Maps.newHashMap();
    public static final List<Tuple<ResourceLocation, Double>> ZOMBIES = Lists.newArrayList();
    public static double WEIGHT_SUM = -1;
    public static final Map<ResourceLocation, ResourceLocation> SPECIFIC_ZOMBIES = Maps.newHashMap();

    public static final AttachedToLeavesDecorator INFECTION_DECORATOR = new AttachedToLeavesDecorator(
            0.25F,  // 25% 概率
            0, 0,   // XZ 范围 2 格内不要重复挂饰，Y 范围 1 格内不要重复
            BlockStateProvider.simple(Blocks.IRON_BLOCK), // 挂灯笼
            1,      // 至少需要 1 格空气
            Direction.Plane.HORIZONTAL.stream().toList() // 只挂在叶子下面
    );

    public static float getInfection(ILivingEntityAccessor livingEntity) {
        return livingEntity.getInfectionLevel();
    }

    public static void setInfection(ILivingEntityAccessor livingEntity, float infection) {
        livingEntity.setInfectionLevel(infection);
    }

    public static void addInfection(ILivingEntityAccessor livingEntity, float addition) {
        if (addition > 0) {
            addition = addition * CommonConfig.INFECTION_WEIGHT.get().floatValue();
        }
        addActualInfection(livingEntity, addition);
    }

    public static void addActualInfection(ILivingEntityAccessor livingEntity, float addition) {
        float infection = addition > 0 ? Math.min(100, addition + getInfection(livingEntity)) : Math.max(0, addition + getInfection(livingEntity));
        setInfection(livingEntity, infection);
    }

    public static void init(ILivingEntityAccessor newPlayer, ILivingEntityAccessor oldPlayer) {
        newPlayer.setInfectionLevel(Math.min(oldPlayer.getInfectionLevel(), CommonConfig.INFECTION_INITIAL_VALUE.get().floatValue()));
    }

    public static boolean canBeInfected(LivingEntity entity) {
        return (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) || CommonConfig.SUSCEPTIBLE_POPULATION.get().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
    }

    public static double canInfect(Entity entity) {
        List<String> entityList;
        if (entity instanceof LivingEntity) {
            entityList = CommonConfig.INFECTION_SOURCE_MOB.get();
        } else {
            entityList = CommonConfig.INFECTION_SOURCE_PROJECTILE.get();
        }
        int index = CommonConfig.findIndex(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString(), entityList);
        if (index == -1) {
            if (entity instanceof Mob mob && mob.getMobType() == MobType.UNDEAD) {
                return 1;
            } else {
                return index;
            }
        } else {
            String[] info = entityList.get(index).split(",");
            if (info.length < 2) {
                return 1;
            }
            return Double.parseDouble(info[1].trim());
        }
    }

    public static void infectionTick(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        long gameTime = level.getGameTime() + livingEntity.getId();
        if (canBeInfected(livingEntity) && gameTime % 1600 == 0 && livingEntity instanceof ILivingEntityAccessor livingEntityAccessor) {
            switch (level.getDifficulty()) {
                case PEACEFUL:
                    addInfection(livingEntityAccessor, -5);
                    break;
                case EASY:
                    if (livingEntityAccessor.getInfectionLevel() < 40) {
                        addInfection(livingEntityAccessor, -2.5f);
                    }
                    break;
                case NORMAL:
                    if (livingEntityAccessor.getInfectionLevel() < 40) {
                        addInfection(livingEntityAccessor, -2.5f);
                    }
                    if (livingEntityAccessor.getInfectionLevel() > 70 && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get())) {
                        addInfection(livingEntityAccessor, 1);
                    }
                    break;
                case HARD:
                    if (livingEntityAccessor.getInfectionLevel() > 40 && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get())) {
                        addInfection(livingEntityAccessor, 1);
                    }
                    break;
            }
        }
        if (gameTime % 20 == 0) {
            if (canChunkInfection(level)) {
                infectionChunkTick(livingEntity);
            }
            infectionEffect(livingEntity);
        }

    }

    protected static void infectionEffect(LivingEntity livingEntity) {
        if (livingEntity.level().isClientSide()) {
            return;
        }
        if (livingEntity instanceof ILivingEntityAccessor livingEntityAccessor) {
            if (livingEntityAccessor.getInfectionLevel() >= 100) {
                livingEntity.hurt(SonaDamageTypes.damageSource(livingEntity.level().registryAccess(), SonaDamageTypes.INFECTION), 999999);
            } else if (livingEntityAccessor.getInfectionLevel() > 90) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 1, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 25, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 85, 0, false, false));
            } else if (livingEntityAccessor.getInfectionLevel() > 70) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 25, 0, false, false));
            } else if (livingEntityAccessor.getInfectionLevel() > 40) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 25, 0, false, false));
            }
            if (livingEntityAccessor.getInfectionLevel() > 70 && (livingEntity.level().getGameTime() + livingEntity.getId()) % 400 == 0 && livingEntity.getRandom().nextDouble() < 0.3) {

                livingEntity.level().playSound(null, livingEntity, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft:entity.zombie.ambient")), SoundSource.NEUTRAL, 1, 1);
            }
        }
    }

    public static void infectionChunkTick(LivingEntity livingEntity) {
        if (canInfect(livingEntity) > 0) {
            chunkInfectedEffect(livingEntity);
        } else if (canBeInfected(livingEntity)) {
            chunkSusceptibleEffect(livingEntity);
        }
    }

    protected static void chunkInfectedEffect(LivingEntity livingEntity) {
        Level level = livingEntity.level();

        int infection = getZoneInfection(level, livingEntity.blockPosition(), false);
        if (infection > 75) {
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.CAMOUFLAGE.get(), 25, 1, false, false));
            livingEntity.heal(0.5F);
        }
        if (infection > 50) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 0, false, false));
        }
        if (infection > 30) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 0, false, false));
        }
    }

    protected static void chunkSusceptibleEffect(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        long gameTime = level.getGameTime() + livingEntity.getId();
        if (gameTime % 20 == 0) {

            int infection = getZoneInfection(level, livingEntity.blockPosition(), false);
            if (infection > 75) {
                livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.EXPOSURE.get(), 25, 1, false, false));
                ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                boolean isGasMask = helmet.is(SonaTags.GAS_MASK) || (helmet.getItem() instanceof IGasMask gasMask && gasMask.isGasMask(livingEntity, helmet));
                if (!isGasMask && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get())) {
                    livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.INFECTION.get(), 25, 1, false, false));
                }
            } else if (infection > 50) {
                ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                boolean isGasMask = helmet.is(SonaTags.GAS_MASK) || (helmet.getItem() instanceof IGasMask gasMask && gasMask.isGasMask(livingEntity, helmet));
                if (!isGasMask && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get())) {
                    livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.INFECTION.get(), 25, 0, false, false));
                }
            }

        }
    }

    public static void turnZombie(LivingEntity livingEntity) {
        double weightSum = getWeightSum();
        double currentWeight = 0;
        double random = new Random().nextDouble();
        ResourceLocation livingEntityResourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(livingEntity.getType());
        ResourceLocation zombieToSpawn = SPECIFIC_ZOMBIES.get(livingEntityResourceLocation);
        if (zombieToSpawn == null) {
            if (weightSum == 0) {
                return;
            }
            for (Tuple<ResourceLocation, Double> tuple : ZOMBIES) {
                currentWeight += tuple.getB();
                if (random < currentWeight / weightSum) {
                    zombieToSpawn = tuple.getA();
                    break;
                }
            }
        }
        if (zombieToSpawn == null) {
            return;
        }
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(zombieToSpawn);
        if (entityType == null) {
            return;
        }
        Entity entityToSpawn = entityType.create(livingEntity.level());

        if (entityToSpawn == null) {
            return;
        }
        entityToSpawn.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        if (livingEntity.level() instanceof ServerLevel serverLevel && entityToSpawn instanceof Mob mob) {
            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entityToSpawn.blockPosition()), MobSpawnType.CONVERSION, null, null);
            mob.setPersistenceRequired();
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            entityToSpawn.setItemSlot(slot, livingEntity.getItemBySlot(slot));
            livingEntity.setItemSlot(slot, ItemStack.EMPTY);
            if (entityToSpawn instanceof Mob mob && livingEntity instanceof Player) {
                mob.setDropChance(slot, 1);
            }
        }

        livingEntity.level().addFreshEntity(entityToSpawn);
    }

    protected static double getWeightSum() {
        if (WEIGHT_SUM == -1) {
            initZombieList();
        }

        return WEIGHT_SUM;
    }

    protected static void initZombieList() {
        double sum = 0;
        for (String str : CommonConfig.ZOMBIE_LIST.get()) {
            String[] buffer = str.split(",");
            if (buffer.length < 2) {
                continue;
            }
            ResourceLocation resourceLocation = new ResourceLocation(buffer[0].trim());
            double weight = Double.parseDouble(buffer[1].trim());
            sum += weight;
            ZOMBIES.add(new Tuple<>(resourceLocation, weight));
        }
        WEIGHT_SUM = sum;
        for (String str : CommonConfig.SPECIFIC_ZOMBIE_LIST.get()) {
            String[] buffer = str.split(",");
            if (buffer.length < 2) {
                continue;
            }
            ResourceLocation resourceLocation1 = new ResourceLocation(buffer[0].trim());
            ResourceLocation resourceLocation2 = new ResourceLocation(buffer[1].trim());
            SPECIFIC_ZOMBIES.put(resourceLocation1, resourceLocation2);
        }
    }

    public static float onAttacked(LivingEntity target, Entity entity, float amount, DamageSource damageSource) {
        Level level = entity.level();
        if (level.isClientSide()) {
            return amount;
        }
        double weight = canInfect(entity);
        if (weight != -1) {
            if (target.hasEffect(SonaMobEffects.IMMUNITY.get()) && entity instanceof LivingEntity livingEntity) {
                immunityEffect(livingEntity, target.getEffect(SonaMobEffects.IMMUNITY.get()).getAmplifier());
            } else {
                if (canBeInfected(target)) {
                    infectionCalculate(target, weight);
                }
            }

        }
        if (target instanceof ILivingEntityAccessor livingEntityAccessor && livingEntityAccessor.isSona$carapace()) {
            if (damageSource.is(DamageTypeTags.IS_FIRE)) {
                livingEntityAccessor.setSona$carapace(false);
            }
            if (amount >= target.getHealth()) {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 4));
                livingEntityAccessor.setSona$carapace(false);
                return target.getHealth() - 1;
            }
        }
        return amount;
    }

    protected static void infectionCalculate(LivingEntity livingEntity, double weight) {
        AttributeInstance attribute = livingEntity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        double resistanceChance = attribute != null ? 0.03 * attribute.getValue() : 0;
        if (Math.random() >= resistanceChance && livingEntity instanceof ILivingEntityAccessor livingEntityAccessor) {
            addInfection(livingEntityAccessor, (float) (weight * (200 / (Math.pow(livingEntity.getArmorValue(), 2) + 50) + 1)));
        }
    }

    protected static void immunityEffect(LivingEntity livingEntity, int amplifier) {
        if (amplifier == 1) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 140, 1));
        } else if (amplifier == 2) {
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.FRAGILITY.get(), 140, 3));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2));
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.CONFUSION.get(), 140, 0));
        }
    }

    public static void infectedEntitySpawn(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        if (level.isClientSide()) {
            return;
        }
        if (canChunkInfection(level) && canInfect(livingEntity) > 0) {
            int infection = getZoneInfection(level, livingEntity.blockPosition(), false);
            if (livingEntity instanceof ILivingEntityAccessor livingEntityAccessor && infection > 75) {
                livingEntityAccessor.setSona$carapace(true);
            }
        }
    }

    public static void onUseItem(ILivingEntityAccessor livingEntity, ItemStack itemStack) {
        int index = Math.max(CommonConfig.tagSearch(itemStack, CommonConfig.INFECTION_SOURCE_ITEM.get()), CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), CommonConfig.INFECTION_SOURCE_ITEM.get()));
        if (index == -1) {
            return;
        }
        String[] str = CommonConfig.INFECTION_SOURCE_ITEM.get().get(index).split(",");
        if (str.length < 4) {
            return;
        }

        Random random = new Random();
        if (random.nextFloat() < Float.parseFloat(str[1].trim()) / 100) {
            float min = Math.min(Float.parseFloat(str[2].trim()), Float.parseFloat(str[3].trim()));
            float max = Math.max(Float.parseFloat(str[2].trim()), Float.parseFloat(str[3].trim()));
            if (min == max) {
                addInfection(livingEntity, min);
            } else {
                addInfection(livingEntity, random.nextFloat(min, max));
            }
        }
    }

    public static boolean blurMessage(float infectionLevel, MutableComponent component) {
        if (infectionLevel > 70) {
            component.withStyle(ChatFormatting.OBFUSCATED);
            return true;
        }
        return false;
    }

    public static boolean canChunkInfection(Level level) {
        return ChunkSectionInfectionManager.getInstance().canChunkInfection(level);
    }

    public static BlockPos calculateZeroZone(BlockPos pos) {
        return ChunkSectionInfectionManager.getInstance().calculateZeroZone(pos);
    }


    public static int initializeInfectionZone(WorldGenLevel level, BlockPos blockPos) {
        return ChunkSectionInfectionManager.getInstance().initializeInfectionZone(level, blockPos);
    }

    public static void calculateInfectionZone(ServerLevel level, ChunkPos chunkPos) {
        ChunkSectionInfectionManager.getInstance().calculateInfectionZone(level, chunkPos);
    }

    public static int getZoneInfection(Level level, BlockPos blockPos, boolean ignoreBlock) {
        return ChunkSectionInfectionManager.getInstance().getZoneInfection(level, blockPos, ignoreBlock);
    }


    public static List<TreeDecorator> addTreeDecorator(ServerLevel serverLevel, BlockPos blockPos) {
        return SonaEventHooks.addTreeDecorator(serverLevel, blockPos, InfectionManager.getZoneInfection(serverLevel, blockPos, true));
    }

    public static boolean canMobSpawn(ServerLevel level, Mob mob, BlockPos blockPos, MobSpawnType spawnType) {
        return ChunkSectionInfectionManager.getInstance().canMobSpawn(level, mob, blockPos, spawnType);
    }


    @OnlyIn(Dist.CLIENT)
    public static Vec3 getFogColor(BlockPos pos, Vec3 oldColor, float particleTick, ClientLevel level) {
        float f = level.getTimeOfDay(particleTick);

        float f1 = Mth.cos(f * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = Mth.clamp(f1, 0.0F, 1.0F);

        float skyLight = level.getBrightness(LightLayer.SKY, pos) / 15.0F;

        f1 = Math.min(skyLight, f1);

        float f2 = (float) oldColor.x * f1;
        float f3 = (float) oldColor.y * f1;
        float f4 = (float) oldColor.z * f1;
        float f5 = level.getRainLevel(particleTick);
        if (f5 > 0.0F) {
            float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
            float f7 = 1.0F - f5 * 0.75F;
            f2 = f2 * f7 + f6 * (1.0F - f7);
            f3 = f3 * f7 + f6 * (1.0F - f7);
            f4 = f4 * f7 + f6 * (1.0F - f7);
        }

        float f9 = level.getThunderLevel(particleTick);
        if (f9 > 0.0F) {
            float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
            float f8 = 1.0F - f9 * 0.75F;
            f2 = f2 * f8 + f10 * (1.0F - f8);
            f3 = f3 * f8 + f10 * (1.0F - f8);
            f4 = f4 * f8 + f10 * (1.0F - f8);
        }

        int i = level.getSkyFlashTime();
        if (i > 0) {
            float f11 = (float) i - particleTick;
            if (f11 > 1.0F) {
                f11 = 1.0F;
            }

            f11 *= 0.45F;
            f2 = f2 * (1.0F - f11) + 0.8F * f11;
            f3 = f3 * (1.0F - f11) + 0.8F * f11;
            f4 = f4 * (1.0F - f11) + f11;
        }

        return new Vec3(f2, f3, f4);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static Vec3 getColorByType(ColorType colorType) {
        if (COLOR_MAP.isEmpty()) {
            COLOR_MAP.put(ColorType.FOG, parseColor(CommonConfig.INFECTED_ZONE_FOG_COLOR.get()));
            COLOR_MAP.put(ColorType.SKY, parseColor(CommonConfig.INFECTED_ZONE_SKY_COLOR.get()));
            COLOR_MAP.put(ColorType.GRASS, parseColor(CommonConfig.INFECTED_ZONE_GRASS_COLOR.get()));
            COLOR_MAP.put(ColorType.WATER, parseColor(CommonConfig.INFECTED_ZONE_WATER_COLOR.get()));
        }
        return COLOR_MAP.get(colorType);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static Vec3 parseColor(String color) {
        String[] rgb = color.split(",");
        if (rgb.length < 3) {
            return null;
        }
        return new Vec3(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim()));
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static Vec3 getInfectionChunkFogColor(Vec3 oldColor, Vec3 pos, ClientLevel level) {
        Vec3 newColor = getColorByType(ColorType.FOG);
        if (newColor == null) {
            return null;
        }
        return getInfectionChunkColor(oldColor, pos, level, getFogColor(BlockPos.containing(pos), new Vec3(newColor.x / 255D, newColor.y / 255D, newColor.z / 255D), 0, level));
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static Vec3 getInfectionChunkSkyColor(Vec3 oldColor, Vec3 pos, Level level) {
        Vec3 newColor = getColorByType(ColorType.SKY);
        if (newColor == null) {
            return null;
        }
        return getInfectionChunkColor(oldColor, pos, level, new Vec3(newColor.x / 255D, newColor.y / 255D, newColor.z / 255D));
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static Vec3 getInfectionChunkGrassColor(Vec3 oldColor, Vec3 pos, Level level) {
        Vec3 newColor = getColorByType(ColorType.GRASS);
        if (newColor == null) {
            return null;
        }
        return getInfectionChunkColor(oldColor, pos, level, newColor);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static Vec3 getInfectionChunkWaterColor(Vec3 oldColor, Vec3 pos, Level level) {
        Vec3 newColor = getColorByType(ColorType.WATER);
        if (newColor == null) {
            return null;
        }
        return getInfectionChunkColor(oldColor, pos, level, newColor);
    }


    @OnlyIn(Dist.CLIENT)
    public static Vec3 getInfectionChunkColor(Vec3 oldColor, Vec3 pos, Level level, Vec3 newColor) {
        return ChunkSectionInfectionManager.getInstance().getInfectionZoneColor(oldColor, pos, level, newColor);
    }

    @OnlyIn(Dist.CLIENT)
    public static double getAveZoneInfectionInRender(Level level, Vec3 position) {
        return ChunkSectionInfectionManager.getInstance().getAveZoneInfectionInRender(level, position);
    }

    enum ColorType {
        FOG,
        SKY,
        GRASS,
        WATER
    }
}
