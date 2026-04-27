package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class FreezeCommand extends FppCommand {
    private final FakePlayerManager manager;
    public FreezeCommand(FakePlayerManager manager) { super("freeze", "Freeze/unfreeze a bot", "fpp.freeze"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length == 0) { ctx.getSource().sendError(Text.literal("Usage: /fpp freeze <bot>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        fp.setFrozen(!fp.isFrozen());
        ctx.getSource().sendMessage(Text.literal(fp.getName() + " is now " + (fp.isFrozen() ? "frozen" : "unfrozen")).formatted(Formatting.GREEN));
        return 1;
    }
}
