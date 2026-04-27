package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class SaveCommand extends FppCommand {
    private final FakePlayerMod mod;
    public SaveCommand(FakePlayerMod mod) { super("save", "Force save bot data", "fpp.save"); this.mod = mod; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Force save bot data - use /fpp save <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
