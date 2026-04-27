package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class FreezeCommand extends FppCommand {
    public FreezeCommand() {
        super("freeze", "Freeze/unfreeze a fake player", "fpp.freeze");
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
                fp.setFrozen(!fp.isFrozen());
                sendSuccess(ctx, fp.getName() + " is now " + (fp.isFrozen() ? "frozen" : "unfrozen"));
                return 1;
            })
        ).then(Commands.literal("all")
            .executes(ctx -> {
                boolean freeze = FakePlayerManager.getAll().stream().anyMatch(fp -> !fp.isFrozen());
                FakePlayerManager.getAll().forEach(fp -> fp.setFrozen(freeze));
                sendSuccess(ctx, (freeze ? "Froze" : "Unfroze") + " all bots");
                return 1;
            })
        );
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp freeze <bot|all>");
        return 0;
    }
}
