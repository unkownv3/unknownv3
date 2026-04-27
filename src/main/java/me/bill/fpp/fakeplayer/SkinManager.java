package me.bill.fpp.fakeplayer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.bill.fpp.util.FppLogger;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager {
    private final File configDir;
    private final Map<String, SkinProfile> cache = new ConcurrentHashMap<>();
    private final HttpClient httpClient;

    public SkinManager(File configDir) {
        this.configDir = configDir;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public SkinProfile resolveSkin(String playerName) {
        SkinProfile cached = cache.get(playerName.toLowerCase());
        if (cached != null) return cached;

        try {
            // Step 1: Get UUID from Mojang
            HttpRequest uuidReq = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + playerName))
                .timeout(Duration.ofSeconds(5))
                .GET().build();
            HttpResponse<String> uuidResp = httpClient.send(uuidReq, HttpResponse.BodyHandlers.ofString());
            if (uuidResp.statusCode() != 200) return null;

            JsonObject uuidJson = JsonParser.parseString(uuidResp.body()).getAsJsonObject();
            String uuid = uuidJson.get("id").getAsString();

            // Step 2: Get profile with skin from session server
            HttpRequest profileReq = HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false"))
                .timeout(Duration.ofSeconds(5))
                .GET().build();
            HttpResponse<String> profileResp = httpClient.send(profileReq, HttpResponse.BodyHandlers.ofString());
            if (profileResp.statusCode() != 200) return null;

            JsonObject profileJson = JsonParser.parseString(profileResp.body()).getAsJsonObject();
            for (JsonElement prop : profileJson.getAsJsonArray("properties")) {
                JsonObject propObj = prop.getAsJsonObject();
                if ("textures".equals(propObj.get("name").getAsString())) {
                    String texture = propObj.get("value").getAsString();
                    String signature = propObj.has("signature") ? propObj.get("signature").getAsString() : null;
                    SkinProfile skin = new SkinProfile(texture, signature);
                    cache.put(playerName.toLowerCase(), skin);
                    return skin;
                }
            }
        } catch (Exception e) {
            FppLogger.debug("Failed to resolve skin for " + playerName + ": " + e.getMessage());
        }
        return null;
    }

    public void clearCache() {
        cache.clear();
    }
}
