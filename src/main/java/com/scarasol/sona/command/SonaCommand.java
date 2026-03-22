package com.scarasol.sona.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.scarasol.sona.util.ServerRenderEmitter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SonaCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sona")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("indicator")
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .then(Commands.argument("range", DoubleArgumentType.doubleArg(0))
                                        .then(Commands.argument("duration", DoubleArgumentType.doubleArg(0))
                                                .executes(context -> {
                                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                    double range = DoubleArgumentType.getDouble(context, "range");
                                                    double durationSeconds = DoubleArgumentType.getDouble(context, "duration");
                                                    int durationTicks = Math.max(1, Mth.floor(durationSeconds * 20.0D));

                                                    ServerRenderEmitter.emitPositionIndicator(context.getSource().getLevel(), pos, range, durationTicks);
                                                    context.getSource().sendSuccess(() -> Component.translatable("command.sona.indicator.success", String.format("%.2f", pos.x), String.format("%.2f", pos.y), String.format("%.2f", pos.z), String.format("%.2f", range), String.format("%.2f", durationSeconds)), true);
                                                    return 1;
                                                }))))));
    }
}
