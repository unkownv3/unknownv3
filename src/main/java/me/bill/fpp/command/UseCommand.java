package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class UseCommand extends FppCommand {
    private final FakePlayerManager manager;
    public UseCommand(FakePlayerManager manager) { super("use", "Make bot use/interact with items", "fpp.use"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Make bot use/interact with items - use /fpp use <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
