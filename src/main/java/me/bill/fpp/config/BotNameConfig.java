package me.bill.fpp.config;

import me.bill.fpp.util.FppLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class BotNameConfig {
    private final File configDir;
    private List<String> names = new ArrayList<>();

    public BotNameConfig(File configDir) {
        this.configDir = configDir;
    }

    public void load() {
        File file = new File(configDir, "bot-names.yml");
        if (!file.exists()) {
            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream("bot-names.yml");
                if (is != null) {
                    Files.copy(is, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                FppLogger.error("Failed to create bot-names.yml: " + e.getMessage());
            }
        }
        try (InputStream is = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(is);
            if (loaded instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) loaded;
                Object namesList = map.get("names");
                if (namesList instanceof List) {
                    names = ((List<?>) namesList).stream().map(Object::toString).toList();
                }
            } else if (loaded instanceof List) {
                names = ((List<?>) loaded).stream().map(Object::toString).toList();
            }
        } catch (Exception e) {
            FppLogger.error("Failed to load bot-names.yml: " + e.getMessage());
        }
    }

    public List<String> getNames() {
        return names;
    }

    public String getRandom() {
        if (names.isEmpty()) return null;
        return names.get(new Random().nextInt(names.size()));
    }
}
