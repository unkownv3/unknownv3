package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class SetOwnerCommand extends FppCommand {
    private final FakePlayerManager manager;
    public SetOwnerCommand(FakePlayerManager manager) { super("setowner", "Transfer bot ownership", "fpp.setowner"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Transfer bot ownership - use /fpp setowner <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
