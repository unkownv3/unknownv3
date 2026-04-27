package me.bill.fpp.config;

import me.bill.fpp.util.FppLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class BotMessageConfig {
    private final File configDir;
    private List<String> messages = new ArrayList<>();

    public BotMessageConfig(File configDir) {
        this.configDir = configDir;
    }

    public void load() {
        File file = new File(configDir, "bot-messages.yml");
        if (!file.exists()) {
            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream("bot-messages.yml");
                if (is != null) {
                    Files.copy(is, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                FppLogger.error("Failed to create bot-messages.yml: " + e.getMessage());
            }
        }
        try (InputStream is = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(is);
            if (loaded instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) loaded;
                Object msgList = map.get("messages");
                if (msgList instanceof List) {
                    messages = ((List<?>) msgList).stream().map(Object::toString).toList();
                }
            } else if (loaded instanceof List) {
                messages = ((List<?>) loaded).stream().map(Object::toString).toList();
            }
        } catch (Exception e) {
            FppLogger.error("Failed to load bot-messages.yml: " + e.getMessage());
        }
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getRandom() {
        if (messages.isEmpty()) return "Hello!";
        return messages.get(new Random().nextInt(messages.size()));
    }
}
