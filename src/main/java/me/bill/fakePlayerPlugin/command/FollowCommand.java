package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.BotType;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class FollowCommand extends FppCommand {
    public FollowCommand() {
        super("follow", "Make a bot follow a player", "fpp.follow");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .then(Commands.argument("target", StringArgumentType.word())
                .suggests(CommandManager.suggestOnlinePlayers())
                .executes(ctx -> {
                    String botName = StringArgumentType.getString(ctx, "bot");
                    String targetName = StringArgumentType.getString(ctx, "target");
                    return startFollow(ctx, botName, targetName);
                })
            )
            .executes(ctx -> {
                String botName = StringArgumentType.getString(ctx, "bot");
                ServerPlayer player = getPlayer(ctx);
                if (player == null) {
                    sendError(ctx, "Specify a target player");
                    return 0;
                }
                return startFollow(ctx, botName, player.getGameProfile().getName());
            })
        );
    }

    private int startFollow(CommandContext<CommandSourceStack> ctx, String botName, String targetName) {
        FakePlayer fp = FakePlayerManager.getByName(botName);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + botName + "' not found");
            return 0;
        }

        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            sendError(ctx, "Player '" + targetName + "' not found");
            return 0;
        }

        fp.setBotType(BotType.FOLLOWING);
        fp.getMetadata().put("follow_target", target.getUUID());
        sendSuccess(ctx, fp.getName() + " is now following " + targetName);
        return 1;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp follow <bot> [player]");
        return 0;
    }
}
