package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class ReloadCommand extends FppCommand {
    private final FakePlayerMod mod;
    public ReloadCommand(FakePlayerMod mod) { super("reload", "Reload configuration", "fpp.reload"); this.mod = mod; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        mod.getConfig().reload();
        mod.getBotNameConfig().load();
        mod.getBotMessageConfig().load();
        if (mod.getPersonalityRepository() != null) mod.getPersonalityRepository().reload();
        if (mod.getFakePlayerManager() != null) mod.getFakePlayerManager().refreshCleanNamePool();
        ctx.getSource().sendMessage(Text.literal("FPP configuration reloaded.").formatted(Formatting.GREEN));
        return 1;
    }
}
