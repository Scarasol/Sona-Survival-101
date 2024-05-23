package com.scarasol.sona.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.manager.InjuryManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayerFactory;

public class InjuryCommand {
    public static void registerInjuryCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("injury").requires(s -> s.hasPermission(2)).then(Commands.argument("entity", EntityArgument.player()).then(Commands.literal("get").executes(arguments -> {
            commandProcess(arguments, true, 0);
            return 0;
        })).then(Commands.literal("set").then(Commands.argument("number", DoubleArgumentType.doubleArg(0, 100)).executes(arguments -> {
            commandProcess(arguments, true, 1);
            return 0;
        }))).then(Commands.literal("add").then(Commands.argument("number", DoubleArgumentType.doubleArg(-100, 100)).executes(arguments -> {
            commandProcess(arguments, true, 2);
            return 0;
        })))));
    }

    public static void registerBandageCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bandage").requires(s -> s.hasPermission(2)).then(Commands.argument("entity", EntityArgument.player()).then(Commands.literal("get").executes(arguments -> {
            commandProcess(arguments, false, 0);
            return 0;
        })).then(Commands.literal("set").then(Commands.argument("number", DoubleArgumentType.doubleArg(0, 50)).executes(arguments -> {
            commandProcess(arguments, false, 1);
            return 0;
        }))).then(Commands.literal("add").then(Commands.argument("number", DoubleArgumentType.doubleArg(-50, 50)).executes(arguments -> {
            commandProcess(arguments, false, 2);
            return 0;
        })))));
    }

    private static void commandProcess(CommandContext<CommandSourceStack> arguments, boolean injury, int process) {
        Entity entity = arguments.getSource().getEntity();
        if (entity == null)
            entity = FakePlayerFactory.getMinecraft(arguments.getSource().getLevel());
        if (entity instanceof Player player) {
            try {
                Entity target = EntityArgument.getEntity(arguments, "entity");
                if (CommonConfig.INJURY_OPEN.get()) {
                    if (process == 0) {
                        getInjuryCommand(player, target, injury);
                    } else {
                        float level = (float) DoubleArgumentType.getDouble(arguments, "number");
                        if (process == 1) {
                            setInjuryCommand(player, target, level, injury);
                        } else {
                            addInjuryCommand(player, target, level, injury);
                        }
                    }
                } else {
                    player.displayClientMessage(new TranslatableComponent("command.sona.injury.close"), false);
                }
            } catch (CommandSyntaxException e) {
                player.displayClientMessage(new TranslatableComponent("command.sona.not_found"), false);
            }
        }
    }

    private static void getInjuryCommand(Player player, Entity target, boolean injury) {
        if (target instanceof ILivingEntityAccessor survivalEntity) {
            if (injury) {
                String injuryLevel = String.format("%.2f", InjuryManager.getInjury(survivalEntity));
                player.displayClientMessage(new TranslatableComponent("command.sona.get_injury.success", target.getDisplayName(), injuryLevel), false);
            } else {
                String bandageLevel = String.format("%.2f", InjuryManager.getBandage(survivalEntity));
                player.displayClientMessage(new TranslatableComponent("command.sona.get_bandage.success", target.getDisplayName(), bandageLevel), false);
            }
        }
    }

    private static void setInjuryCommand(Player player, Entity target, float level, boolean injury) {
        if (target instanceof ILivingEntityAccessor survivalEntity) {
            if (injury) {
                InjuryManager.setInjury(survivalEntity, level);
                player.displayClientMessage(new TranslatableComponent("command.sona.modify_injury.success", target.getDisplayName(), InjuryManager.getInjury(survivalEntity)), false);
            } else {
                InjuryManager.setBandageSafe(survivalEntity, level);
                player.displayClientMessage(new TranslatableComponent("command.sona.modify_bandage.success", target.getDisplayName(), InjuryManager.getBandage(survivalEntity)), false);
            }
        }
    }

    private static void addInjuryCommand(Player player, Entity target, float level, boolean injury) {
        if (target instanceof ILivingEntityAccessor survivalEntity) {
            if (injury) {
                InjuryManager.addInjury(survivalEntity, level);
                player.displayClientMessage(new TranslatableComponent("command.sona.modify_injury.success", target.getDisplayName(), InjuryManager.getInjury(survivalEntity)), false);
            } else {
                InjuryManager.addBandage(survivalEntity, level);
                player.displayClientMessage(new TranslatableComponent("command.sona.modify_bandage.success", target.getDisplayName(), InjuryManager.getBandage(survivalEntity)), false);
            }
        }
    }
}
