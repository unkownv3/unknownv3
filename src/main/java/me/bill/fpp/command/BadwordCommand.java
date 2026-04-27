package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class BadwordCommand extends FppCommand {
    private final FakePlayerManager manager;
    public BadwordCommand(FakePlayerManager manager) { super("badword", "Manage badword filter", "fpp.badword"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Manage badword filter - use /fpp badword <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
