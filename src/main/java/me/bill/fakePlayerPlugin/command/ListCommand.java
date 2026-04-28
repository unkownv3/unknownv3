package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.time.Instant;

public class ListCommand extends FppCommand {
    public ListCommand() {
        super("list", "List all fake players", "fpp.list", "ls");
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        var bots = FakePlayerManager.getAll();
        if (bots.isEmpty()) {
            sendMessage(context, "\u00a7bNo bots are currently spawned.");
            return 0;
        }

        sendMessage(context, "\u00a7b\u00a7lFake Players (" + bots.size() + "):");
        for (FakePlayer fp : bots) {
            Duration uptime = Duration.between(fp.getSpawnTime(), Instant.now());
            String time = formatDuration(uptime);
            String world = fp.getLastKnownWorld() != null ? fp.getLastKnownWorld() : "unknown";
            String pos = "unknown";
            if (fp.getServerPlayer() != null) {
                pos = String.format("%.0f, %.0f, %.0f",
                    fp.getServerPlayer().getX(),
                    fp.getServerPlayer().getY(),
                    fp.getServerPlayer().getZ());
            }
            sendMessage(context, String.format(
                "\u00a77 - \u00a7b%s \u00a77[\u00a7f%s\u00a77] at \u00a7f%s \u00a77(%s) by \u00a7f%s",
                fp.getName(), fp.getBotType().displayName(), pos, world, fp.getSpawnedBy()
            ));
        }
        return bots.size();
    }

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long mins = d.toMinutesPart();
        long secs = d.toSecondsPart();
        if (hours > 0) return hours + "h " + mins + "m";
        if (mins > 0) return mins + "m " + secs + "s";
        return secs + "s";
    }
}
