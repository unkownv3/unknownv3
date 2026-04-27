package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.config.Config;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import me.bill.fakePlayerPlugin.fakeplayer.SkinProfile;
import me.bill.fakePlayerPlugin.util.BotNameGenerator;
import me.bill.fakePlayerPlugin.util.SkinFetcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SpawnCommand extends FppCommand {
    public SpawnCommand() {
        super("spawn", "Spawn fake player(s)", "fpp.spawn");
    }

    @Override
    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node
            // /fpp spawn
            .executes(this::execute)
            // /fpp spawn <count>
            .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                .executes(ctx -> spawnMultiple(ctx, IntegerArgumentType.getInteger(ctx, "count"), null))
                // /fpp spawn <count> --name <name>
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> spawnMultiple(ctx,
                        IntegerArgumentType.getInteger(ctx, "count"),
                        StringArgumentType.getString(ctx, "name")))
                )
            )
            // /fpp spawn --name <name>
            .then(Commands.literal("--name")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> spawnNamed(ctx, StringArgumentType.getString(ctx, "name")))
                )
            );
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        return spawnMultiple(context, 1, null);
    }

    private int spawnNamed(CommandContext<CommandSourceStack> ctx, String name) {
        ServerPlayer player = getPlayer(ctx);
        Vec3 position;
        ServerLevel world;
        String spawnerName;
        UUID spawnerUuid;

        if (player != null) {
            position = player.position();
            world = player.serverLevel();
            spawnerName = player.getGameProfile().getName();
            spawnerUuid = player.getUUID();
        } else {
            var server = ctx.getSource().getServer();
            world = server.overworld();
            var spawnPos = world.getSharedSpawnPos();
            position = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            spawnerName = "Console";
            spawnerUuid = new UUID(0, 0);
        }

        SkinFetcher.fetchSkin(name).thenAccept(skin -> {
            ctx.getSource().getServer().execute(() -> {
                FakePlayer fp = FakePlayerManager.spawn(name, world, position, spawnerName, spawnerUuid, skin);
                if (fp != null) {
                    sendSuccess(ctx, "Spawned bot: " + name);
                } else {
                    sendError(ctx, "Failed to spawn bot: " + name);
                }
            });
        });

        return 1;
    }

    private int spawnMultiple(CommandContext<CommandSourceStack> ctx, int count, String baseName) {
        ServerPlayer player = getPlayer(ctx);
        Vec3 position;
        ServerLevel world;
        String spawnerName;
        UUID spawnerUuid;

        if (player != null) {
            position = player.position();
            world = player.serverLevel();
            spawnerName = player.getGameProfile().getName();
            spawnerUuid = player.getUUID();
        } else {
            var server = ctx.getSource().getServer();
            world = server.overworld();
            var spawnPos = world.getSharedSpawnPos();
            position = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            spawnerName = "Console";
            spawnerUuid = new UUID(0, 0);
        }

        sendMessage(ctx, "\u00a7bSpawning " + count + " bot(s)...");

        final ServerLevel finalWorld = world;
        final Vec3 finalPos = position;
        final String finalSpawner = spawnerName;
        final UUID finalUuid = spawnerUuid;

        for (int i = 0; i < count; i++) {
            String name = baseName != null ? (count == 1 ? baseName : baseName + "_" + (i + 1))
                                          : BotNameGenerator.generate();

            int delay = Config.joinDelayMin() + (int)(Math.random() * (Config.joinDelayMax() - Config.joinDelayMin() + 1));
            final String finalName = name;
            final int index = i;

            if (delay > 0) {
                ctx.getSource().getServer().execute(() -> {
                    SkinFetcher.fetchSkin(finalName).thenAccept(skin -> {
                        ctx.getSource().getServer().execute(() -> {
                            FakePlayerManager.spawn(finalName, finalWorld, finalPos, finalSpawner, finalUuid, skin);
                        });
                    });
                });
            } else {
                SkinFetcher.fetchSkin(finalName).thenAccept(skin -> {
                    ctx.getSource().getServer().execute(() -> {
                        FakePlayerManager.spawn(finalName, finalWorld, finalPos, finalSpawner, finalUuid, skin);
                    });
                });
            }
        }

        return 1;
    }
}
