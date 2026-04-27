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

public class AttackCommand extends FppCommand {
    public AttackCommand() {
        super("attack", "Make a bot attack entities", "fpp.attack");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .then(Commands.literal("start")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    return startAttack(ctx, name);
                })
            )
            .then(Commands.literal("stop")
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "bot");
                    return stopAttack(ctx, name);
                })
            )
            .then(Commands.argument("target", StringArgumentType.word())
                .suggests(CommandManager.suggestOnlinePlayers())
                .executes(ctx -> {
                    String botName = StringArgumentType.getString(ctx, "bot");
                    String targetName = StringArgumentType.getString(ctx, "target");
                    return attackTarget(ctx, botName, targetName);
                })
            )
        );
    }

    private int startAttack(CommandContext<CommandSourceStack> ctx, String botName) {
        FakePlayer fp = FakePlayerManager.getByName(botName);
        if (fp == null || fp.getServerPlayer() == null) {
            sendError(ctx, "Bot '" + botName + "' not found");
            return 0;
        }
        fp.setBotType(BotType.ATTACKING);
        sendSuccess(ctx, fp.getName() + " is now attacking nearby entities");
        return 1;
    }

    private int stopAttack(CommandContext<CommandSourceStack> ctx, String botName) {
        FakePlayer fp = FakePlayerManager.getByName(botName);
        if (fp == null) {
            sendError(ctx, "Bot '" + botName + "' not found");
            return 0;
        }
        fp.setBotType(BotType.AFK);
        sendSuccess(ctx, fp.getName() + " stopped attacking");
        return 1;
    }

    private int attackTarget(CommandContext<CommandSourceStack> ctx, String botName, String targetName) {
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
        fp.setBotType(BotType.ATTACKING);
        fp.getMetadata().put("attack_target", target.getUUID());
        sendSuccess(ctx, fp.getName() + " is attacking " + targetName);
        return 1;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp attack <bot> <start|stop|player>");
        return 0;
    }
}
