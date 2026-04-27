package me.bill.fpp.fakeplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.database.BotRecord;
import me.bill.fpp.database.DatabaseManager;
import me.bill.fpp.util.FppLogger;
import me.bill.fpp.util.RandomNameGenerator;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class FakePlayerManager {

    private final MinecraftServer server;
    private final Map<UUID, FakePlayer> bots = new ConcurrentHashMap<>();
    private final Map<String, FakePlayer> botsByName = new ConcurrentHashMap<>();
    private final Set<UUID> fakePlayerUuids = ConcurrentHashMap.newKeySet();

    private DatabaseManager databaseManager;
    private BotPersistence botPersistence;
    private BotIdentityCache identityCache;
    private ChunkLoader chunkLoader;

    private final List<String> cleanNamePool = new CopyOnWriteArrayList<>();
    private int tickCounter = 0;

    public FakePlayerManager(MinecraftServer server) {
        this.server = server;
    }

    public void setDatabaseManager(DatabaseManager dm) { this.databaseManager = dm; }
    public void setIdentityCache(BotIdentityCache ic) { this.identityCache = ic; }
    public void setBotPersistence(BotPersistence bp) { this.botPersistence = bp; }
    public void setChunkLoader(ChunkLoader cl) { this.chunkLoader = cl; }

    public boolean isFakePlayer(UUID uuid) {
        return fakePlayerUuids.contains(uuid);
    }

    public boolean isFakePlayer(ServerPlayerEntity player) {
        return player != null && fakePlayerUuids.contains(player.getUuid());
    }

    public FakePlayer getBot(UUID uuid) {
        return bots.get(uuid);
    }

    public FakePlayer getBot(String name) {
        return botsByName.get(name.toLowerCase());
    }

    public Collection<FakePlayer> getAllBots() {
        return Collections.unmodifiableCollection(bots.values());
    }

    public int getBotCount() {
        return bots.size();
    }

    public void refreshCleanNamePool() {
        var nameConfig = FakePlayerMod.getInstance().getBotNameConfig();
        if (nameConfig != null) {
            cleanNamePool.clear();
            cleanNamePool.addAll(nameConfig.getNames());
        }
    }

    public FakePlayer spawnBot(String name, ServerWorld world, Vec3d pos, float yaw, float pitch,
                                String spawnerName, UUID spawnerUuid) {
        var config = FakePlayerMod.getInstance().getConfig();
        if (bots.size() >= config.maxBots() && config.maxBots() > 0) {
            return null;
        }

        if (name == null || name.isEmpty()) {
            name = generateBotName();
        }

        if (name.length() > 16) {
            name = name.substring(0, 16);
        }

        if (botsByName.containsKey(name.toLowerCase())) {
            return null;
        }

        UUID uuid = UUID.nameUUIDFromBytes(("FPP:" + name).getBytes());
        FakePlayer fp = new FakePlayer(uuid, name);
        fp.setSpawnLocation(pos);
        fp.setSpawnWorldName(world.getRegistryKey().getValue().toString());
        fp.setSpawnedBy(spawnerName);
        fp.setSpawnedByUuid(spawnerUuid);

        // Resolve skin
        SkinProfile skin = resolveSkin(fp);
        fp.setResolvedSkin(skin);

        // Create the ServerPlayerEntity
        ServerPlayerEntity playerEntity = createFakePlayerEntity(fp, world, pos, yaw, pitch);
        if (playerEntity == null) {
            FppLogger.error("Failed to create ServerPlayerEntity for bot " + name);
            return null;
        }

        fp.setPlayerEntity(playerEntity);
        fp.setSpawnTime(Instant.now());

        bots.put(uuid, fp);
        botsByName.put(name.toLowerCase(), fp);
        fakePlayerUuids.add(uuid);

        // Add to player list
        server.getPlayerManager().getPlayerList().add(playerEntity);

        // Notify all players
        broadcastBotJoin(playerEntity);

        // Database record
        if (databaseManager != null) {
            BotRecord record = new BotRecord(
                name, fp.getDisplayName(), uuid.toString(),
                spawnerName, spawnerUuid.toString(),
                world.getRegistryKey().getValue().toString(),
                pos.x, pos.y, pos.z, yaw, pitch
            );
            databaseManager.insertSession(record);
            fp.setDbRecord(record);
        }

        FppLogger.info("Bot spawned: " + name + " at " + String.format("%.1f, %.1f, %.1f", pos.x, pos.y, pos.z));
        return fp;
    }

    private ServerPlayerEntity createFakePlayerEntity(FakePlayer fp, ServerWorld world,
                                                       Vec3d pos, float yaw, float pitch) {
        try {
            GameProfile profile = new GameProfile(fp.getUuid(), fp.getName());

            // Apply skin
            if (fp.getResolvedSkin() != null && fp.getResolvedSkin().isValid()) {
                profile.getProperties().put("textures",
                    new Property("textures", fp.getResolvedSkin().texture(),
                        fp.getResolvedSkin().signature()));
            }

            ServerPlayerEntity playerEntity = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());
            playerEntity.setPosition(pos.x, pos.y, pos.z);
            playerEntity.setYaw(yaw);
            playerEntity.setPitch(pitch);
            playerEntity.changeGameMode(GameMode.SURVIVAL);

            // Set max health
            var config = FakePlayerMod.getInstance().getConfig();
            double maxHp = config.maxHealth();
            playerEntity.setHealth((float) maxHp);

            return playerEntity;
        } catch (Exception e) {
            FppLogger.error("Failed to create fake player entity: " + e.getMessage(), e);
            return null;
        }
    }

    public boolean despawnBot(FakePlayer fp, String reason) {
        if (fp == null) return false;

        ServerPlayerEntity entity = fp.getPlayerEntity();
        if (entity != null) {
            broadcastBotLeave(entity);
            server.getPlayerManager().getPlayerList().remove(entity);
            entity.discard();
        }

        bots.remove(fp.getUuid());
        botsByName.remove(fp.getName().toLowerCase());
        fakePlayerUuids.remove(fp.getUuid());
        fp.setAlive(false);
        fp.setPlayerEntity(null);

        if (databaseManager != null) {
            databaseManager.markSessionRemoved(fp.getUuid().toString(), reason);
        }

        FppLogger.info("Bot despawned: " + fp.getName() + " (" + reason + ")");
        return true;
    }

    public void despawnAll(String reason) {
        List<FakePlayer> all = new ArrayList<>(bots.values());
        for (FakePlayer fp : all) {
            despawnBot(fp, reason);
        }
    }

    public void tick(MinecraftServer srv) {
        tickCounter++;
        if (tickCounter % 20 == 0) {
            for (FakePlayer fp : bots.values()) {
                tickBot(fp);
            }
        }
    }

    private void tickBot(FakePlayer fp) {
        if (fp.getPlayerEntity() == null || !fp.isAlive()) return;
        ServerPlayerEntity entity = fp.getPlayerEntity();

        // Head look AI - look at nearest player
        if (fp.isHeadAiEnabled()) {
            lookAtNearestPlayer(fp);
        }

        // Auto eat
        if (fp.isAutoEatEnabled() && entity.getHungerManager().getFoodLevel() < 14) {
            tryAutoEat(fp);
        }

        // Update last known world
        fp.setLastKnownWorld(entity.getWorld().getRegistryKey().getValue().toString());
    }

    private void lookAtNearestPlayer(FakePlayer fp) {
        ServerPlayerEntity entity = fp.getPlayerEntity();
        if (entity == null) return;

        ServerPlayerEntity nearest = null;
        double nearestDist = 32.0 * 32.0;

        for (ServerPlayerEntity p : entity.getServerWorld().getPlayers()) {
            if (fakePlayerUuids.contains(p.getUuid())) continue;
            double dist = p.squaredDistanceTo(entity);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }

        if (nearest != null) {
            entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, nearest.getEyePos());
        }
    }

    private void tryAutoEat(FakePlayer fp) {
        // Simplified auto-eat: find food in inventory and consume
        ServerPlayerEntity entity = fp.getPlayerEntity();
        if (entity == null) return;

        for (int i = 0; i < entity.getInventory().size(); i++) {
            var stack = entity.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getComponents().get(DataComponentTypes.FOOD) != null) {
                entity.getHungerManager().setFoodLevel(20);
                stack.decrement(1);
                break;
            }
        }
    }

    private void broadcastBotJoin(ServerPlayerEntity entity) {
        // The entity is already added to the player list, which handles broadcasting
    }

    private void broadcastBotLeave(ServerPlayerEntity entity) {
        // Handled by player manager removal
    }

    private SkinProfile resolveSkin(FakePlayer fp) {
        SkinManager skinManager = FakePlayerMod.getInstance().getSkinManager();
        if (skinManager != null) {
            return skinManager.resolveSkin(fp.getName());
        }
        return null;
    }

    private String generateBotName() {
        var config = FakePlayerMod.getInstance().getConfig();
        if ("pool".equals(config.botNameMode()) && !cleanNamePool.isEmpty()) {
            for (String name : cleanNamePool) {
                if (!botsByName.containsKey(name.toLowerCase())) {
                    return name;
                }
            }
        }
        return RandomNameGenerator.generate();
    }

    public List<FakePlayer> getBotsBy(UUID spawnerUuid) {
        return bots.values().stream()
            .filter(fp -> fp.getSpawnedByUuid().equals(spawnerUuid))
            .collect(Collectors.toList());
    }

    public List<FakePlayer> getBotsInWorld(String worldName) {
        return bots.values().stream()
            .filter(fp -> worldName.equals(fp.getLiveWorldName()))
            .collect(Collectors.toList());
    }

    public void teleportBot(FakePlayer fp, ServerWorld world, Vec3d pos, float yaw, float pitch) {
        ServerPlayerEntity entity = fp.getPlayerEntity();
        if (entity == null) return;
        entity.teleport(world, pos.x, pos.y, pos.z, Set.of(), yaw, pitch, true);
    }

    public void lookAt(FakePlayer fp, ServerPlayerEntity target) {
        if (fp.getPlayerEntity() != null && target != null) {
            fp.getPlayerEntity().lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos());
        }
    }

    public void respawnBot(FakePlayer fp) {
        if (fp.isRespawning() || !fp.isAlive()) return;
        fp.setRespawning(true);

        var config = FakePlayerMod.getInstance().getConfig();
        int delay = config.bodyRespawnDelay() * 20;

        server.execute(() -> {
            if (fp.getSpawnLocation() != null) {
                ServerWorld world = server.getOverworld();
                for (ServerWorld w : server.getWorlds()) {
                    if (w.getRegistryKey().getValue().toString().equals(fp.getSpawnWorldName())) {
                        world = w;
                        break;
                    }
                }
                ServerPlayerEntity newEntity = createFakePlayerEntity(fp, world,
                    fp.getSpawnLocation(), 0, 0);
                if (newEntity != null) {
                    fp.setPlayerEntity(newEntity);
                    fp.setAlive(true);
                    fp.setRespawning(false);
                    server.getPlayerManager().getPlayerList().add(newEntity);
                    FppLogger.info("Bot respawned: " + fp.getName());
                }
            }
        });
    }
}
