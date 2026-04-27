package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class SettingCommand extends FppCommand {
    private final FakePlayerManager manager;
    public SettingCommand(FakePlayerManager manager) { super("setting", "Modify bot settings", "fpp.settings"); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Modify bot settings - use /fpp setting <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
