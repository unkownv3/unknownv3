package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.BotType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
public class FollowCommand extends FppCommand {
    private final FakePlayerManager manager;
    public FollowCommand(FakePlayerManager manager) { super("follow", "Make bot follow a player", "fpp.follow"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length < 1) { ctx.getSource().sendError(Text.literal("Usage: /fpp follow <bot> [player|stop]")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        if (args.length >= 2 && "stop".equalsIgnoreCase(args[1])) { fp.setBotType(BotType.AFK); ctx.getSource().sendMessage(Text.literal(fp.getName() + " stopped following.").formatted(Formatting.GREEN)); return 1; }
        fp.setBotType(BotType.FOLLOWING);
        ctx.getSource().sendMessage(Text.literal(fp.getName() + " is now following you.").formatted(Formatting.GREEN));
        return 1;
    }
}
