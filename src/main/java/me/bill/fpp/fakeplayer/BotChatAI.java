package me.bill.fpp.fakeplayer;

import me.bill.fpp.ai.BotConversationManager;
import me.bill.fpp.config.FppConfig;
import me.bill.fpp.util.FppLogger;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BotChatAI {
    private final FakePlayerManager manager;
    private final BotConversationManager conversationManager;
    private final FppConfig config;
    private final Map<UUID, Long> lastChatTime = new ConcurrentHashMap<>();

    public BotChatAI(FakePlayerManager manager, BotConversationManager conversationManager, FppConfig config) {
        this.manager = manager;
        this.conversationManager = conversationManager;
        this.config = config;
    }

    public void onPlayerChat(ServerPlayerEntity player, String message) {
        if (manager.isFakePlayer(player)) return;
        if (!config.aiEnabled()) return;

        for (FakePlayer fp : manager.getAllBots()) {
            if (!fp.isChatEnabled() || !fp.isOnline()) continue;
            ServerPlayerEntity botEntity = fp.getPlayerEntity();
            if (botEntity == null) continue;

            double dist = botEntity.squaredDistanceTo(player);
            if (dist > 256) continue; // 16 blocks radius

            conversationManager.generateResponse(fp, player.getName().getString(), message)
                .thenAccept(response -> {
                    if (response != null && !response.isEmpty()) {
                        botEntity.getServer().execute(() -> {
                            botEntity.getServer().getPlayerManager().broadcast(
                                Text.literal("<" + fp.getDisplayName() + "> " + response), false
                            );
                        });
                    }
                });
        }
    }

    public void triggerRandomChat(FakePlayer fp) {
        if (!fp.isChatEnabled() || !fp.isOnline()) return;
        if (!config.chatEnabled()) return;

        long now = System.currentTimeMillis();
        Long last = lastChatTime.get(fp.getUuid());
        int minInterval = config.chatIntervalMin() * 1000;
        int maxInterval = config.chatIntervalMax() * 1000;
        int interval = ThreadLocalRandom.current().nextInt(minInterval, maxInterval + 1);

        if (last != null && now - last < interval) return;

        var messageConfig = me.bill.fpp.FakePlayerMod.getInstance().getBotMessageConfig();
        String msg = messageConfig.getRandom();
        ServerPlayerEntity entity = fp.getPlayerEntity();
        if (entity != null) {
            entity.getServer().getPlayerManager().broadcast(
                Text.literal("<" + fp.getDisplayName() + "> " + msg), false
            );
        }
        lastChatTime.put(fp.getUuid(), now);
    }
}
