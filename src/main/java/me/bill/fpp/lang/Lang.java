package me.bill.fpp.lang;

import me.bill.fpp.util.FppLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class Lang {
    private static Map<String, String> messages = new LinkedHashMap<>();

    private Lang() {}

    public static void init(File configDir) {
        File langDir = new File(configDir, "lang");
        if (!langDir.exists()) langDir.mkdirs();

        File langFile = new File(langDir, "en.yml");
        if (!langFile.exists()) {
            try {
                InputStream is = Lang.class.getClassLoader().getResourceAsStream("lang/en.yml");
                if (is != null) {
                    Files.copy(is, langFile.toPath());
                } else {
                    createDefault(langFile);
                }
            } catch (IOException e) {
                FppLogger.error("Failed to create lang file: " + e.getMessage());
            }
        }

        load(langFile);
    }

    private static void load(File file) {
        try (InputStream is = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(is);
            if (loaded instanceof Map) {
                flattenMap("", (Map<String, Object>) loaded);
            }
        } catch (Exception e) {
            FppLogger.error("Failed to load lang: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void flattenMap(String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenMap(key, (Map<String, Object>) entry.getValue());
            } else {
                messages.put(key, String.valueOf(entry.getValue()));
            }
        }
    }

    private static void createDefault(File file) {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("prefix", "&b[FPP]&r ");
        defaults.put("no-permission", "&cYou don't have permission to do that.");
        defaults.put("player-only", "&cThis command can only be used by players.");
        defaults.put("bot-not-found", "&cBot not found: {name}");
        defaults.put("bot-spawned", "&aBot {name} spawned.");
        defaults.put("bot-despawned", "&aBot {name} despawned.");
        defaults.put("max-bots-reached", "&cMax bot limit reached ({max}).");
        defaults.put("reload-success", "&aConfiguration reloaded.");
        defaults.put("cooldown-active", "&cPlease wait {seconds}s before spawning.");

        try (FileWriter writer = new FileWriter(file)) {
            new Yaml().dump(defaults, writer);
        } catch (IOException ignored) {}
        flattenMap("", defaults);
    }

    public static String get(String key) {
        return messages.getOrDefault(key, key);
    }

    public static String get(String key, Object... placeholders) {
        String msg = get(key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }
        return msg.replace("&a", "§a").replace("&b", "§b").replace("&c", "§c")
            .replace("&d", "§d").replace("&e", "§e").replace("&f", "§f")
            .replace("&r", "§r").replace("&l", "§l").replace("&o", "§o")
            .replace("&7", "§7").replace("&8", "§8").replace("&6", "§6")
            .replace("&0", "§0").replace("&1", "§1").replace("&2", "§2")
            .replace("&3", "§3").replace("&4", "§4").replace("&5", "§5")
            .replace("&9", "§9");
    }
}
