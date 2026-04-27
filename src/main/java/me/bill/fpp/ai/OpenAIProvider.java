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

public class OpenAIProvider implements AIProvider {
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final HttpClient client;

    public OpenAIProvider(String apiKey, String model) {
        this(apiKey, model, "https://api.openai.com/v1");
    }

    public OpenAIProvider(String apiKey, String model, String baseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public String getName() { return "openai"; }

    @Override
    public CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("model", model);
                body.addProperty("max_tokens", 150);
                body.addProperty("temperature", 0.8);

                JsonArray messages = new JsonArray();
                if (systemPrompt != null) {
                    JsonObject sys = new JsonObject();
                    sys.addProperty("role", "system");
                    sys.addProperty("content", systemPrompt);
                    messages.add(sys);
                }
                for (ChatMessage msg : history) {
                    JsonObject m = new JsonObject();
                    m.addProperty("role", msg.role());
                    m.addProperty("content", msg.content());
                    messages.add(m);
                }
                body.add("messages", messages);

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
                    return json.getAsJsonArray("choices").get(0).getAsJsonObject()
                        .getAsJsonObject("message").get("content").getAsString().trim();
                }
                FppLogger.debug("OpenAI API error: " + resp.statusCode() + " - " + resp.body());
            } catch (Exception e) {
                FppLogger.debug("OpenAI error: " + e.getMessage());
            }
            return null;
        });
    }
}
