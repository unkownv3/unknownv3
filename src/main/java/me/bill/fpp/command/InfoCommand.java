package me.bill.fpp.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.database.DatabaseManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.time.Duration;
import java.time.Instant;

public class InfoCommand extends FppCommand {
    private final FakePlayerManager manager;
    private final DatabaseManager db;
    public InfoCommand(FakePlayerManager manager, DatabaseManager db) {
        super("info", "Show bot information", "fpp.info");
        this.manager = manager; this.db = db;
    }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length == 0) { ctx.getSource().sendError(Text.literal("Usage: /fpp info <bot>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        var src = ctx.getSource();
        src.sendMessage(Text.literal("═══ Bot Info: " + fp.getName() + " ═══").formatted(Formatting.BLUE));
        src.sendMessage(Text.literal("  UUID: " + fp.getUuid()).formatted(Formatting.GRAY));
        src.sendMessage(Text.literal("  Spawned by: " + fp.getSpawnedBy()).formatted(Formatting.GRAY));
        src.sendMessage(Text.literal("  Type: " + fp.getBotType()).formatted(Formatting.GRAY));
        src.sendMessage(Text.literal("  Frozen: " + fp.isFrozen()).formatted(Formatting.GRAY));
        src.sendMessage(Text.literal("  Chat: " + fp.isChatEnabled()).formatted(Formatting.GRAY));
        src.sendMessage(Text.literal("  Personality: " + (fp.getAiPersonality() != null ? fp.getAiPersonality() : "default")).formatted(Formatting.GRAY));
        if (fp.getPlayerEntity() != null) {
            var pos = fp.getPlayerEntity().getPos();
            src.sendMessage(Text.literal("  Position: " + String.format("%.1f, %.1f, %.1f", pos.x, pos.y, pos.z)).formatted(Formatting.GRAY));
            src.sendMessage(Text.literal("  World: " + fp.getLiveWorldName()).formatted(Formatting.GRAY));
            src.sendMessage(Text.literal("  Health: " + String.format("%.1f", fp.getPlayerEntity().getHealth())).formatted(Formatting.GRAY));
        }
        Duration uptime = Duration.between(fp.getSpawnTime(), Instant.now());
        src.sendMessage(Text.literal("  Uptime: " + uptime.toHours() + "h " + uptime.toMinutesPart() + "m").formatted(Formatting.GRAY));
        return 1;
    }
}
