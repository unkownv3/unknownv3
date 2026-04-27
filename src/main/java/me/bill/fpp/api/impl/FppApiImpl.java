package me.bill.fpp.api.impl;

import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.UUID;

public class FppApiImpl {
    private final FakePlayerManager manager;

    public FppApiImpl(FakePlayerManager manager) {
        this.manager = manager;
    }

    public FakePlayer spawnBot(String name, ServerWorld world, Vec3d pos) {
        return manager.spawnBot(name, world, pos, 0, 0, "API", new UUID(0, 0));
    }

    public boolean despawnBot(String name) {
        FakePlayer fp = manager.getBot(name);
        if (fp == null) return false;
        return manager.despawnBot(fp, "api");
    }

    public FakePlayer getBot(String name) {
        return manager.getBot(name);
    }

    public FakePlayer getBot(UUID uuid) {
        return manager.getBot(uuid);
    }

    public Collection<FakePlayer> getAllBots() {
        return manager.getAllBots();
    }

    public int getBotCount() {
        return manager.getBotCount();
    }

    public boolean isFakePlayer(UUID uuid) {
        return manager.isFakePlayer(uuid);
    }
}
