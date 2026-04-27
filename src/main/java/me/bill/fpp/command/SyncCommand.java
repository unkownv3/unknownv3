package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class SyncCommand extends FppCommand {
    private final FakePlayerMod mod;
    public SyncCommand(FakePlayerMod mod) { super("sync", "Sync config across network", "fpp.sync"); this.mod = mod; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Sync config across network - use /fpp sync <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
