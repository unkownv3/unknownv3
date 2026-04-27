package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class RenameCommand extends FppCommand {
    private final FakePlayerManager manager;
    public RenameCommand(FakePlayerManager manager) { super("rename", "Rename a bot", "fpp.rename"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Rename a bot - use /fpp rename <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
