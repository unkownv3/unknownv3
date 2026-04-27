package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.BotType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class SleepCommand extends FppCommand {
    private final FakePlayerManager manager;
    public SleepCommand(FakePlayerManager manager) { super("sleep", "Make bot sleep in a bed", "fpp.sleep"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length < 1) { ctx.getSource().sendError(Text.literal("Usage: /fpp sleep <bot>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        fp.setBotType(BotType.SLEEPING); fp.setSleeping(true);
        ctx.getSource().sendMessage(Text.literal(fp.getName() + " is going to sleep.").formatted(Formatting.GREEN));
        return 1;
    }
}
