package me.bill.fpp.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class ListCommand extends FppCommand {
    private final FakePlayerManager manager;
    public ListCommand(FakePlayerManager manager) {
        super("list", "List all fake players", "fpp.list", List.of("ls", "l"));
        this.manager = manager;
    }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        Collection<FakePlayer> bots = manager.getAllBots();
        if (bots.isEmpty()) {
            ctx.getSource().sendMessage(Text.literal("No bots online.").formatted(Formatting.GRAY));
            return 1;
        }
        ctx.getSource().sendMessage(Text.literal("═══ Bots (" + bots.size() + ") ═══").formatted(Formatting.BLUE));
        for (FakePlayer fp : bots) {
            String uptime = formatDuration(Duration.between(fp.getSpawnTime(), Instant.now()));
            ctx.getSource().sendMessage(Text.literal("  " + fp.getName()).formatted(Formatting.YELLOW)
                .append(Text.literal(" [" + fp.getBotType() + "] " + uptime + " by " + fp.getSpawnedBy()).formatted(Formatting.GRAY)));
        }
        return 1;
    }
    private String formatDuration(Duration d) {
        long h = d.toHours(); long m = d.toMinutesPart(); long s = d.toSecondsPart();
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}
