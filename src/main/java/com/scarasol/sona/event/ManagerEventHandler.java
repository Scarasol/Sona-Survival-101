package com.scarasol.sona.event;

import com.google.common.collect.Lists;
import com.scarasol.sona.accessor.mixin.IBaseContainerBlockEntityAccessor;
import com.scarasol.sona.accessor.mixin.IChunkAccessor;
import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.accessor.mixin.IServerLevelAccessor;
import com.scarasol.sona.client.renderer.PositionIndicatorManager;
import com.scarasol.sona.command.InfectionCommand;
import com.scarasol.sona.command.InjuryCommand;
import com.scarasol.sona.command.RotCommand;
import com.scarasol.sona.command.SonaCommand;
import com.scarasol.sona.command.RustCommand;
import com.scarasol.sona.compat.lostcities.LostCitiesCompat;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.init.SonaSounds;
import com.scarasol.sona.manager.*;
import com.scarasol.sona.network.MapVariables;
import com.scarasol.sona.network.NetworkHandler;
import com.scarasol.sona.network.SavedDataSyncPacket;
import com.scarasol.sona.util.JsonHandler;
import com.scarasol.sona.util.SonaStructureFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import static com.scarasol.sona.accessor.mixin.IChunkAccessor.COMPOUND_TAG_NAME;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber
public class ManagerEventHandler {
    @SubscribeEvent
    public static void onAttacked(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity entity = event.getSource().getDirectEntity();
        if (target == null || target.level().isClientSide()) {
            return;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        if (CommonConfig.INFECTION_OPEN.get() && entity != null) {
            event.setAmount(InfectionManager.onAttacked(target, entity, event.getAmount(), event.getSource()));
        }
        if (CommonConfig.INJURY_OPEN.get()) {
            InjuryManager.onAttacked(target, event.getSource(), event.getAmount());
        }
        if (CommonConfig.RUST_OPEN.get()) {
            RustManager.onAttacked(target);
        }
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity == null || livingEntity.level().isClientSide()) {
            return;
        }
        if (livingEntity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (livingEntity instanceof ILivingEntityAccessor survivalEntity) {
            if (CommonConfig.INFECTION_OPEN.get()) {
                InfectionManager.onUseItem(survivalEntity, itemStack);
            }
            if (CommonConfig.INJURY_OPEN.get()) {
                InjuryManager.onUseItem(survivalEntity, itemStack);
            }
        }
        if (CommonConfig.ROT_OPEN.get() && CommonConfig.ROT_EFFECT.get() && itemStack.isEdible() && RotManager.canBeRotten(itemStack)) {
            RotManager.eatRotFood(livingEntity, itemStack);
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity == null || livingEntity.level().isClientSide()) {
            return;
        }
        if (livingEntity instanceof Player player && !livingEntity.level().isClientSide()) {
            DeathManager.KeepItem(player);
        }
        if (livingEntity instanceof Villager && event.getSource().getDirectEntity() instanceof Zombie) {
            return;
        }
        if (CommonConfig.INFECTION_OPEN.get() && CommonConfig.TURN_ZOMBIE.get()) {
            if (InfectionManager.canBeInfected(livingEntity)) {
                if (livingEntity instanceof ILivingEntityAccessor survivalEntity && InfectionManager.getInfection(survivalEntity) > CommonConfig.INFECTION_THRESHOLD.get()) {
                    InfectionManager.turnZombie(livingEntity);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        DeathManager.respawnItem(player);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            if (event.getOriginal() instanceof ILivingEntityAccessor oldPlayer && event.getEntity() instanceof ILivingEntityAccessor newPlayer) {
                InfectionManager.init(newPlayer, oldPlayer);
                InjuryManager.init(newPlayer, oldPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        if (CommonConfig.INJURY_OPEN.get() && CommonConfig.HEAL_WHILE_SLEEP.get() && !event.updateLevel() && player instanceof ILivingEntityAccessor survivalEntity) {
            InjuryManager.healBySleep(survivalEntity);
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        SonaCommand.registerCommand(event.getDispatcher());
        InfectionCommand.registerCommand(event.getDispatcher());
        InjuryCommand.registerInjuryCommand(event.getDispatcher());
        InjuryCommand.registerBandageCommand(event.getDispatcher());
        RotCommand.registerCommand(event.getDispatcher());
        RustCommand.registerCommand(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onOpen(PlayerContainerEvent.Open event) {
        if (!CommonConfig.ROT_OPEN.get()) {
            return;
        }
        AbstractContainerMenu abstractContainerMenu = event.getContainer();
        if (abstractContainerMenu.slots.isEmpty()) {
            return;
        }
        Player entity = event.getEntity();


        RotManager.containerOpen(entity, abstractContainerMenu);
    }

    @SubscribeEvent
    public static void onClose(PlayerContainerEvent.Close event) {
        if (!CommonConfig.ROT_OPEN.get()) {
            return;
        }
        AbstractContainerMenu abstractContainerMenu = event.getContainer();
        RotManager.rotTimeUpdate(abstractContainerMenu.slots, abstractContainerMenu.slots.size() - event.getEntity().getInventory().items.size(), event.getEntity().level().getGameTime());
    }

    @SubscribeEvent
    public static void tooltipInsert(ItemTooltipEvent itemTooltipEvent) {
        ItemStack itemStack = itemTooltipEvent.getItemStack();
        if (itemStack.isEdible() && CommonConfig.ROT_OPEN.get() && RotManager.canBeRotten(itemStack)) {
            RotManager.tooltipInsert(itemTooltipEvent.getToolTip(), itemStack);
        }
        if (CommonConfig.RUST_OPEN.get() && RustManager.canBeRust(itemStack)) {
            RustManager.tooltipInsert(itemTooltipEvent.getToolTip(), itemStack);
        }
    }

    @SubscribeEvent
    public static void rustAttributeModifierEvent(ItemAttributeModifierEvent event) {
        RustManager.addRustAttributeModifier(event);
    }

    @SubscribeEvent
    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offhandItem = player.getOffhandItem();
        if (!player.isShiftKeyDown() || offhandItem.isEmpty() || player.getCooldowns().isOnCooldown(mainHandItem.getItem()) || player.getCooldowns().isOnCooldown(offhandItem.getItem())) {
            return;
        }
        if (mainHandItem.isEdible() && CommonConfig.ROT_OPEN.get() && CommonConfig.ROT_WARPED.get() && !RotManager.isWarped(mainHandItem) && RotManager.warpFood(player.getMainHandItem(), player.getOffhandItem(), player)) {
            player.getCooldowns().addCooldown(player.getOffhandItem().getItem(), 20);
            return;
        }
        if (CommonConfig.RUST_OPEN.get() && (RustManager.wax(mainHandItem, offhandItem, player) || RustManager.removalRust(mainHandItem, offhandItem, player))) {
            player.getCooldowns().addCooldown(mainHandItem.getItem(), 100);
        }


    }

    @SubscribeEvent
    public static void soundDecoyTarget(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob && SoundManager.isSoundOpen()) {
            SoundManager.insertAi(mob);
        }
        if (CommonConfig.INFECTION_OPEN.get() && entity instanceof ILivingEntityAccessor livingEntity && !event.loadedFromDisk()) {
            livingEntity.setNeedInit(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void syncData(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (CommonConfig.UPDATE_LOG.get()) {
                for (int i = 1; i <= 8; i++) {
                    serverPlayer.sendSystemMessage(Component.translatable("msg.sona.update_log_" + i));
                }
            }
            SoundManager.syncSoundWhiteList(serverPlayer);
            ChatManager.syncChatManager(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void loadData(ServerStartedEvent event) {
        SoundManager.setSoundOpen(CommonConfig.SOUND_OPEN.get());
        for (String sound : CommonConfig.SOUND_WHITELIST.get()) {
            SoundManager.addSoundWhiteList(sound);
        }
        ChatManager.setChatLimit(CommonConfig.CHAT_LIMIT.get());
        ChatManager.setChatRange(CommonConfig.CHAT_RANGE.get());
        for (String item : CommonConfig.RANGE_ITEM.get()) {
            ChatManager.addRangeItem(item);
        }
    }

    @SubscribeEvent
    public static void breakLock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        if (level.isClientSide()) {
            return;
        }
        BlockPos blockPos = event.getHitVec().getBlockPos();
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack itemStack = player.getMainHandItem();
        if (blockEntity instanceof IBaseContainerBlockEntityAccessor baseContainerBlockEntityAccessor) {
            if (!baseContainerBlockEntityAccessor.isLocked(player)) {
                return;
            }
            int index = CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), CommonConfig.LOCK_BREAKER.get());
            if (index != -1 && !player.getCooldowns().isOnCooldown(itemStack.getItem())) {
                String[] str = CommonConfig.LOCK_BREAKER.get().get(index).split(",");
                if (str.length < 2) {
                    return;
                }
                if (player.getRandom().nextDouble() * 100 < Integer.parseInt(str[1].trim())) {
                    baseContainerBlockEntityAccessor.breakLockKey();
                } else {
                    event.setCanceled(true);
                }
                if (itemStack.isDamageableItem()) {
                    itemStack.hurtAndBreak(5, player, consumer -> consumer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                } else {
                    itemStack.shrink(1);
                }
                level.levelEvent(2001, event.getHitVec().getBlockPos(), Block.getId(blockState));
                level.playSound(null, event.getEntity(), SonaSounds.CRATE.get(), SoundSource.PLAYERS, 1, 1);
                player.getCooldowns().addCooldown(itemStack.getItem(), CommonConfig.LOCK_BREAKER_COOLDOWN.get());
            } else if (blockEntity instanceof BaseContainerBlockEntity baseContainerBlockEntity) {
                event.setCanceled(true);
                player.displayClientMessage(Component.translatable("container.isLocked", baseContainerBlockEntity.getName()), true);
                player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void blurMessage(ClientChatReceivedEvent event) {
        Player player = Minecraft.getInstance().player;
        if (event.getBoundChatType().chatType().equals(player.level().registryAccess().registryOrThrow(Registries.CHAT_TYPE).getOrThrow(ChatType.SAY_COMMAND))) {
            return;
        }
        Component component = event.getMessage();
        String[] info = ChatManager.parseMessage(component.getString());
        if (info != null) {
            MutableComponent mutableComponent = Component.literal(component.getString().split(" @TheMessageCutter@ ")[0]).setStyle(component.getStyle());
            int range = Integer.parseInt(info[2]);
            String[] posInfo = info[1].split(", ");
            if (posInfo.length == 3) {
                double distance = Math.pow(player.distanceToSqr(Double.parseDouble(posInfo[0]), Double.parseDouble(posInfo[1]), Double.parseDouble(posInfo[2])), 0.5);
                if (ChatManager.isChatLimit() && distance > ChatManager.getChatRange() * 1.5 && !ChatManager.canReceiveBeyondMessage(player) && event.isCancelable()) {
                    event.setCanceled(true);
                    return;
                }
                if (!InfectionManager.blurMessage(Float.parseFloat(info[0]), mutableComponent) && ChatManager.isChatLimit() && !Minecraft.getInstance().player.getUUID().equals(event.getSender())) {
                    mutableComponent = ChatManager.lostMessage(mutableComponent, distance, range);
                }

            }
            event.setMessage(mutableComponent);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && InfectionManager.canChunkInfection(serverLevel)) {
            ChunkPos chunkPos = event.getChunk().getPos();
            IServerLevelAccessor.fromServerLevel(serverLevel).getSonaLoadedChunk().offer(chunkPos);
        }
    }

    @SubscribeEvent
    public static void onChunkUnLoad(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && InfectionManager.canChunkInfection(serverLevel)) {
            ChunkPos chunkPos = event.getChunk().getPos();
            IServerLevelAccessor.fromServerLevel(serverLevel).getSonaLoadedChunk().remove(chunkPos);
        }
    }

    @SubscribeEvent
    public static void onChunkWrite(ChunkDataEvent.Save event) {
        IChunkAccessor.fromLevelChunk(event.getChunk()).saveChunkData(event.getData());
    }

    @SubscribeEvent
    public static void onChunkRead(ChunkDataEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel) {
            IChunkAccessor.fromLevelChunk(event.getChunk()).loadChunkData(event.getData());
        }
    }

    @SubscribeEvent
    public static void chunkWatch(ChunkWatchEvent.Watch event) {
        IChunkAccessor.fromLevelChunk(event.getChunk()).syncChunkData(event.getPlayer(), event.getPos());
    }

//    @SubscribeEvent
//    public static void cropGrow(BlockEvent.CropGrowEvent.Pre event) {
//        if (event.getLevel() instanceof ServerLevel serverLevel && InfectionManager.canChunkInfection(serverLevel)) {
//            int chunkInfectionLevel = InfectionManager.getFullChunkInfection(serverLevel, new ChunkPos(event.getPos()));
//            if (serverLevel.random.nextInt(30, 50) - chunkInfectionLevel <= 0) {
//                event.setResult(Event.Result.DENY);
//            }
//        }
//    }

//    @SubscribeEvent
//    public static void treeGrow(SaplingGrowTreeEvent event) {
//        if (event.getLevel() instanceof ServerLevel serverLevel && InfectionManager.canChunkInfection(serverLevel)) {
////            if (serverLevel.random.nextInt(30, 50) - InfectionManager.getFullChunkInfection(serverLevel, new ChunkPos(event.getPos())) <= 0) {
////                event.setResult(Event.Result.DENY);
////                return;
////            }
//            Holder<ConfiguredFeature<?, ?>> original = event.getFeature();
//            if (original == null) {
//                return;
//            }
//            ConfiguredFeature<?, ?> cf = original.value();
//            Holder<ConfiguredFeature<?, ?>> holder = Holder.direct(InfectionManager.replaceTreeFeature(cf, serverLevel, event.getPos()));
//            event.setFeature(holder);
//        }
//    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(EntityJoinLevelEvent event) {
        Level level = event.getEntity().level();
        if (!level.isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            SavedData mapData = MapVariables.get(level);
            if (mapData != null) {
                NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SavedDataSyncPacket(mapData));
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PositionIndicatorManager.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            PositionIndicatorManager.clear();
        }
    }

    @SubscribeEvent
    public static void onLevelSpawn(LevelEvent.CreateSpawnPosition event) {
        MapVariables mapData = MapVariables.get(event.getLevel());
        if (mapData.getZeroChunk() == null) {
            ServerLevelData serverLevelData = event.getSettings();
            mapData.setZeroChunk(InfectionManager.calculateZeroZone(new BlockPos(serverLevelData.getXSpawn(), serverLevelData.getYSpawn(), serverLevelData.getZSpawn())));
        }
    }

    @SubscribeEvent
    public static void infectionZoneMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!InfectionManager.canMobSpawn(event.getLevel().getLevel(), event.getEntity(), BlockPos.containing(event.getX(), event.getY(), event.getZ()), event.getSpawnType())) {
            event.setSpawnCancelled(true);
        }
    }

//    @SubscribeEvent(priority = EventPriority.LOW)
//    public static void broadcastMessage(ServerChatEvent event){
//        Player player = event.getPlayer();
//        Component component = eve nt.getMessage();
//        ChatManager.broadcastMessage(player, component);
//        if (event.isCancelable())
//            event.setCanceled(true);
//    }




}
