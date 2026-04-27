package me.bill.fpp.ai;

import me.bill.fpp.config.FppConfig;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.util.FppLogger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BotConversationManager {
    private final AIProviderRegistry registry;
    private final PersonalityRepository personalities;
    private final FppConfig config;
    private final Map<UUID, List<AIProvider.ChatMessage>> conversationHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 20;

    public BotConversationManager(AIProviderRegistry registry, PersonalityRepository personalities, FppConfig config) {
        this.registry = registry;
        this.personalities = personalities;
        this.config = config;
    }

    public CompletableFuture<String> generateResponse(FakePlayer fp, String playerName, String message) {
        if (!config.aiEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        AIProvider provider = registry.getActiveProvider();
        if (provider == null) {
            return CompletableFuture.completedFuture(null);
        }

        List<AIProvider.ChatMessage> history = conversationHistory.computeIfAbsent(
            fp.getUuid(), k -> new ArrayList<>()
        );

        history.add(new AIProvider.ChatMessage("user", playerName + ": " + message));
        if (history.size() > MAX_HISTORY) {
            history.subList(0, history.size() - MAX_HISTORY).clear();
        }

        String personality = fp.getAiPersonality() != null ?
            fp.getAiPersonality() : "default";
        String systemPrompt = personalities.getPrompt(personality, fp.getName());

        return provider.generateResponse(history, systemPrompt).thenApply(response -> {
            if (response != null) {
                history.add(new AIProvider.ChatMessage("assistant", response));
            }
            return response;
        });
    }

    public void clearHistory(FakePlayer fp) {
        conversationHistory.remove(fp.getUuid());
    }

    public void clearAll() {
        conversationHistory.clear();
    }
}
