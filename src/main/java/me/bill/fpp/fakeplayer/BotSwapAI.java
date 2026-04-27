package me.bill.fpp.fakeplayer;

import me.bill.fpp.config.FppConfig;
import me.bill.fpp.util.FppLogger;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BotSwapAI {
    private final FakePlayerManager manager;
    private final FppConfig config;
    private long lastSwapTick = 0;
    private long nextSwapInterval;

    public BotSwapAI(FakePlayerManager manager, FppConfig config) {
        this.manager = manager;
        this.config = config;
        this.nextSwapInterval = randomInterval();
    }

    public void tick() {
        if (!config.swapEnabled()) return;
        lastSwapTick++;
        if (lastSwapTick >= nextSwapInterval) {
            performSwap();
            lastSwapTick = 0;
            nextSwapInterval = randomInterval();
        }
    }

    private void performSwap() {
        List<FakePlayer> bots = new ArrayList<>(manager.getAllBots());
        if (bots.size() < 2) return;

        int idx1 = ThreadLocalRandom.current().nextInt(bots.size());
        int idx2;
        do {
            idx2 = ThreadLocalRandom.current().nextInt(bots.size());
        } while (idx2 == idx1);

        FakePlayer bot1 = bots.get(idx1);
        FakePlayer bot2 = bots.get(idx2);

        if (bot1.getPlayerEntity() == null || bot2.getPlayerEntity() == null) return;

        Vec3d pos1 = bot1.getPlayerEntity().getPos();
        Vec3d pos2 = bot2.getPlayerEntity().getPos();
        ServerWorld world1 = bot1.getPlayerEntity().getServerWorld();
        ServerWorld world2 = bot2.getPlayerEntity().getServerWorld();
        float yaw1 = bot1.getPlayerEntity().getYaw();
        float pitch1 = bot1.getPlayerEntity().getPitch();
        float yaw2 = bot2.getPlayerEntity().getYaw();
        float pitch2 = bot2.getPlayerEntity().getPitch();

        manager.teleportBot(bot1, world2, pos2, yaw2, pitch2);
        manager.teleportBot(bot2, world1, pos1, yaw1, pitch1);

        FppLogger.debug("Swapped bots: " + bot1.getName() + " <-> " + bot2.getName());
    }

    private long randomInterval() {
        return ThreadLocalRandom.current().nextLong(
            config.swapIntervalMin() * 20L,
            config.swapIntervalMax() * 20L + 1
        );
    }
}
