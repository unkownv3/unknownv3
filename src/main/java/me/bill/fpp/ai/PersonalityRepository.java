package me.bill.fpp.ai;

import me.bill.fpp.util.FppLogger;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PersonalityRepository {
    private final File configDir;
    private final Map<String, String> personalities = new LinkedHashMap<>();

    public PersonalityRepository(File configDir) {
        this.configDir = configDir;
    }

    public void load() {
        File dir = new File(configDir, "personalities");
        if (!dir.exists()) {
            dir.mkdirs();
            extractDefault(dir);
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File file : files) {
                try {
                    String name = file.getName().replace(".txt", "");
                    String content = Files.readString(file.toPath());
                    personalities.put(name.toLowerCase(), content);
                } catch (IOException e) {
                    FppLogger.error("Failed to load personality " + file.getName() + ": " + e.getMessage());
                }
            }
        }
        FppLogger.info("Loaded " + personalities.size() + " bot personalities.");
    }

    private void extractDefault(File dir) {
        String[] defaults = {"default", "friendly", "grumpy", "silent", "noob", "builder", "miner", "farmer", "explorer"};
        for (String name : defaults) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("personalities/" + name + ".txt")) {
                if (is != null) {
                    Files.copy(is, new File(dir, name + ".txt").toPath());
                }
            } catch (IOException e) {
                // Create minimal default
                try {
                    Files.writeString(new File(dir, name + ".txt").toPath(),
                        "You are a Minecraft player named {bot_name}. Respond in character as a " + name + " player.");
                } catch (IOException ignored) {}
            }
        }
    }

    public String getPrompt(String personality, String botName) {
        String template = personalities.getOrDefault(personality.toLowerCase(),
            personalities.getOrDefault("default",
                "You are a Minecraft player named {bot_name}. Keep responses short and in character."));
        return template.replace("{bot_name}", botName);
    }

    public Set<String> getPersonalityNames() {
        return personalities.keySet();
    }

    public void reload() {
        personalities.clear();
        load();
    }
}
