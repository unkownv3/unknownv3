package me.bill.fpp.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.bill.fpp.util.FppLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoogleGeminiProvider implements AIProvider {
    private final String apiKey;
    private final String model;
    private final HttpClient client;

    public GoogleGeminiProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public String getName() { return "gemini"; }

    @Override
    public CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject body = new JsonObject();
                JsonArray contents = new JsonArray();
                if (systemPrompt != null) {
                    JsonObject sys = new JsonObject();
                    sys.addProperty("role", "user");
                    JsonArray parts = new JsonArray();
                    JsonObject part = new JsonObject();
                    part.addProperty("text", "System: " + systemPrompt);
                    parts.add(part);
                    sys.add("parts", parts);
                    contents.add(sys);
                }
                for (ChatMessage msg : history) {
                    JsonObject m = new JsonObject();
                    m.addProperty("role", "user".equals(msg.role()) ? "user" : "model");
                    JsonArray parts = new JsonArray();
                    JsonObject part = new JsonObject();
                    part.addProperty("text", msg.content());
                    parts.add(part);
                    m.add("parts", parts);
                    contents.add(m);
                }
                body.add("contents", contents);

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
                    return json.getAsJsonArray("candidates").get(0).getAsJsonObject()
                        .getAsJsonObject("content").getAsJsonArray("parts").get(0)
                        .getAsJsonObject().get("text").getAsString().trim();
                }
                FppLogger.debug("Gemini API error: " + resp.statusCode());
            } catch (Exception e) {
                FppLogger.debug("Gemini error: " + e.getMessage());
            }
            return null;
        });
    }
}
