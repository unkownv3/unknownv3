package me.bill.fpp.ai;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CopilotProvider extends OpenAIProvider {
    public CopilotProvider(String apiKey, String model, String baseUrl) {
        super(apiKey, model, baseUrl);
    }

    @Override
    public String getName() { return "copilot"; }
}
