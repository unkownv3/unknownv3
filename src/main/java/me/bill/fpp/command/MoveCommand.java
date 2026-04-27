package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.PathfindingService;
import me.bill.fpp.fakeplayer.BotType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import java.util.List;
public class MoveCommand extends FppCommand {
    private final FakePlayerManager manager;
    private final PathfindingService pathfinding;
    public MoveCommand(FakePlayerManager manager, PathfindingService pathfinding) {
        super("move", "Move bot to location", "fpp.move", List.of("walk", "goto"));
        this.manager = manager; this.pathfinding = pathfinding;
    }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length < 2) { ctx.getSource().sendError(Text.literal("Usage: /fpp move <bot> <to|stop|x y z>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        if ("stop".equalsIgnoreCase(args[1])) {
            pathfinding.cancelNavigation(fp); fp.setBotType(BotType.AFK);
            ctx.getSource().sendMessage(Text.literal(fp.getName() + " stopped moving.").formatted(Formatting.GREEN)); return 1;
        }
        if ("to".equalsIgnoreCase(args[1])) {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            if (player == null) return 0;
            Vec3d target = player.getPos();
            fp.setBotType(BotType.MOVING);
            pathfinding.navigateTo(fp, target, () -> fp.setBotType(BotType.AFK));
            ctx.getSource().sendMessage(Text.literal(fp.getName() + " moving to your position.").formatted(Formatting.GREEN)); return 1;
        }
        if (args.length >= 4) {
            try {
                double x = Double.parseDouble(args[1]), y = Double.parseDouble(args[2]), z = Double.parseDouble(args[3]);
                fp.setBotType(BotType.MOVING);
                pathfinding.navigateTo(fp, new Vec3d(x, y, z), () -> fp.setBotType(BotType.AFK));
                ctx.getSource().sendMessage(Text.literal(fp.getName() + " moving to " + x + ", " + y + ", " + z).formatted(Formatting.GREEN));
            } catch (NumberFormatException e) { ctx.getSource().sendError(Text.literal("Invalid coordinates")); }
        }
        return 1;
    }
}
