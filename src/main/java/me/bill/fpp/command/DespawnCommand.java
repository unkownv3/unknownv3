package me.bill.fpp.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class DespawnCommand extends FppCommand {
    private final FakePlayerManager manager;

    public DespawnCommand(FakePlayerManager manager) {
        super("despawn", "Remove fake player(s)", "fpp.despawn", List.of("delete", "remove", "d"));
        this.manager = manager;
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ServerCommandSource source = ctx.getSource();

        if (args.length == 0) {
            source.sendError(Text.literal("Usage: /fpp despawn <name|all|count>"));
            return 0;
        }

        String target = args[0];
        if ("all".equalsIgnoreCase(target)) {
            int count = manager.getBotCount();
            manager.despawnAll("command");
            source.sendMessage(Text.literal("Despawned all " + count + " bot(s).").formatted(Formatting.GREEN));
            return 1;
        }

        // Try as number
        try {
            int count = Integer.parseInt(target);
            List<FakePlayer> bots = new ArrayList<>(manager.getAllBots());
            int removed = 0;
            for (int i = 0; i < Math.min(count, bots.size()); i++) {
                if (manager.despawnBot(bots.get(i), "command")) removed++;
            }
            source.sendMessage(Text.literal("Despawned " + removed + " bot(s).").formatted(Formatting.GREEN));
            return 1;
        } catch (NumberFormatException ignored) {}

        // Try as name
        FakePlayer fp = manager.getBot(target);
        if (fp != null) {
            manager.despawnBot(fp, "command");
            source.sendMessage(Text.literal("Despawned bot: " + target).formatted(Formatting.GREEN));
            return 1;
        }

        source.sendError(Text.literal("Bot not found: " + target));
        return 0;
    }

    @Override
    public List<String> tabComplete(CommandContext<ServerCommandSource> ctx, String[] args) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("all");
        for (FakePlayer fp : manager.getAllBots()) {
            suggestions.add(fp.getName());
        }
        return suggestions;
    }
}
