package me.bill.fakePlayerPlugin;

import me.bill.fakePlayerPlugin.command.*;
import me.bill.fakePlayerPlugin.config.Config;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import me.bill.fakePlayerPlugin.util.FppLogger;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class FakePlayerPluginFabric implements DedicatedServerModInitializer {
    private static FakePlayerPluginFabric instance;
    private static MinecraftServer server;
    private CommandManager commandManager;

    public static FakePlayerPluginFabric getInstance() {
        return instance;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitializeServer() {
        instance = this;

        FppLogger.init();
        FppLogger.boldRule();
        FppLogger.highlight("Fake Player Plugin (FPP) v1.6.6.7 - Fabric");
        FppLogger.info("Ported from Paper to Fabric for MC 1.21.1");
        FppLogger.boldRule();

        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("fpp");
        Config.init(configDir);
        Config.debugStartup("Config loaded.");

        commandManager = new CommandManager();
        registerCommands();

        // Register Fabric events
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            commandManager.registerAll(dispatcher);
        });

        FppLogger.success("FPP Fabric initialized successfully!");
    }

    private void registerCommands() {
        commandManager.register(new SpawnCommand());
        commandManager.register(new DeleteCommand());
        commandManager.register(new ListCommand());
        commandManager.register(new TpCommand());
        commandManager.register(new InfoCommand());
        commandManager.register(new FreezeCommand());
        commandManager.register(new MoveCommand());
        commandManager.register(new FollowCommand());
        commandManager.register(new AttackCommand());
        commandManager.register(new ChatCommand());
        commandManager.register(new MineCommand());
        commandManager.register(new PlaceCommand());
        commandManager.register(new SleepCommand());
        commandManager.register(new StopCommand());
        commandManager.register(new RenameCommand());
        commandManager.register(new ReloadCommand());
        commandManager.register(new StatsCommand());
    }

    private void onServerStarted(MinecraftServer srv) {
        server = srv;
        FppLogger.success("Server started - FPP ready!");
        FppLogger.kv("Max bots", Config.maxBots());
        FppLogger.kv("Database", Config.databaseType());
        FppLogger.kv("Skin mode", Config.skinMode());
        FppLogger.kv("Body enabled", Config.bodyEnabled());
        FppLogger.kv("Persistence", Config.persistenceEnabled());
    }

    private void onServerStopping(MinecraftServer srv) {
        FppLogger.info("Server stopping - despawning all bots...");
        int count = FakePlayerManager.despawnAll();
        FppLogger.info("Despawned " + count + " bot(s)");
        server = null;
    }

    private int tickCounter = 0;

    private void onServerTick(MinecraftServer srv) {
        tickCounter++;
        // Tick bots every tick
        FakePlayerManager.tick();
    }
}
