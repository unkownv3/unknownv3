package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.BotType;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class MoveCommand extends FppCommand {
    public MoveCommand() {
        super("move", "Move a bot to coordinates or a player", "fpp.move");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .then(Commands.literal("to")
                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                    .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "bot");
                                double x = DoubleArgumentType.getDouble(ctx, "x");
                                double y = DoubleArgumentType.getDouble(ctx, "y");
                                double z = DoubleArgumentType.getDouble(ctx, "z");
                                return moveTo(ctx, name, x, y, z);
                            })
                        )
                    )
                )
                .then(Commands.argument("target", StringArgumentType.word())
                    .suggests(CommandManager.suggestOnlinePlayers())
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "bot");
                        String target = StringArgumentType.getString(ctx, "target");
                        return moveToPlayer(ctx, name, target);
                    })
                )
            )
            .then(Commands.literal("stop")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    return stopMoving(ctx, name);
                })
            )
        );
    }

    private int moveTo(CommandContext<CommandSourceStack> ctx, String name, double x, double y, double z) {
        FakePlayer fp = FakePlayerManager.getByName(name);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + name + "' not found");
            return 0;
        }

        ServerPlayer bot = fp.getServerPlayer();
        bot.teleportTo(bot.serverLevel(), x, y, z, bot.getYRot(), bot.getXRot());
        fp.setBotType(BotType.MOVING);
        sendSuccess(ctx, "Moved " + name + " to " + String.format("%.1f, %.1f, %.1f", x, y, z));
        return 1;
    }

    private int moveToPlayer(CommandContext<CommandSourceStack> ctx, String name, String targetName) {
        FakePlayer fp = FakePlayerManager.getByName(name);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + name + "' not found");
            return 0;
        }

        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            sendError(ctx, "Player '" + targetName + "' not found");
            return 0;
        }

        ServerPlayer bot = fp.getServerPlayer();
        bot.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(),
            target.getYRot(), target.getXRot());
        fp.setBotType(BotType.MOVING);
        sendSuccess(ctx, "Moved " + name + " to " + targetName);
        return 1;
    }

    private int stopMoving(CommandContext<CommandSourceStack> ctx, String name) {
        FakePlayer fp = FakePlayerManager.getByName(name);
        if (fp == null) {
            sendError(ctx, "Bot '" + name + "' not found");
            return 0;
        }
        fp.setBotType(BotType.AFK);
        sendSuccess(ctx, name + " stopped moving");
        return 1;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp move <bot> to <x> <y> <z> | /fpp move <bot> to <player>");
        return 0;
    }
}
