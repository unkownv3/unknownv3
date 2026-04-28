package me.bill.fakePlayerPlugin.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.bill.fakePlayerPlugin.fakeplayer.SkinProfile;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinFetcher {
    private static final String MOJANG_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Map<String, SkinProfile> cache = new ConcurrentHashMap<>();

    private SkinFetcher() {}

    public static CompletableFuture<SkinProfile> fetchSkin(String playerName) {
        SkinProfile cached = cache.get(playerName.toLowerCase());
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = resolveUUID(playerName);
                if (uuid == null) return null;

                HttpURLConnection conn = (HttpURLConnection) URI.create(SESSION_API + uuid + "?unsigned=false").toURL().openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() != 200) return null;

                JsonObject json;
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                }

                var properties = json.getAsJsonArray("properties");
                if (properties == null || properties.isEmpty()) return null;

                JsonObject textures = properties.get(0).getAsJsonObject();
                String value = textures.get("value").getAsString();
                String signature = textures.has("signature") ? textures.get("signature").getAsString() : null;

                SkinProfile profile = new SkinProfile(value, signature);
                cache.put(playerName.toLowerCase(), profile);
                return profile;
            } catch (Exception e) {
                FppLogger.debug("Failed to fetch skin for " + playerName + ": " + e.getMessage());
                return null;
            }
        });
    }

    private static String resolveUUID(String playerName) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(MOJANG_API + playerName).toURL().openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) return null;

            JsonObject json;
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }

            return json.get("id").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public static void clearCache() {
        cache.clear();
    }
}
