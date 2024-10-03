package com.scarasol.sona.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RotCommand {
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rot").requires(s -> s.hasPermission(2)).then(Commands.literal("get").executes(arguments -> {
            commandProcess(arguments, 0);
            return 0;
        })).then(Commands.literal("set").then(Commands.argument("number", DoubleArgumentType.doubleArg(0, 100)).executes(arguments -> {
            commandProcess(arguments, 1);
            return 0;
        }))).then(Commands.literal("add").then(Commands.argument("number", DoubleArgumentType.doubleArg(-100, 100)).executes(arguments -> {
            commandProcess(arguments, 2);
            return 0;
        }))));
    }

    private static void commandProcess(CommandContext<CommandSourceStack> arguments, int process) {
        Entity entity = arguments.getSource().getEntity();
        if (entity instanceof Player player){
            if (CommonConfig.ROT_OPEN.get()){
                if (player.getMainHandItem().isEdible() && RotManager.canBeRotten(player.getMainHandItem())){
                    if (process == 0){
                        getCommand(player, player.getMainHandItem());
                    }else{
                        double rotLevel = DoubleArgumentType.getDouble(arguments, "number");
                        if (process == 1){
                            setCommand(player, player.getMainHandItem(), rotLevel);
                        }else {
                            addCommand(player, player.getMainHandItem(), rotLevel);
                        }
                    }
                }else {
                    player.displayClientMessage(Component.literal(Component.translatable("command.sona.not_rot").getString()), false);
                }
            }else {
                player.displayClientMessage(Component.literal(Component.translatable("command.sona.rot.close").getString()), false);
            }

        }
    }

    private static void addCommand(Player player, ItemStack itemStack, double rotLevel) {
        RotManager.addRot(itemStack, rotLevel);
        if (player instanceof ServerPlayer serverPlayer)
            RotManager.syncRotValue(RotManager.getRot(itemStack), player.getInventory().selected, true, serverPlayer);
        player.displayClientMessage(Component.literal(Component.translatable("command.sona.modify_rot.success", itemStack.getDisplayName(), RotManager.getRot(itemStack)).getString()), false);
    }

    private static void setCommand(Player player, ItemStack itemStack, double rotLevel) {
        RotManager.putRot(itemStack, rotLevel);
        if (player instanceof ServerPlayer serverPlayer)
            RotManager.syncRotValue(RotManager.getRot(itemStack), player.getInventory().selected, true, serverPlayer);
        player.displayClientMessage(Component.literal(Component.translatable("command.sona.modify_rot.success", itemStack.getDisplayName(), rotLevel).getString()), false);
    }

    private static void getCommand(Player player, ItemStack itemStack) {
        String rotLevel = String.format("%.2f", RotManager.getRot(itemStack));
        player.displayClientMessage(Component.literal(Component.translatable("command.sona.get_rot.success", itemStack.getDisplayName(), rotLevel).getString()), false);
    }


}
