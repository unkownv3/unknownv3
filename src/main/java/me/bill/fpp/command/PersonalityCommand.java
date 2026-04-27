package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class PersonalityCommand extends FppCommand {
    private final FakePlayerMod mod;
    public PersonalityCommand(FakePlayerMod mod) { super("personality", "Set bot AI personality", "fpp.personality"); this.mod = mod; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ctx.getSource().sendMessage(Text.literal("Set bot AI personality - use /fpp personality <args>").formatted(Formatting.YELLOW));
        return 1;
    }
}
