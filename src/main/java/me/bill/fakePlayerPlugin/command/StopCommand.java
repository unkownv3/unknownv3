package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.BotType;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class StopCommand extends FppCommand {
    public StopCommand() {
        super("stop", "Stop all tasks for a bot", "fpp.stop");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "bot");
                FakePlayer fp = FakePlayerManager.getByName(name);
                if (fp == null) {
                    sendError(ctx, "Bot '" + name + "' not found");
                    return 0;
                }
                fp.setBotType(BotType.AFK);
                fp.getMetadata().remove("follow_target");
                fp.getMetadata().remove("attack_target");
                fp.getMetadata().remove("mine_area");
                sendSuccess(ctx, fp.getName() + " stopped all tasks");
                return 1;
            })
        ).then(Commands.literal("all")
            .executes(ctx -> {
                FakePlayerManager.getAll().forEach(fp -> {
                    fp.setBotType(BotType.AFK);
                    fp.getMetadata().clear();
                });
                sendSuccess(ctx, "Stopped all tasks for all bots");
                return 1;
            })
        );
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp stop <bot|all>");
        return 0;
    }
}
