package me.bill.fpp.fakeplayer;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkLoader {
    private final FakePlayerManager manager;
    private final Map<UUID, Set<ChunkPos>> loadedChunks = new ConcurrentHashMap<>();

    public ChunkLoader(FakePlayerManager manager) {
        this.manager = manager;
    }

    public void updateChunkLoading(FakePlayer fp) {
        if (fp.getPlayerEntity() == null || fp.getChunkLoadRadius() < 0) return;

        ServerWorld world = fp.getPlayerEntity().getServerWorld();
        ChunkPos center = fp.getPlayerEntity().getChunkPos();
        int radius = fp.getChunkLoadRadius();

        Set<ChunkPos> newChunks = ConcurrentHashMap.newKeySet();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos cp = new ChunkPos(center.x + dx, center.z + dz);
                newChunks.add(cp);
                world.getChunkManager().addTicket(
                    net.minecraft.server.world.ChunkTicketType.PLAYER,
                    cp, radius, cp
                );
            }
        }

        Set<ChunkPos> old = loadedChunks.put(fp.getUuid(), newChunks);
        if (old != null) {
            for (ChunkPos cp : old) {
                if (!newChunks.contains(cp)) {
                    world.getChunkManager().removeTicket(
                        net.minecraft.server.world.ChunkTicketType.PLAYER,
                        cp, radius, cp
                    );
                }
            }
        }
    }

    public void unloadAll(FakePlayer fp) {
        Set<ChunkPos> chunks = loadedChunks.remove(fp.getUuid());
        if (chunks != null && fp.getPlayerEntity() != null) {
            ServerWorld world = fp.getPlayerEntity().getServerWorld();
            for (ChunkPos cp : chunks) {
                world.getChunkManager().removeTicket(
                    net.minecraft.server.world.ChunkTicketType.PLAYER,
                    cp, fp.getChunkLoadRadius(), cp
                );
            }
        }
    }
}
