package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.BotType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
public class PlaceCommand extends FppCommand {
    private final FakePlayerManager manager;
    public PlaceCommand(FakePlayerManager manager) { super("place", "Make bot place blocks", "fpp.place", List.of("build")); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length < 2) { ctx.getSource().sendError(Text.literal("Usage: /fpp place <bot> <start|stop>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        if ("stop".equalsIgnoreCase(args[1])) { fp.setBotType(BotType.AFK); ctx.getSource().sendMessage(Text.literal(fp.getName() + " stopped placing.").formatted(Formatting.GREEN)); return 1; }
        fp.setBotType(BotType.PLACING);
        ctx.getSource().sendMessage(Text.literal(fp.getName() + " started placing blocks from inventory.").formatted(Formatting.GREEN));
        return 1;
    }
}
