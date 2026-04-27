package me.bill.fpp.ai;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AIProvider {
    String getName();
    CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemPrompt);

    record ChatMessage(String role, String content) {}
}
