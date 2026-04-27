package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class AlertCommand extends FppCommand {
    public AlertCommand() { super("alert", "Send alert to all bots", "fpp.alert"); }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length == 0) { ctx.getSource().sendError(Text.literal("Usage: /fpp alert <message>")); return 0; }
        String msg = String.join(" ", args);
        ctx.getSource().getServer().getPlayerManager().broadcast(Text.literal("[FPP Alert] " + msg).formatted(Formatting.RED), false);
        return 1;
    }
}
