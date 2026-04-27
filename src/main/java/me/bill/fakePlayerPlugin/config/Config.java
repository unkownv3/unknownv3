package me.bill.fakePlayerPlugin.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.bill.fakePlayerPlugin.util.FppLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configDir;
    private static JsonObject cfg = new JsonObject();

    private Config() {}

    public static void init(Path modConfigDir) {
        configDir = modConfigDir;
        reload();
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static void reload() {
        Path file = configDir.resolve("config.json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                cfg = GSON.fromJson(reader, JsonObject.class);
                if (cfg == null) cfg = new JsonObject();
            } catch (Exception e) {
                FppLogger.error("Failed to load config.json: " + e.getMessage());
                cfg = new JsonObject();
            }
        } else {
            cfg = getDefaults();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(configDir);
            try (Writer writer = Files.newBufferedWriter(configDir.resolve("config.json"), StandardCharsets.UTF_8)) {
                GSON.toJson(cfg, writer);
            }
        } catch (IOException e) {
            FppLogger.error("Failed to save config.json: " + e.getMessage());
        }
    }

    private static JsonObject getDefaults() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("config-version", 67);
        defaults.addProperty("language", "en");
        defaults.addProperty("debug", false);

        JsonObject limits = new JsonObject();
        limits.addProperty("max-bots", 1000);
        limits.addProperty("user-bot-limit", 1);
        defaults.add("limits", limits);

        defaults.addProperty("spawn-cooldown", 0);

        JsonObject persistence = new JsonObject();
        persistence.addProperty("enabled", true);
        defaults.add("persistence", persistence);

        JsonObject joinDelay = new JsonObject();
        joinDelay.addProperty("min", 0);
        joinDelay.addProperty("max", 1);
        defaults.add("join-delay", joinDelay);

        JsonObject leaveDelay = new JsonObject();
        leaveDelay.addProperty("min", 0);
        leaveDelay.addProperty("max", 1);
        defaults.add("leave-delay", leaveDelay);

        JsonObject botName = new JsonObject();
        botName.addProperty("mode", "random");
        botName.addProperty("admin-format", "{bot_name}");
        botName.addProperty("user-format", "bot-{spawner}-{num}");
        defaults.add("bot-name", botName);

        JsonObject skin = new JsonObject();
        skin.addProperty("mode", "player");
        skin.addProperty("guaranteed-skin", true);
        skin.addProperty("clear-cache-on-reload", true);
        defaults.add("skin", skin);

        JsonObject tabList = new JsonObject();
        tabList.addProperty("enabled", true);
        tabList.addProperty("listed", true);
        defaults.add("tab-list", tabList);

        JsonObject serverList = new JsonObject();
        serverList.addProperty("count-bots", true);
        serverList.addProperty("include-remote-bots", false);
        defaults.add("server-list", serverList);

        JsonObject body = new JsonObject();
        body.addProperty("enabled", true);
        body.addProperty("invulnerable", false);
        body.addProperty("respawn", true);
        body.addProperty("pick-up-items", false);
        body.addProperty("pick-up-xp", false);
        body.addProperty("collision", true);
        body.addProperty("knockback", true);
        body.addProperty("gravity", true);
        body.addProperty("head-ai", true);
        body.addProperty("swim-ai", true);
        body.addProperty("auto-eat", false);
        body.addProperty("auto-place-bed", false);
        defaults.add("body", body);

        JsonObject pathfinding = new JsonObject();
        pathfinding.addProperty("parkour", false);
        pathfinding.addProperty("break-blocks", false);
        pathfinding.addProperty("place-blocks", false);
        defaults.add("pathfinding", pathfinding);

        JsonObject attack = new JsonObject();
        attack.addProperty("mob-default-range", 16.0);
        attack.addProperty("mob-default-priority", "nearest");
        defaults.add("attack", attack);

        JsonObject database = new JsonObject();
        database.addProperty("type", "sqlite");
        defaults.add("database", database);

        JsonObject updateChecker = new JsonObject();
        updateChecker.addProperty("enabled", true);
        defaults.add("update-checker", updateChecker);

        JsonObject help = new JsonObject();
        help.addProperty("mode", "gui");
        defaults.add("help", help);

        JsonObject metrics = new JsonObject();
        metrics.addProperty("enabled", true);
        defaults.add("metrics", metrics);

        defaults.addProperty("bad-word-filter", false);

        return defaults;
    }

    // Helper accessors
    private static String getString(String path, String def) {
        String[] parts = path.split("\\.");
        JsonObject obj = cfg;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement e = obj.get(parts[i]);
            if (e != null && e.isJsonObject()) obj = e.getAsJsonObject();
            else return def;
        }
        JsonElement val = obj.get(parts[parts.length - 1]);
        return (val != null && val.isJsonPrimitive()) ? val.getAsString() : def;
    }

    private static int getInt(String path, int def) {
        String[] parts = path.split("\\.");
        JsonObject obj = cfg;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement e = obj.get(parts[i]);
            if (e != null && e.isJsonObject()) obj = e.getAsJsonObject();
            else return def;
        }
        JsonElement val = obj.get(parts[parts.length - 1]);
        return (val != null && val.isJsonPrimitive()) ? val.getAsInt() : def;
    }

    private static double getDouble(String path, double def) {
        String[] parts = path.split("\\.");
        JsonObject obj = cfg;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement e = obj.get(parts[i]);
            if (e != null && e.isJsonObject()) obj = e.getAsJsonObject();
            else return def;
        }
        JsonElement val = obj.get(parts[parts.length - 1]);
        return (val != null && val.isJsonPrimitive()) ? val.getAsDouble() : def;
    }

    private static boolean getBoolean(String path, boolean def) {
        String[] parts = path.split("\\.");
        JsonObject obj = cfg;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement e = obj.get(parts[i]);
            if (e != null && e.isJsonObject()) obj = e.getAsJsonObject();
            else return def;
        }
        JsonElement val = obj.get(parts[parts.length - 1]);
        return (val != null && val.isJsonPrimitive()) ? val.getAsBoolean() : def;
    }

    // Public config accessors (matching Paper plugin API)
    public static int configVersion() { return getInt("config-version", 0); }
    public static String getLanguage() { return getString("language", "en"); }
    public static boolean isDebug() { return getBoolean("debug", false); }

    public static boolean debugStartup() { return isDebug() || getBoolean("logging.debug.startup", false); }
    public static boolean debugNms() { return isDebug() || getBoolean("logging.debug.nms", false); }
    public static boolean debugPackets() { return isDebug() || getBoolean("logging.debug.packets", false); }
    public static boolean debugLuckPerms() { return isDebug() || getBoolean("logging.debug.luckperms", false); }
    public static boolean debugNetwork() { return isDebug() || getBoolean("logging.debug.network", false); }
    public static boolean debugSkin() { return isDebug() || getBoolean("logging.debug.skin", false); }
    public static boolean debugDatabase() { return isDebug() || getBoolean("logging.debug.database", false); }
    public static boolean debugChat() { return isDebug() || getBoolean("logging.debug.chat", false); }
    public static boolean debugSwap() { return isDebug() || getBoolean("logging.debug.swap", false); }

    public static void debug(String message) {
        if (isDebug()) FppLogger.debug(message);
    }

    public static void debugStartup(String message) {
        if (debugStartup()) FppLogger.debug("STARTUP", true, message);
    }

    public static boolean updateCheckerEnabled() { return getBoolean("update-checker.enabled", true); }
    public static String helpMode() { return getString("help.mode", "gui").toLowerCase(); }
    public static boolean metricsEnabled() { return getBoolean("metrics.enabled", true); }
    public static int spawnCooldown() { return Math.max(0, getInt("spawn-cooldown", 0)); }

    public static boolean tabListEnabled() { return getBoolean("tab-list.enabled", true); }
    public static boolean tabListListed() { return getBoolean("tab-list.listed", true); }

    public static boolean serverListCountBots() { return getBoolean("server-list.count-bots", true); }
    public static boolean serverListIncludeRemote() { return getBoolean("server-list.include-remote-bots", false); }

    public static int maxBots() { return getInt("limits.max-bots", 1000); }
    public static int userBotLimit() { return getInt("limits.user-bot-limit", 1); }

    public static String adminBotNameFormat() { return getString("bot-name.admin-format", "{bot_name}"); }
    public static String userBotNameFormat() { return getString("bot-name.user-format", "bot-{spawner}-{num}"); }
    public static String botNameMode() { return getString("bot-name.mode", "random"); }

    public static String skinMode() { return getString("skin.mode", "player"); }
    public static boolean guaranteedSkin() { return getBoolean("skin.guaranteed-skin", true); }
    public static boolean clearSkinCacheOnReload() { return getBoolean("skin.clear-cache-on-reload", true); }

    public static boolean persistenceEnabled() { return getBoolean("persistence.enabled", true); }
    public static int joinDelayMin() { return getInt("join-delay.min", 0); }
    public static int joinDelayMax() { return getInt("join-delay.max", 1); }
    public static int leaveDelayMin() { return getInt("leave-delay.min", 0); }
    public static int leaveDelayMax() { return getInt("leave-delay.max", 1); }

    public static boolean bodyEnabled() { return getBoolean("body.enabled", true); }
    public static boolean bodyInvulnerable() { return getBoolean("body.invulnerable", false); }
    public static boolean bodyRespawn() { return getBoolean("body.respawn", true); }
    public static boolean bodyPickUpItems() { return getBoolean("body.pick-up-items", false); }
    public static boolean bodyPickUpXp() { return getBoolean("body.pick-up-xp", false); }
    public static boolean bodyCollision() { return getBoolean("body.collision", true); }
    public static boolean bodyKnockback() { return getBoolean("body.knockback", true); }
    public static boolean bodyGravity() { return getBoolean("body.gravity", true); }
    public static boolean bodyHeadAi() { return getBoolean("body.head-ai", true); }
    public static boolean swimAiEnabled() { return getBoolean("body.swim-ai", true); }
    public static boolean autoEatEnabled() { return getBoolean("body.auto-eat", false); }
    public static boolean autoPlaceBedEnabled() { return getBoolean("body.auto-place-bed", false); }

    public static boolean pathfindingParkour() { return getBoolean("pathfinding.parkour", false); }
    public static boolean pathfindingBreakBlocks() { return getBoolean("pathfinding.break-blocks", false); }
    public static boolean pathfindingPlaceBlocks() { return getBoolean("pathfinding.place-blocks", false); }

    public static double attackMobDefaultRange() { return getDouble("attack.mob-default-range", 16.0); }
    public static String attackMobDefaultPriority() { return getString("attack.mob-default-priority", "nearest"); }

    public static String databaseType() { return getString("database.type", "sqlite"); }
    public static String databaseHost() { return getString("database.host", "localhost"); }
    public static int databasePort() { return getInt("database.port", 3306); }
    public static String databaseName() { return getString("database.name", "fpp"); }
    public static String databaseUser() { return getString("database.user", "root"); }
    public static String databasePassword() { return getString("database.password", ""); }

    public static boolean isBadwordFilterEnabled() { return getBoolean("bad-word-filter", false); }
}
