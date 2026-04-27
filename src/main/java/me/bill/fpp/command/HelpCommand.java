package me.bill.fpp.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HelpCommand extends FppCommand {
    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        super("help", "Show help information", "fpp.help");
        this.manager = manager;
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ServerCommandSource source = ctx.getSource();
        source.sendMessage(Text.literal("═══ FPP Help ═══").formatted(Formatting.BLUE));
        for (FppCommand cmd : manager.getCommands()) {
            source.sendMessage(Text.literal("  /fpp " + cmd.getName()).formatted(Formatting.YELLOW)
                .append(Text.literal(" - " + cmd.getDescription()).formatted(Formatting.GRAY)));
        }
        return 1;
    }
}
