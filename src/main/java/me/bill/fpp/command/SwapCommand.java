package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class SwapCommand extends FppCommand {
    private final FakePlayerManager manager;
    public SwapCommand(FakePlayerManager manager) { super("swap", "Swap bot positions", "fpp.swap"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Swap bot positions - use /fpp swap <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
