package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class WaypointCommand extends FppCommand {
    private final FakePlayerManager manager;
    public WaypointCommand(FakePlayerManager manager) { super("waypoint", "Manage bot waypoints", "fpp.move.waypoint"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Manage bot waypoints - use /fpp waypoint <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
