package me.bill.fpp.ai;

import me.bill.fpp.config.FppConfig;
import me.bill.fpp.util.FppLogger;

import java.util.LinkedHashMap;
import java.util.Map;

public class AIProviderRegistry {
    private final Map<String, AIProvider> providers = new LinkedHashMap<>();
    private final FppConfig config;

    public AIProviderRegistry(FppConfig config) {
        this.config = config;
        registerDefaults();
    }

    private void registerDefaults() {
        String key;

        // OpenAI
        key = config.openaiApiKey();
        if (key != null && !key.isEmpty()) {
            providers.put("openai", new OpenAIProvider(key, config.openaiModel()));
        }

        // Anthropic
        key = config.anthropicApiKey();
        if (key != null && !key.isEmpty()) {
            providers.put("anthropic", new AnthropicProvider(key, config.anthropicModel()));
        }

        // Google Gemini
        key = config.geminiApiKey();
        if (key != null && !key.isEmpty()) {
            providers.put("gemini", new GoogleGeminiProvider(key, config.geminiModel()));
        }

        // Groq
        key = config.groqApiKey();
        if (key != null && !key.isEmpty()) {
            providers.put("groq", new GroqProvider(key, config.groqModel()));
        }

        // Ollama (no API key needed)
        providers.put("ollama", new OllamaProvider(config.ollamaUrl(), config.ollamaModel()));

        FppLogger.info("AI providers registered: " + providers.keySet());
    }

    public AIProvider getProvider(String name) {
        return providers.get(name.toLowerCase());
    }

    public AIProvider getActiveProvider() {
        String active = config.aiProvider();
        return providers.get(active.toLowerCase());
    }

    public Map<String, AIProvider> getAllProviders() {
        return providers;
    }

    public void register(String name, AIProvider provider) {
        providers.put(name.toLowerCase(), provider);
    }
}
