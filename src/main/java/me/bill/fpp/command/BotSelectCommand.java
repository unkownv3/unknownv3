package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class BotSelectCommand extends FppCommand {
    private final FakePlayerManager manager;
    public BotSelectCommand(FakePlayerManager manager) { super("select", "Select bots for bulk actions", "fpp.select"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Select bots for bulk actions - use /fpp select <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
