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

public class AnthropicProvider implements AIProvider {
    private final String apiKey;
    private final String model;
    private final HttpClient client;

    public AnthropicProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public String getName() { return "anthropic"; }

    @Override
    public CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("model", model);
                body.addProperty("max_tokens", 150);
                if (systemPrompt != null) body.addProperty("system", systemPrompt);

                JsonArray messages = new JsonArray();
                for (ChatMessage msg : history) {
                    JsonObject m = new JsonObject();
                    m.addProperty("role", msg.role());
                    m.addProperty("content", msg.content());
                    messages.add(m);
                }
                body.add("messages", messages);

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anthropic.com/v1/messages"))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
                    return json.getAsJsonArray("content").get(0).getAsJsonObject()
                        .get("text").getAsString().trim();
                }
                FppLogger.debug("Anthropic API error: " + resp.statusCode());
            } catch (Exception e) {
                FppLogger.debug("Anthropic error: " + e.getMessage());
            }
            return null;
        });
    }
}
