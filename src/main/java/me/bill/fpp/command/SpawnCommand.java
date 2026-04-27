package me.bill.fpp.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SpawnCommand extends FppCommand {
    private final FakePlayerManager manager;

    public SpawnCommand(FakePlayerManager manager) {
        super("spawn", "Spawn fake player(s)", "fpp.spawn", List.of("s", "add"));
        this.manager = manager;
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        int count = 1;
        String name = null;
        Vec3d pos = source.getPosition();
        ServerWorld world = source.getWorld();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--name=") || arg.startsWith("-n=")) {
                name = arg.substring(arg.indexOf('=') + 1);
            } else if (arg.equals("--name") || arg.equals("-n")) {
                if (i + 1 < args.length) name = args[++i];
            } else {
                try {
                    count = Integer.parseInt(arg);
                } catch (NumberFormatException ignored) {
                    if (name == null) name = arg;
                }
            }
        }

        String spawnerName = player != null ? player.getName().getString() : "CONSOLE";
        java.util.UUID spawnerUuid = player != null ? player.getUuid() : new java.util.UUID(0, 0);

        int spawned = 0;
        for (int i = 0; i < count; i++) {
            String botName = count == 1 && name != null ? name : null;
            FakePlayer fp = manager.spawnBot(botName, world, pos, 
                player != null ? player.getYaw() : 0,
                player != null ? player.getPitch() : 0,
                spawnerName, spawnerUuid);
            if (fp != null) spawned++;
        }

        if (spawned > 0) {
            source.sendMessage(Text.literal("Spawned " + spawned + " bot(s).").formatted(Formatting.GREEN));
        } else {
            source.sendError(Text.literal("Failed to spawn bots. Max limit may be reached."));
        }

        return spawned > 0 ? 1 : 0;
    }

    @Override
    public List<String> tabComplete(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length == 1) {
            return List.of("1", "5", "10", "--name=");
        }
        return List.of();
    }
}
