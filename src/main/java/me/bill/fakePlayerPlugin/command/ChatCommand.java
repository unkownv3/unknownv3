package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ChatCommand extends FppCommand {
    public ChatCommand() {
        super("chat", "Make a bot send a chat message", "fpp.chat", "say");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String botName = StringArgumentType.getString(ctx, "bot");
                    String message = StringArgumentType.getString(ctx, "message");
                    return sendChat(ctx, botName, message);
                })
            )
        );
    }

    private int sendChat(CommandContext<CommandSourceStack> ctx, String botName, String message) {
        FakePlayer fp = FakePlayerManager.getByName(botName);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + botName + "' not found");
            return 0;
        }

        var server = ctx.getSource().getServer();
        Component chatMessage = Component.literal("<" + fp.getName() + "> " + message);
        server.getPlayerList().broadcastSystemMessage(chatMessage, false);
        sendSuccess(ctx, fp.getName() + " said: " + message);
        return 1;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp chat <bot> <message>");
        return 0;
    }
}
