package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TpCommand extends FppCommand {
    public TpCommand() {
        super("tp", "Teleport to/from a fake player", "fpp.tp");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "bot");
                return teleportToBot(ctx, name);
            })
            .then(Commands.literal("here")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    return teleportBotHere(ctx, name);
                })
            )
        );
    }

    private int teleportToBot(CommandContext<CommandSourceStack> ctx, String botName) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) {
            sendError(ctx, "This command can only be used by a player");
            return 0;
        }

        FakePlayer fp = FakePlayerManager.getByName(botName);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + botName + "' not found");
            return 0;
        }

        ServerPlayer bot = fp.getServerPlayer();
        player.teleportTo(bot.serverLevel(), bot.getX(), bot.getY(), bot.getZ(), bot.getYRot(), bot.getXRot());
        sendSuccess(ctx, "Teleported to " + botName);
        return 1;
    }

    private int teleportBotHere(CommandContext<CommandSourceStack> ctx, String botName) {
        ServerPlayer player = getPlayer(ctx);
        if (player == null) {
            sendError(ctx, "This command can only be used by a player");
            return 0;
        }

        FakePlayer fp = FakePlayerManager.getByName(botName);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + botName + "' not found");
            return 0;
        }

        ServerPlayer bot = fp.getServerPlayer();
        bot.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(),
            player.getYRot(), player.getXRot());
        sendSuccess(ctx, "Teleported " + botName + " to you");
        return 1;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp tp <bot> [here]");
        return 0;
    }
}
