package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.BotType;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SleepCommand extends FppCommand {
    public SleepCommand() {
        super("sleep", "Make a bot sleep in a bed", "fpp.sleep");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .then(Commands.literal("start")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    FakePlayer fp = FakePlayerManager.getByName(name);
                    if (fp == null) { sendError(ctx, "Bot not found"); return 0; }
                    fp.setBotType(BotType.SLEEPING);
                    sendSuccess(ctx, fp.getName() + " is now sleeping");
                    return 1;
                })
            )
            .then(Commands.literal("stop")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    FakePlayer fp = FakePlayerManager.getByName(name);
                    if (fp == null) { sendError(ctx, "Bot not found"); return 0; }
                    fp.setBotType(BotType.AFK);
                    sendSuccess(ctx, fp.getName() + " woke up");
                    return 1;
                })
            )
        );
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp sleep <bot> <start|stop>");
        return 0;
    }
}
