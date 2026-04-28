package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RenameCommand extends FppCommand {
    public RenameCommand() {
        super("rename", "Rename a fake player's display name", "fpp.rename");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .then(Commands.argument("newname", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String botName = StringArgumentType.getString(ctx, "bot");
                    String newName = StringArgumentType.getString(ctx, "newname");
                    FakePlayer fp = FakePlayerManager.getByName(botName);
                    if (fp == null) {
                        sendError(ctx, "Bot '" + botName + "' not found");
                        return 0;
                    }
                    fp.setDisplayName(newName);
                    if (fp.getServerPlayer() != null) {
                        fp.getServerPlayer().setCustomName(
                            net.minecraft.network.chat.Component.literal(newName)
                        );
                        fp.getServerPlayer().setCustomNameVisible(true);
                    }
                    sendSuccess(ctx, "Renamed " + botName + " display to: " + newName);
                    return 1;
                })
            )
        );
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp rename <bot> <newname>");
        return 0;
    }
}
