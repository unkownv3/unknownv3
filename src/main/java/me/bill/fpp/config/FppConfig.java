package me.bill.fpp.config;

import me.bill.fpp.util.FppLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class FppConfig {

    private final File configDir;
    private File configFile;
    private Map<String, Object> data = new LinkedHashMap<>();

    public FppConfig(File configDir) {
        this.configDir = configDir;
        this.configFile = new File(configDir, "config.yml");
    }

    public void load() {
        if (!configFile.exists()) {
            saveDefault();
        }
        try (InputStream is = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(is);
            if (loaded instanceof Map) {
                data = (Map<String, Object>) loaded;
            }
        } catch (Exception e) {
            FppLogger.error("Failed to load config.yml: " + e.getMessage());
            data = new LinkedHashMap<>();
        }
    }

    public void reload() {
        load();
    }

    private void saveDefault() {
        try {
            configDir.mkdirs();
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (is != null) {
                    Files.copy(is, configFile.toPath());
                } else {
                    configFile.createNewFile();
                    saveDefaultConfig();
                }
            }
        } catch (IOException e) {
            FppLogger.error("Failed to save default config: " + e.getMessage());
        }
    }

    private void saveDefaultConfig() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("config-version", 67);
        defaults.put("language", "en");
        defaults.put("debug", false);

        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("max-bots", 1000);
        limits.put("user-bot-limit", 1);
        limits.put("spawn-presets", Arrays.asList(1, 5, 10, 15, 20));
        defaults.put("limits", limits);

        defaults.put("spawn-cooldown", 0);

        Map<String, Object> persistence = new LinkedHashMap<>();
        persistence.put("enabled", true);
        defaults.put("persistence", persistence);

        Map<String, Object> joinDelay = new LinkedHashMap<>();
        joinDelay.put("min", 0);
        joinDelay.put("max", 1);
        defaults.put("join-delay", joinDelay);

        Map<String, Object> leaveDelay = new LinkedHashMap<>();
        leaveDelay.put("min", 0);
        leaveDelay.put("max", 1);
        defaults.put("leave-delay", leaveDelay);

        Map<String, Object> botName = new LinkedHashMap<>();
        botName.put("mode", "random");
        botName.put("admin-format", "{bot_name}");
        botName.put("user-format", "bot-{spawner}-{num}");
        defaults.put("bot-name", botName);

        Map<String, Object> skin = new LinkedHashMap<>();
        skin.put("mode", "player");
        skin.put("guaranteed-skin", true);
        skin.put("clear-cache-on-reload", true);
        defaults.put("skin", skin);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", true);
        body.put("max-health", 20.0);
        body.put("pick-up-items", false);
        body.put("pick-up-xp", true);
        body.put("respawn", true);
        body.put("respawn-delay", 60);
        defaults.put("body", body);

        Map<String, Object> tabList = new LinkedHashMap<>();
        tabList.put("enabled", true);
        defaults.put("tab-list", tabList);

        Map<String, Object> serverList = new LinkedHashMap<>();
        serverList.put("count-bots", true);
        defaults.put("server-list", serverList);

        Map<String, Object> chat = new LinkedHashMap<>();
        chat.put("enabled", true);
        chat.put("interval-min", 30);
        chat.put("interval-max", 120);
        chat.put("global-chat", false);
        defaults.put("chat", chat);

        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("enabled", false);
        ai.put("provider", "openai");
        Map<String, Object> openai = new LinkedHashMap<>();
        openai.put("api-key", "");
        openai.put("model", "gpt-4o-mini");
        ai.put("openai", openai);
        Map<String, Object> anthropic = new LinkedHashMap<>();
        anthropic.put("api-key", "");
        anthropic.put("model", "claude-3-haiku-20240307");
        ai.put("anthropic", anthropic);
        Map<String, Object> gemini = new LinkedHashMap<>();
        gemini.put("api-key", "");
        gemini.put("model", "gemini-1.5-flash");
        ai.put("gemini", gemini);
        Map<String, Object> groq = new LinkedHashMap<>();
        groq.put("api-key", "");
        groq.put("model", "llama3-8b-8192");
        ai.put("groq", groq);
        Map<String, Object> ollama = new LinkedHashMap<>();
        ollama.put("url", "http://localhost:11434");
        ollama.put("model", "llama3");
        ai.put("ollama", ollama);
        defaults.put("ai", ai);

        Map<String, Object> database = new LinkedHashMap<>();
        database.put("enabled", false);
        database.put("mode", "sqlite");
        defaults.put("database", database);

        Map<String, Object> pathfinding = new LinkedHashMap<>();
        pathfinding.put("parkour", false);
        pathfinding.put("break-blocks", false);
        pathfinding.put("place-blocks", false);
        defaults.put("pathfinding", pathfinding);

        Map<String, Object> attack = new LinkedHashMap<>();
        attack.put("mob-default-range", 16.0);
        attack.put("mob-default-priority", "closest");
        defaults.put("attack", attack);

        Map<String, Object> autoEat = new LinkedHashMap<>();
        autoEat.put("enabled", true);
        defaults.put("auto-eat", autoEat);

        Map<String, Object> autoPlaceBed = new LinkedHashMap<>();
        autoPlaceBed.put("enabled", true);
        defaults.put("auto-place-bed", autoPlaceBed);

        Map<String, Object> swim = new LinkedHashMap<>();
        swim.put("enabled", true);
        defaults.put("swim-ai", swim);

        Map<String, Object> peakHours = new LinkedHashMap<>();
        peakHours.put("enabled", false);
        peakHours.put("timezone", "UTC");
        peakHours.put("schedules", new ArrayList<>());
        defaults.put("peak-hours", peakHours);

        Map<String, Object> swap = new LinkedHashMap<>();
        swap.put("enabled", false);
        swap.put("interval-min", 300);
        swap.put("interval-max", 600);
        defaults.put("swap", swap);

        Map<String, Object> badwordFilter = new LinkedHashMap<>();
        badwordFilter.put("enabled", false);
        badwordFilter.put("use-global-list", false);
        defaults.put("badword-filter", badwordFilter);

        data = defaults;
        save();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            Yaml yaml = new Yaml();
            yaml.dump(data, writer);
        } catch (IOException e) {
            FppLogger.error("Failed to save config: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Object getPath(String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    public String getString(String path, String def) {
        Object val = getPath(path);
        return val != null ? val.toString() : def;
    }

    public int getInt(String path, int def) {
        Object val = getPath(path);
        if (val instanceof Number) return ((Number) val).intValue();
        return def;
    }

    public double getDouble(String path, double def) {
        Object val = getPath(path);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return def;
    }

    public boolean getBoolean(String path, boolean def) {
        Object val = getPath(path);
        if (val instanceof Boolean) return (Boolean) val;
        return def;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object val = getPath(path);
        if (val instanceof List) {
            return ((List<?>) val).stream().map(Object::toString).toList();
        }
        return new ArrayList<>();
    }

    // Convenience methods matching the original Config class
    public boolean isDebug() { return getBoolean("debug", false); }
    public String getLanguage() { return getString("language", "en"); }
    public int maxBots() { return getInt("limits.max-bots", 1000); }
    public int userBotLimit() { return getInt("limits.user-bot-limit", 1); }
    public int spawnCooldown() { return Math.max(0, getInt("spawn-cooldown", 0)); }
    public boolean persistenceEnabled() { return getBoolean("persistence.enabled", true); }
    public boolean tabListEnabled() { return getBoolean("tab-list.enabled", true); }
    public boolean serverListCountBots() { return getBoolean("server-list.count-bots", true); }
    public boolean databaseEnabled() { return getBoolean("database.enabled", false); }
    public String databaseMode() { return getString("database.mode", "sqlite"); }
    public boolean mysqlEnabled() { return "mysql".equalsIgnoreCase(databaseMode()); }
    public String serverId() { return getString("database.server-id", "default"); }
    public boolean chatEnabled() { return getBoolean("chat.enabled", true); }
    public int chatIntervalMin() { return getInt("chat.interval-min", 30); }
    public int chatIntervalMax() { return getInt("chat.interval-max", 120); }
    public boolean globalChat() { return getBoolean("chat.global-chat", false); }
    public boolean aiEnabled() { return getBoolean("ai.enabled", false); }
    public String aiProvider() { return getString("ai.provider", "openai"); }
    public String skinMode() { return getString("skin.mode", "player").toLowerCase(); }
    public boolean skinClearCacheOnReload() { return getBoolean("skin.clear-cache-on-reload", true); }
    public boolean guaranteedSkin() { return getBoolean("skin.guaranteed-skin", true); }
    public String botNameMode() { return getString("bot-name.mode", "random").toLowerCase(); }
    public String adminBotNameFormat() { return getString("bot-name.admin-format", "{bot_name}"); }
    public String userBotNameFormat() { return getString("bot-name.user-format", "bot-{spawner}-{num}"); }
    public double maxHealth() { return getDouble("body.max-health", 20.0); }
    public boolean bodyEnabled() { return getBoolean("body.enabled", true); }
    public boolean bodyPickUpItems() { return getBoolean("body.pick-up-items", false); }
    public boolean bodyPickUpXp() { return getBoolean("body.pick-up-xp", true); }
    public boolean bodyRespawn() { return getBoolean("body.respawn", true); }
    public int bodyRespawnDelay() { return getInt("body.respawn-delay", 60); }
    public boolean pathfindingParkour() { return getBoolean("pathfinding.parkour", false); }
    public boolean pathfindingBreakBlocks() { return getBoolean("pathfinding.break-blocks", false); }
    public boolean pathfindingPlaceBlocks() { return getBoolean("pathfinding.place-blocks", false); }
    public double attackMobDefaultRange() { return getDouble("attack.mob-default-range", 16.0); }
    public String attackMobDefaultPriority() { return getString("attack.mob-default-priority", "closest"); }
    public boolean autoEatEnabled() { return getBoolean("auto-eat.enabled", true); }
    public boolean autoPlaceBedEnabled() { return getBoolean("auto-place-bed.enabled", true); }
    public boolean swimAiEnabled() { return getBoolean("swim-ai.enabled", true); }
    public boolean peakHoursEnabled() { return getBoolean("peak-hours.enabled", false); }
    public boolean swapEnabled() { return getBoolean("swap.enabled", false); }
    public int swapIntervalMin() { return getInt("swap.interval-min", 300); }
    public int swapIntervalMax() { return getInt("swap.interval-max", 600); }
    public boolean isBadwordFilterEnabled() { return getBoolean("badword-filter.enabled", false); }
    public int joinDelayMin() { return getInt("join-delay.min", 0); }
    public int joinDelayMax() { return getInt("join-delay.max", 1); }
    public int leaveDelayMin() { return getInt("leave-delay.min", 0); }
    public int leaveDelayMax() { return getInt("leave-delay.max", 1); }
    public boolean isNetworkMode() { return getBoolean("network.enabled", false); }
    public String configSyncMode() { return getString("network.config-sync", "none"); }

    // AI provider configs
    public String openaiApiKey() { return getString("ai.openai.api-key", ""); }
    public String openaiModel() { return getString("ai.openai.model", "gpt-4o-mini"); }
    public String anthropicApiKey() { return getString("ai.anthropic.api-key", ""); }
    public String anthropicModel() { return getString("ai.anthropic.model", "claude-3-haiku-20240307"); }
    public String geminiApiKey() { return getString("ai.gemini.api-key", ""); }
    public String geminiModel() { return getString("ai.gemini.model", "gemini-1.5-flash"); }
    public String groqApiKey() { return getString("ai.groq.api-key", ""); }
    public String groqModel() { return getString("ai.groq.model", "llama3-8b-8192"); }
    public String ollamaUrl() { return getString("ai.ollama.url", "http://localhost:11434"); }
    public String ollamaModel() { return getString("ai.ollama.model", "llama3"); }

    // MySQL configs
    public String mysqlHost() { return getString("database.mysql.host", "localhost"); }
    public int mysqlPort() { return getInt("database.mysql.port", 3306); }
    public String mysqlDatabase() { return getString("database.mysql.database", "fpp"); }
    public String mysqlUsername() { return getString("database.mysql.username", "root"); }
    public String mysqlPassword() { return getString("database.mysql.password", ""); }
}
