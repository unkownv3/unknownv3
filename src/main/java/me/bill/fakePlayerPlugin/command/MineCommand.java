package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.BotType;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

public class MineCommand extends FppCommand {
    public MineCommand() {
        super("mine", "Make a bot mine blocks", "fpp.mine");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .then(Commands.literal("start")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    return startMining(ctx, name);
                })
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64))
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "bot");
                        int radius = IntegerArgumentType.getInteger(ctx, "radius");
                        return startMiningRadius(ctx, name, radius);
                    })
                )
            )
            .then(Commands.literal("stop")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    return stopMining(ctx, name);
                })
            )
        );
    }

    private int startMining(CommandContext<CommandSourceStack> ctx, String name) {
        return startMiningRadius(ctx, name, 8);
    }

    private int startMiningRadius(CommandContext<CommandSourceStack> ctx, String name, int radius) {
        FakePlayer fp = FakePlayerManager.getByName(name);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + name + "' not found");
            return 0;
        }
        fp.setBotType(BotType.MINING);
        fp.getMetadata().put("mine_radius", radius);
        sendSuccess(ctx, fp.getName() + " started mining (radius: " + radius + ")");
        return 1;
    }

    private int stopMining(CommandContext<CommandSourceStack> ctx, String name) {
        FakePlayer fp = FakePlayerManager.getByName(name);
        if (fp == null) {
            sendError(ctx, "Bot '" + name + "' not found");
            return 0;
        }
        fp.setBotType(BotType.AFK);
        fp.getMetadata().remove("mine_radius");
        sendSuccess(ctx, fp.getName() + " stopped mining");
        return 1;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp mine <bot> <start [radius]|stop>");
        return 0;
    }
}
