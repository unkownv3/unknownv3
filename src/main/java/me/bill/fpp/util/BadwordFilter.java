package me.bill.fpp.util;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class BadwordFilter {
    private static Set<String> badwords = new HashSet<>();

    private BadwordFilter() {}

    public static void reload(File configDir) {
        File file = new File(configDir, "bad-words.yml");
        if (!file.exists()) {
            try {
                InputStream is = BadwordFilter.class.getClassLoader().getResourceAsStream("bad-words.yml");
                if (is != null) {
                    Files.copy(is, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException ignored) {}
        }

        try (InputStream is = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(is);
            if (loaded instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) loaded;
                Object wordsList = map.get("words");
                if (wordsList instanceof List) {
                    badwords = new HashSet<>();
                    for (Object w : (List<?>) wordsList) {
                        badwords.add(w.toString().toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            FppLogger.error("Failed to load bad-words.yml: " + e.getMessage());
        }
    }

    public static boolean containsBadWord(String text) {
        String lower = text.toLowerCase();
        for (String word : badwords) {
            if (lower.contains(word)) return true;
        }
        return false;
    }

    public static String filter(String text) {
        String result = text;
        for (String word : badwords) {
            result = result.replaceAll("(?i)" + java.util.regex.Pattern.quote(word),
                "*".repeat(word.length()));
        }
        return result;
    }
}
