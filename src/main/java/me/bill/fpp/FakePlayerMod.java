package me.bill.fpp;

import me.bill.fpp.ai.AIProviderRegistry;
import me.bill.fpp.ai.BotConversationManager;
import me.bill.fpp.ai.PersonalityRepository;
import me.bill.fpp.command.CommandManager;
import me.bill.fpp.config.FppConfig;
import me.bill.fpp.config.BotMessageConfig;
import me.bill.fpp.config.BotNameConfig;
import me.bill.fpp.database.DatabaseManager;
import me.bill.fpp.fakeplayer.BotPersistence;
import me.bill.fpp.fakeplayer.ChunkLoader;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.PathfindingService;
import me.bill.fpp.fakeplayer.SkinManager;
import me.bill.fpp.fakeplayer.PeakHoursManager;
import me.bill.fpp.fakeplayer.BotSwapAI;
import me.bill.fpp.fakeplayer.BotChatAI;
import me.bill.fpp.fakeplayer.BotIdentityCache;
import me.bill.fpp.lang.Lang;
import me.bill.fpp.listener.FppEventListeners;
import me.bill.fpp.util.FppLogger;
import me.bill.fpp.util.BadwordFilter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.nio.file.Path;

public class FakePlayerMod implements DedicatedServerModInitializer {

    public static final String MOD_ID = "fpp";
    public static final String VERSION = "1.6.6.7";

    private static FakePlayerMod instance;
    private static MinecraftServer server;

    private FppConfig config;
    private CommandManager commandManager;
    private FakePlayerManager fakePlayerManager;
    private ChunkLoader chunkLoader;
    private DatabaseManager databaseManager;
    private BotPersistence botPersistence;
    private PathfindingService pathfindingService;
    private SkinManager skinManager;
    private AIProviderRegistry aiProviderRegistry;
    private BotConversationManager botConversationManager;
    private PersonalityRepository personalityRepository;
    private PeakHoursManager peakHoursManager;
    private BotSwapAI botSwapAI;
    private BotChatAI botChatAI;
    private BotIdentityCache botIdentityCache;
    private BotNameConfig botNameConfig;
    private BotMessageConfig botMessageConfig;

    public static FakePlayerMod getInstance() {
        return instance;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitializeServer() {
        instance = this;
        FppLogger.init();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        FppEventListeners.register();

        FppLogger.info("FakePlayerPlugin (Fabric) v" + VERSION + " pre-initialized.");
    }

    private void onServerStarting(MinecraftServer srv) {
        server = srv;
        File configDir = getConfigDir().toFile();
        if (!configDir.exists()) configDir.mkdirs();

        config = new FppConfig(configDir);
        config.load();

        Lang.init(configDir);
        BadwordFilter.reload(configDir);

        botNameConfig = new BotNameConfig(configDir);
        botNameConfig.load();
        botMessageConfig = new BotMessageConfig(configDir);
        botMessageConfig.load();

        // Database
        if (config.databaseEnabled()) {
            databaseManager = new DatabaseManager();
            boolean dbOk = databaseManager.init(configDir);
            if (!dbOk) {
                FppLogger.warn("Database could not be initialised - session tracking disabled.");
                databaseManager = null;
            }
        }

        botIdentityCache = new BotIdentityCache(databaseManager, configDir);

        // Skin manager
        skinManager = new SkinManager(configDir);

        // Fake player manager
        fakePlayerManager = new FakePlayerManager(srv);
        if (databaseManager != null) {
            fakePlayerManager.setDatabaseManager(databaseManager);
        }
        fakePlayerManager.setIdentityCache(botIdentityCache);

        // Chunk loader
        chunkLoader = new ChunkLoader(fakePlayerManager);
        fakePlayerManager.setChunkLoader(chunkLoader);

        // Persistence
        botPersistence = new BotPersistence(configDir, fakePlayerManager);
        fakePlayerManager.setBotPersistence(botPersistence);

        // Pathfinding
        pathfindingService = new PathfindingService(fakePlayerManager);

        // AI
        personalityRepository = new PersonalityRepository(configDir);
        personalityRepository.load();
        aiProviderRegistry = new AIProviderRegistry(config);
        botConversationManager = new BotConversationManager(aiProviderRegistry, personalityRepository, config);
        botChatAI = new BotChatAI(fakePlayerManager, botConversationManager, config);
        botSwapAI = new BotSwapAI(fakePlayerManager, config);
        peakHoursManager = new PeakHoursManager(fakePlayerManager, config);

        // Commands
        commandManager = new CommandManager(this);

        FppLogger.info("FakePlayerPlugin (Fabric) v" + VERSION + " initialized.");
    }

    private void onServerStarted(MinecraftServer srv) {
        // Restore persistent bots
        if (config.persistenceEnabled() && botPersistence != null) {
            botPersistence.restoreBots(srv);
        }
        if (peakHoursManager != null && config.peakHoursEnabled()) {
            peakHoursManager.start();
        }
        FppLogger.info("FakePlayerPlugin (Fabric) v" + VERSION + " enabled. Bots restored.");
    }

    private void onServerStopping(MinecraftServer srv) {
        if (fakePlayerManager != null) {
            if (config.persistenceEnabled() && botPersistence != null) {
                botPersistence.saveBots(fakePlayerManager.getAllBots());
            }
            fakePlayerManager.despawnAll("server_shutdown");
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (peakHoursManager != null) {
            peakHoursManager.stop();
        }
        FppLogger.info("FakePlayerPlugin (Fabric) disabled.");
    }

    private void onServerTick(MinecraftServer srv) {
        if (fakePlayerManager != null) {
            fakePlayerManager.tick(srv);
        }
        if (botSwapAI != null) {
            botSwapAI.tick();
        }
    }

    public static Path getConfigDir() {
        return Path.of("config", MOD_ID);
    }

    public FppConfig getConfig() { return config; }
    public FakePlayerManager getFakePlayerManager() { return fakePlayerManager; }
    public CommandManager getCommandManager() { return commandManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public PathfindingService getPathfindingService() { return pathfindingService; }
    public SkinManager getSkinManager() { return skinManager; }
    public AIProviderRegistry getAiProviderRegistry() { return aiProviderRegistry; }
    public BotConversationManager getBotConversationManager() { return botConversationManager; }
    public PersonalityRepository getPersonalityRepository() { return personalityRepository; }
    public PeakHoursManager getPeakHoursManager() { return peakHoursManager; }
    public BotSwapAI getBotSwapAI() { return botSwapAI; }
    public BotChatAI getBotChatAI() { return botChatAI; }
    public BotIdentityCache getBotIdentityCache() { return botIdentityCache; }
    public BotNameConfig getBotNameConfig() { return botNameConfig; }
    public BotMessageConfig getBotMessageConfig() { return botMessageConfig; }
    public BotPersistence getBotPersistence() { return botPersistence; }
    public ChunkLoader getChunkLoader() { return chunkLoader; }
}
