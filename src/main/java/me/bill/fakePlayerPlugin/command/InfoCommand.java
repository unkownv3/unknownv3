package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.time.Duration;
import java.time.Instant;

public class InfoCommand extends FppCommand {
    public InfoCommand() {
        super("info", "View info about a fake player", "fpp.info");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.argument("bot", StringArgumentType.word())
            .suggests(CommandManager.suggestBotNames())
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "bot");
                return showInfo(ctx, name);
            })
        );
    }

    private int showInfo(CommandContext<CommandSourceStack> ctx, String name) {
        FakePlayer fp = FakePlayerManager.getByName(name);
        if (fp == null) {
            sendError(ctx, "Bot '" + name + "' not found");
            return 0;
        }

        Duration uptime = Duration.between(fp.getSpawnTime(), Instant.now());
        String pos = "unknown";
        if (fp.getServerPlayer() != null) {
            pos = String.format("%.1f, %.1f, %.1f",
                fp.getServerPlayer().getX(), fp.getServerPlayer().getY(), fp.getServerPlayer().getZ());
        }

        sendMessage(ctx, "\u00a7b\u00a7l[FPP] Bot Info: " + fp.getName());
        sendMessage(ctx, "\u00a77UUID: \u00a7f" + fp.getUuid());
        sendMessage(ctx, "\u00a77Spawned by: \u00a7f" + fp.getSpawnedBy());
        sendMessage(ctx, "\u00a77Status: \u00a7f" + fp.getBotType().displayName());
        sendMessage(ctx, "\u00a77Position: \u00a7f" + pos);
        sendMessage(ctx, "\u00a77World: \u00a7f" + (fp.getLastKnownWorld() != null ? fp.getLastKnownWorld() : "unknown"));
        sendMessage(ctx, "\u00a77Alive: \u00a7f" + fp.isAlive());
        sendMessage(ctx, "\u00a77Frozen: \u00a7f" + fp.isFrozen());
        sendMessage(ctx, "\u00a77Uptime: \u00a7f" + formatDuration(uptime));
        sendMessage(ctx, "\u00a77Deaths: \u00a7f" + fp.getDeathCount());
        sendMessage(ctx, "\u00a77Damage taken: \u00a7f" + String.format("%.1f", fp.getTotalDamageTaken()));

        return 1;
    }

    private String formatDuration(Duration d) {
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        sendError(context, "Usage: /fpp info <bot>");
        return 0;
    }
}
