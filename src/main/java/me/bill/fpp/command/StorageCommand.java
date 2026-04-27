package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class StorageCommand extends FppCommand {
    private final FakePlayerManager manager;
    public StorageCommand(FakePlayerManager manager) { super("storage", "Manage bot storage locations", "fpp.storage"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Manage bot storage locations - use /fpp storage <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
