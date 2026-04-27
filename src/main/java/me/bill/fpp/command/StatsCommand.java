package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.database.DatabaseManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class StatsCommand extends FppCommand {
    private final FakePlayerManager manager;
    private final DatabaseManager db;
    public StatsCommand(FakePlayerManager manager, DatabaseManager db) { super("stats", "View bot statistics", "fpp.stats"); this.manager = manager; this.db = db; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("═══ FPP Stats ═══").formatted(Formatting.BLUE));
        ctx.getSource().sendMessage(Text.literal("  Online bots: " + manager.getBotCount()).formatted(Formatting.GRAY));
        if (db != null) { ctx.getSource().sendMessage(Text.literal("  Total sessions: " + db.getTotalSessionCount()).formatted(Formatting.GRAY)); }
        return 1;
    }
}
