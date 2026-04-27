package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class CmdCommand extends FppCommand {
    private final FakePlayerManager manager;
    public CmdCommand(FakePlayerManager manager) { super("cmd", "Set bot right-click command", "fpp.cmd"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Set bot right-click command - use /fpp cmd <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
