package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.BotType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
public class StopCommand extends FppCommand {
    private final FakePlayerManager manager;
    public StopCommand(FakePlayerManager manager) { super("stop", "Stop bot's current task", "fpp.stop", List.of("cancel")); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length == 0) { ctx.getSource().sendError(Text.literal("Usage: /fpp stop <bot|all>")); return 0; }
        if ("all".equalsIgnoreCase(args[0])) {
            for (FakePlayer fp : manager.getAllBots()) fp.setBotType(BotType.AFK);
            ctx.getSource().sendMessage(Text.literal("All bots stopped.").formatted(Formatting.GREEN)); return 1;
        }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        fp.setBotType(BotType.AFK); fp.setSleeping(false);
        ctx.getSource().sendMessage(Text.literal(fp.getName() + " stopped.").formatted(Formatting.GREEN));
        return 1;
    }
}
