package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class FindCommand extends FppCommand {
    private final FakePlayerManager manager;
    public FindCommand(FakePlayerManager manager) { super("find", "Make bot find and mine specific blocks", "fpp.find"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Make bot find and mine specific blocks - use /fpp find <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
