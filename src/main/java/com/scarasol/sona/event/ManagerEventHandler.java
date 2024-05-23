package com.scarasol.sona.event;

import com.scarasol.sona.accessor.IBaseContainerBlockEntityAccessor;
import com.scarasol.sona.command.InfectionCommand;
import com.scarasol.sona.command.InjuryCommand;
import com.scarasol.sona.command.RotCommand;
import com.scarasol.sona.command.RustCommand;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.accessor.ICompoundContainerAccessor;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.manager.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class ManagerEventHandler {

    @SubscribeEvent
    public static void onAttacked(LivingDamageEvent event) {
        LivingEntity target = event.getEntityLiving();
        Entity entity = event.getSource().getDirectEntity();
        if (target == null || target.getLevel().isClientSide())
            return;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator()))
            return;
        if (CommonConfig.INFECTION_OPEN.get() && entity != null)
            InfectionManager.onAttacked(target, entity);
        if (CommonConfig.INJURY_OPEN.get())
            InjuryManager.onAttacked(target, event.getSource(), event.getAmount());
        if (CommonConfig.RUST_OPEN.get())
            RustManager.onAttacked(target);
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (livingEntity == null || livingEntity.getLevel().isClientSide())
            return;
        if (livingEntity instanceof Player player && (player.isCreative() || player.isSpectator()))
            return;
        ItemStack itemStack = event.getItem();
        if (livingEntity instanceof ILivingEntityAccessor survivalEntity) {
            if (CommonConfig.INFECTION_OPEN.get())
                InfectionManager.onUseItem(survivalEntity, itemStack);
            if (CommonConfig.INJURY_OPEN.get())
                InjuryManager.onUseItem(survivalEntity, itemStack);
        }
        if (CommonConfig.ROT_OPEN.get() && CommonConfig.ROT_EFFECT.get() && itemStack.isEdible() && RotManager.canBeRotten(itemStack)){
            RotManager.eatRotFood(livingEntity, itemStack);
        }

    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (livingEntity == null || livingEntity.getLevel().isClientSide())
            return;
        if (livingEntity instanceof Player && !livingEntity.getLevel().isClientSide()) {
            if (livingEntity instanceof ILivingEntityAccessor survivalEntity) {
                InfectionManager.init(survivalEntity);
                InjuryManager.init(survivalEntity);
            }
        }
        if (livingEntity instanceof Villager && event.getSource().getDirectEntity() instanceof Zombie)
            return;
        if (CommonConfig.INFECTION_OPEN.get() && CommonConfig.TURN_ZOMBIE.get()) {
            if (livingEntity instanceof Player || CommonConfig.SUSCEPTIBLE_POPULATION.get().contains(ForgeRegistries.ENTITIES.getKey(livingEntity.getType()).toString())) {
                if (livingEntity instanceof ILivingEntityAccessor survivalEntity && InfectionManager.getInfection(survivalEntity) > CommonConfig.INFECTION_THRESHOLD.get()) {
                    InfectionManager.turnZombie(livingEntity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getLevel().isClientSide())
            return;
        if (CommonConfig.INJURY_OPEN.get() && CommonConfig.HEAL_WHILE_SLEEP.get() && !event.updateWorld() && player instanceof ILivingEntityAccessor survivalEntity) {
            InjuryManager.healBySleep(survivalEntity);
        }
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        Player player = event.getPlayer();
        if (CommonConfig.INFECTION_OPEN.get() && CommonConfig.BLUR_MESSAGE.get() && player instanceof ILivingEntityAccessor survivalEntity) {
            event.setComponent(new TextComponent(InfectionManager.blurMessage(survivalEntity, event.getMessage())));
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        InfectionCommand.registerCommand(event.getDispatcher());
        InjuryCommand.registerInjuryCommand(event.getDispatcher());
        InjuryCommand.registerBandageCommand(event.getDispatcher());
        RotCommand.registerCommand(event.getDispatcher());
        RustCommand.registerCommand(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onOpen(PlayerContainerEvent.Open event) {
        AbstractContainerMenu abstractContainerMenu = event.getContainer();
        double containerMultiplier = 1;
        double temperature = event.getPlayer().getLevel().getBiome(event.getPlayer().getOnPos()).value().getBaseTemperature() / 2 + 0.6;
        Container container = abstractContainerMenu.slots.get(0).container;
        if (container instanceof ICompoundContainerAccessor compoundContainer) {
            container = compoundContainer.getContainer1();
        }
        if (container instanceof BlockEntity blockEntity) {
            containerMultiplier = RotManager.getContainerMultiplier(blockEntity.getType().getRegistryName().toString());
            if (CommonConfig.findIndex(blockEntity.getType().getRegistryName().toString(), CommonConfig.ROT_TEMPERATURE_WHITELIST.get()) == -1){
                temperature = blockEntity.getLevel().getBiome(blockEntity.getBlockPos()).value().getBaseTemperature() / 2 + 0.6;
            }else {
                temperature = 1;
            }
        } else if (container instanceof PlayerEnderChestContainer) {
            containerMultiplier = RotManager.getContainerMultiplier("minecraft:ender_chest");
            temperature = 1;
        }
        if (containerMultiplier != 0 && event.getPlayer() instanceof ServerPlayer serverPlayer) {
            RotManager.rotInContainer(serverPlayer, abstractContainerMenu.slots, abstractContainerMenu.slots.size() - event.getPlayer().getInventory().items.size(), containerMultiplier, event.getPlayer().getLevel().getGameTime(), temperature);
        }
    }

    @SubscribeEvent
    public static void onClose(PlayerContainerEvent.Close event) {
        AbstractContainerMenu abstractContainerMenu = event.getContainer();
        RotManager.rotTimeUpdate(abstractContainerMenu.slots, abstractContainerMenu.slots.size() - event.getPlayer().getInventory().items.size(), event.getPlayer().getLevel().getGameTime());
    }

    @SubscribeEvent
    public static void tooltipInsert(ItemTooltipEvent itemTooltipEvent){
        ItemStack itemStack = itemTooltipEvent.getItemStack();
        if (itemStack.isEdible() && CommonConfig.ROT_OPEN.get() && RotManager.canBeRotten(itemStack))
            RotManager.tooltipInsert(itemTooltipEvent.getToolTip(), itemStack);
        if (CommonConfig.RUST_OPEN.get() && RustManager.canBeRust(itemStack))
            RustManager.tooltipInsert(itemTooltipEvent.getToolTip(), itemStack);
    }

    @SubscribeEvent
    public static void rustAttributeModifierEvent(ItemAttributeModifierEvent event){
        RustManager.addRustAttributeModifier(event);
    }

    @SubscribeEvent
    public static void rustFunction(PlayerInteractEvent.RightClickItem event){
        Player player = event.getPlayer();
        if (!player.isShiftKeyDown() || !CommonConfig.RUST_OPEN.get() || player.getOffhandItem().isEmpty() || player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()))
            return;
        if (RustManager.wax(player.getMainHandItem(), player.getOffhandItem(), player) || RustManager.removalRust(player.getMainHandItem(), player.getOffhandItem(), player))
            player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 100);
    }

    @SubscribeEvent
    public static void soundDecoyTarget(EntityJoinWorldEvent event){
        if (event.getEntity() instanceof Mob mob && SoundManager.isSoundOpen())
            SoundManager.insertAi(mob);
    }

    @SubscribeEvent
    public static void syncSoundList(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getPlayer() instanceof ServerPlayer serverPlayer){
            SoundManager.syncSoundWhiteList(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void loadSoundWhiteList(ServerStartedEvent event){
        SoundManager.setSoundOpen(CommonConfig.SOUND_OPEN.get());
        for (String sound : CommonConfig.SOUND_WHITELIST.get())
            SoundManager.addSoundWhiteList(sound);
    }

    @SubscribeEvent
    public static void breakLock(PlayerInteractEvent.RightClickBlock event){
        Player player = event.getPlayer();
        Level level = event.getPlayer().getLevel();
        if (level.isClientSide())
            return;
        BlockPos blockPos = event.getHitVec().getBlockPos();
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack itemStack = player.getMainHandItem();
        if (blockEntity instanceof IBaseContainerBlockEntityAccessor baseContainerBlockEntityAccessor){
            if (!baseContainerBlockEntityAccessor.isLocked(player)){
                return;
            }
            int index = CommonConfig.findIndex(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), CommonConfig.LOCK_BREAKER.get());
            if (index != -1 && !player.getCooldowns().isOnCooldown(itemStack.getItem())){
                String[] str = CommonConfig.LOCK_BREAKER.get().get(index).split(",");
                if (str.length < 2)
                    return;
                if (player.getRandom().nextDouble() * 100 < Integer.parseInt(str[1].trim())){
                    baseContainerBlockEntityAccessor.breakLockKey();
                }else {
                    event.setCanceled(true);
                }
                if (itemStack.isDamageableItem()){
                    itemStack.hurtAndBreak(5, player, consumer -> consumer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                }else {
                    itemStack.shrink(1);
                }
                level.levelEvent(2001, event.getHitVec().getBlockPos(), Block.getId(blockState));
                level.playSound(null, event.getPlayer(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("survivalkit:crate")), SoundSource.PLAYERS, 1, 1);
                player.getCooldowns().addCooldown(itemStack.getItem(), 200);
            }else if (blockEntity instanceof BaseContainerBlockEntity baseContainerBlockEntity){
                event.setCanceled(true);
                player.displayClientMessage(new TranslatableComponent("container.isLocked", baseContainerBlockEntity.getName()), true);
                player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

}
