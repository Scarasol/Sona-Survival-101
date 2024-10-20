package com.scarasol.sona.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class InfectionCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("infection").requires(s -> s.hasPermission(2)).then(Commands.argument("entity", EntityArgument.entity()).then(Commands.literal("get").executes(arguments -> {
            commandProcess(arguments, 0);
            return 0;
        })).then(Commands.literal("set").then(Commands.argument("number", DoubleArgumentType.doubleArg(0, 100)).executes(arguments -> {
            commandProcess(arguments, 1);
            return 0;
        }))).then(Commands.literal("add").then(Commands.argument("number", DoubleArgumentType.doubleArg(-100, 100)).executes(arguments -> {
            commandProcess(arguments, 2);
            return 0;
        })))));
    }

    private static void commandProcess(CommandContext<CommandSourceStack> arguments, int process) {
        Entity entity = arguments.getSource().getEntity();
        if (entity == null)
            entity = FakePlayerFactory.getMinecraft(arguments.getSource().getLevel());
        if (entity instanceof Player player) {
            try {
                Entity target = EntityArgument.getEntity(arguments, "entity");
                if (CommonConfig.INFECTION_OPEN.get()) {
                    if (target instanceof Player || CommonConfig.SUSCEPTIBLE_POPULATION.get().contains(ForgeRegistries.ENTITIES.getKey(target.getType()).toString())) {
                        if (process == 0) {
                            getCommand(player, target);
                        } else {
                            float infectionLevel = (float) DoubleArgumentType.getDouble(arguments, "number");
                            if (process == 1) {
                                setCommand(player, target, infectionLevel);
                            } else {
                                addCommand(player, target, infectionLevel);
                            }
                        }
                    } else {
                        player.displayClientMessage(new TranslatableComponent("command.sona.infection.uninfected"), false);
                    }
                } else {
                    player.displayClientMessage(new TranslatableComponent("command.sona.infection.close"), false);
                }
            } catch (CommandSyntaxException e) {
                player.displayClientMessage(new TranslatableComponent("command.sona.not_found"), false);
            }
        }
    }

    private static void getCommand(Player player, Entity target) {
        if (target instanceof ILivingEntityAccessor survivalEntity) {
            String infectionLevel = String.format("%.2f", InfectionManager.getInfection(survivalEntity));
            player.displayClientMessage(new TranslatableComponent("command.sona.get_infection.success", target.getDisplayName(), infectionLevel), false);
        }

    }

    private static void setCommand(Player player, Entity target, float infectionLevel) {
        if (target instanceof ILivingEntityAccessor survivalEntity) {
            InfectionManager.setInfection(survivalEntity, infectionLevel);
            player.displayClientMessage(new TranslatableComponent("command.sona.modify_infection.success", target.getDisplayName(), infectionLevel), false);
        }
    }

    private static void addCommand(Player player, Entity target, float infectionLevel) {
        if (target instanceof ILivingEntityAccessor survivalEntity) {
            InfectionManager.addActualInfection(survivalEntity, infectionLevel);
            player.displayClientMessage(new TranslatableComponent("command.sona.modify_infection.success", target.getDisplayName(), InfectionManager.getInfection(survivalEntity)), false);
        }
    }

}
