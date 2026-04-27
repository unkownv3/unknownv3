package me.bill.fpp.fakeplayer;

import me.bill.fpp.database.DatabaseManager;
import me.bill.fpp.util.FppLogger;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BotIdentityCache {
    private final DatabaseManager db;
    private final File configDir;
    private final Map<String, UUID> nameToUuid = new ConcurrentHashMap<>();
    private final Map<UUID, String> uuidToName = new ConcurrentHashMap<>();

    public BotIdentityCache(DatabaseManager db, File configDir) {
        this.db = db;
        this.configDir = configDir;
    }

    public UUID getOrCreateUuid(String name) {
        return nameToUuid.computeIfAbsent(name.toLowerCase(), n -> {
            UUID uuid = UUID.nameUUIDFromBytes(("FPP:" + name).getBytes());
            uuidToName.put(uuid, name);
            return uuid;
        });
    }

    public String getName(UUID uuid) {
        return uuidToName.get(uuid);
    }

    public void put(String name, UUID uuid) {
        nameToUuid.put(name.toLowerCase(), uuid);
        uuidToName.put(uuid, name);
    }

    public void remove(String name) {
        UUID uuid = nameToUuid.remove(name.toLowerCase());
        if (uuid != null) uuidToName.remove(uuid);
    }
}
