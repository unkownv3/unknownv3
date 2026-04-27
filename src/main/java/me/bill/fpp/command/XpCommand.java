package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class XpCommand extends FppCommand {
    private final FakePlayerManager manager;
    public XpCommand(FakePlayerManager manager) { super("xp", "Manage bot XP levels", "fpp.xp"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Manage bot XP levels - use /fpp xp <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
