package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class PeaksCommand extends FppCommand {
    private final FakePlayerMod mod;
    public PeaksCommand(FakePlayerMod mod) { super("peaks", "Manage peak hours", "fpp.peaks"); this.mod = mod; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Manage peak hours - use /fpp peaks <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
