package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class InventoryCommand extends FppCommand {
    private final FakePlayerManager manager;
    public InventoryCommand(FakePlayerManager manager) { super("inventory", "Manage bot inventory", "fpp.inventory"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Manage bot inventory - use /fpp inventory <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
