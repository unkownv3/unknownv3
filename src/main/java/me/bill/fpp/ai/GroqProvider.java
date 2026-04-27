package me.bill.fpp.ai;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GroqProvider extends OpenAIProvider {
    public GroqProvider(String apiKey, String model) {
        super(apiKey, model, "https://api.groq.com/openai/v1");
    }

    @Override
    public String getName() { return "groq"; }
}
