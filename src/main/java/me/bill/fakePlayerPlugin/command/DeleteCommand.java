package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class DeleteCommand extends FppCommand {
    public DeleteCommand() {
        super("despawn", "Despawn fake player(s)", "fpp.despawn", "delete", "remove");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node
            .then(Commands.literal("all")
                .executes(ctx -> {
                    int count = FakePlayerManager.despawnAll();
                    sendSuccess(ctx, "Despawned " + count + " bot(s)");
                    return count;
                }))
            .then(Commands.argument("name", StringArgumentType.word())
                .suggests(CommandManager.suggestBotNames())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    FakePlayer fp = FakePlayerManager.getByName(name);
                    if (fp == null) {
                        sendError(ctx, "Bot '" + name + "' not found");
                        return 0;
                    }
                    FakePlayerManager.despawn(fp);
                    sendSuccess(ctx, "Despawned bot: " + name);
                    return 1;
                }))
            .executes(this::execute);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp despawn <name|all>");
        return 0;
    }
}
