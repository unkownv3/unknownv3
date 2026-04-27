package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class RankCommand extends FppCommand {
    private final FakePlayerManager manager;
    public RankCommand(FakePlayerManager manager) { super("rank", "Set bot LuckPerms rank", "fpp.rank"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Set bot LuckPerms rank - use /fpp rank <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
