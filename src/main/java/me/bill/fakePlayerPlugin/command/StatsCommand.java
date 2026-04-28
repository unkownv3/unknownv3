package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.config.Config;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;

public class StatsCommand extends FppCommand {
    public StatsCommand() {
        super("stats", "Show FPP statistics", "fpp.stats");
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        int active = FakePlayerManager.count();
        int max = Config.maxBots();
        long frozen = FakePlayerManager.getAll().stream().filter(fp -> fp.isFrozen()).count();

        sendMessage(context, "\u00a7b\u00a7l[FPP] Statistics:");
        sendMessage(context, "\u00a77Active bots: \u00a7f" + active + "/" + max);
        sendMessage(context, "\u00a77Frozen bots: \u00a7f" + frozen);
        sendMessage(context, "\u00a77Database type: \u00a7f" + Config.databaseType());
        sendMessage(context, "\u00a77Version: \u00a7f1.6.6.7-fabric");
        return 1;
    }
}
