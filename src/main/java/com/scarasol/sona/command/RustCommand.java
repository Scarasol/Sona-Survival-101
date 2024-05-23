package com.scarasol.sona.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RustManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RustCommand {
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rust").requires(s -> s.hasPermission(2)).then(Commands.literal("get").executes(arguments -> {
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
            if (CommonConfig.RUST_OPEN.get()){
                if (RustManager.canBeRust(player.getMainHandItem())){
                    if (process == 0){
                        getCommand(player, player.getMainHandItem());
                    }else{
                        double rustLevel = DoubleArgumentType.getDouble(arguments, "number");
                        if (process == 1){
                            setCommand(player, player.getMainHandItem(), rustLevel);
                        }else {
                            addCommand(player, player.getMainHandItem(), rustLevel);
                        }
                    }
                }else {
                    player.displayClientMessage(new TranslatableComponent("command.sona.not_rust"), false);
                }
            }else {
                player.displayClientMessage(new TranslatableComponent("command.sona.rust.close"), false);
            }

        }
    }

    private static void addCommand(Player player, ItemStack itemStack, double rustLevel) {
        RustManager.addRust(itemStack, rustLevel);
//        RustManager.syncRustValue(RustManager.getRust(itemStack), player.getInventory().selected, true);
        player.displayClientMessage(new TranslatableComponent("command.sona.modify_rust.success", itemStack.getDisplayName(), RustManager.getRust(itemStack)), false);
    }

    private static void setCommand(Player player, ItemStack itemStack, double rustLevel) {
        RustManager.putRust(itemStack, rustLevel);
//        RustManager.syncRustValue(RustManager.getRust(itemStack), player.getInventory().selected, true);
        player.displayClientMessage(new TranslatableComponent("command.sona.modify_rust.success", itemStack.getDisplayName(), rustLevel), false);
    }

    private static void getCommand(Player player, ItemStack itemStack) {
        String rustLevel = String.format("%.2f", RustManager.getRust(itemStack));
        player.displayClientMessage(new TranslatableComponent("command.sona.get_rust.success", itemStack.getDisplayName(), rustLevel), false);
    }
}
