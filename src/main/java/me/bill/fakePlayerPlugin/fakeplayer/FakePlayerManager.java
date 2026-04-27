package me.bill.fakePlayerPlugin.fakeplayer;

import com.mojang.authlib.GameProfile;
import me.bill.fakePlayerPlugin.FakePlayerPluginFabric;
import me.bill.fakePlayerPlugin.config.Config;
import me.bill.fakePlayerPlugin.fakeplayer.network.FakeConnection;
import me.bill.fakePlayerPlugin.fakeplayer.network.FakeServerGamePacketListenerImpl;
import me.bill.fakePlayerPlugin.util.FppLogger;
import me.bill.fakePlayerPlugin.util.SkinFetcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class FakePlayerManager {
    private static final Map<UUID, FakePlayer> fakePlayers = new ConcurrentHashMap<>();
    private static final Map<String, FakePlayer> fakePlayersByName = new ConcurrentHashMap<>();
    private static final Map<ServerPlayer, FakePlayer> serverPlayerMap = new ConcurrentHashMap<>();

    private FakePlayerManager() {}

    public static Collection<FakePlayer> getAll() {
        return Collections.unmodifiableCollection(fakePlayers.values());
    }

    public static int count() {
        return fakePlayers.size();
    }

    public static FakePlayer getByName(String name) {
        return fakePlayersByName.get(name.toLowerCase(Locale.ROOT));
    }

    public static FakePlayer getByUuid(UUID uuid) {
        return fakePlayers.get(uuid);
    }

    public static FakePlayer getByServerPlayer(ServerPlayer player) {
        return serverPlayerMap.get(player);
    }

    public static boolean isFakePlayer(ServerPlayer player) {
        return serverPlayerMap.containsKey(player);
    }

    public static boolean isFakePlayer(UUID uuid) {
        return fakePlayers.containsKey(uuid);
    }

    public static FakePlayer spawn(String name, ServerLevel world, Vec3 position,
                                    String spawnerName, UUID spawnerUuid, SkinProfile skin) {
        MinecraftServer server = FakePlayerPluginFabric.getServer();
        if (server == null) {
            FppLogger.error("Cannot spawn bot - server not available");
            return null;
        }

        if (count() >= Config.maxBots()) {
            FppLogger.warn("Bot limit reached (" + Config.maxBots() + ")");
            return null;
        }

        if (getByName(name) != null) {
            FppLogger.warn("Bot with name '" + name + "' already exists");
            return null;
        }

        UUID uuid = UUIDUtil.createOfflinePlayerUUID(name);
        GameProfile profile = new GameProfile(uuid, name);

        if (skin != null && skin.isValid()) {
            profile.getProperties().put("textures",
                new com.mojang.authlib.properties.Property("textures", skin.value(), skin.signature()));
        }

        FakePlayer fakePlayer = new FakePlayer(uuid, name, profile);
        fakePlayer.setSpawnPosition(position);
        fakePlayer.setSpawnWorld(world);
        fakePlayer.setSpawnedBy(spawnerName);
        fakePlayer.setSpawnedByUuid(spawnerUuid);

        try {
            ServerPlayer serverPlayer = new ServerPlayer(server, world, profile, ClientInformation.createDefault());

            FakeConnection fakeConnection = new FakeConnection(InetAddress.getLoopbackAddress());
            CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
            FakeServerGamePacketListenerImpl listener = new FakeServerGamePacketListenerImpl(
                server, fakeConnection, serverPlayer, cookie
            );

            serverPlayer.setPos(position.x, position.y, position.z);

            PlayerList playerList = server.getPlayerList();
            playerList.placeNewPlayer(fakeConnection, serverPlayer, cookie);

            if (Config.bodyEnabled()) {
                serverPlayer.setGameMode(GameType.SURVIVAL);
            } else {
                serverPlayer.setGameMode(GameType.CREATIVE);
            }

            if (Config.bodyInvulnerable()) {
                serverPlayer.setInvulnerable(true);
            }

            fakePlayer.setServerPlayer(serverPlayer);
            fakePlayer.setAlive(true);
            fakePlayer.setLastKnownWorld(world.dimension().location().toString());

            fakePlayers.put(uuid, fakePlayer);
            fakePlayersByName.put(name.toLowerCase(Locale.ROOT), fakePlayer);
            serverPlayerMap.put(serverPlayer, fakePlayer);

            FppLogger.info("Spawned bot: " + name + " at " +
                String.format("%.1f, %.1f, %.1f", position.x, position.y, position.z) +
                " in " + world.dimension().location());

            return fakePlayer;

        } catch (Exception e) {
            FppLogger.error("Failed to spawn bot '" + name + "': " + e.getMessage());
            FppLogger.error("Stack trace:", e);
            return null;
        }
    }

    public static boolean despawn(FakePlayer fakePlayer) {
        if (fakePlayer == null) return false;

        ServerPlayer serverPlayer = fakePlayer.getServerPlayer();
        if (serverPlayer != null) {
            try {
                MinecraftServer server = FakePlayerPluginFabric.getServer();
                if (server != null) {
                    server.getPlayerList().remove(serverPlayer);
                }
                serverPlayerMap.remove(serverPlayer);
            } catch (Exception e) {
                FppLogger.error("Error despawning bot: " + e.getMessage());
            }
        }

        fakePlayers.remove(fakePlayer.getUuid());
        fakePlayersByName.remove(fakePlayer.getName().toLowerCase(Locale.ROOT));
        fakePlayer.setAlive(false);

        FppLogger.info("Despawned bot: " + fakePlayer.getName());
        return true;
    }

    public static int despawnAll() {
        int count = fakePlayers.size();
        List<FakePlayer> toRemove = new ArrayList<>(fakePlayers.values());
        for (FakePlayer fp : toRemove) {
            despawn(fp);
        }
        return count;
    }

    public static void tick() {
        for (FakePlayer fp : fakePlayers.values()) {
            if (!fp.isAlive() || fp.isFrozen()) continue;
            ServerPlayer sp = fp.getServerPlayer();
            if (sp == null || sp.isRemoved()) {
                fp.setAlive(false);
                continue;
            }
            tickBot(fp, sp);
        }
    }

    private static void tickBot(FakePlayer fp, ServerPlayer sp) {
        // Head AI - look at nearest player
        if (fp.isHeadAiEnabled()) {
            ServerPlayer nearest = findNearestRealPlayer(sp, 32.0);
            if (nearest != null) {
                lookAt(sp, nearest, 10.0f, 10.0f);
            }
        }

        // Update chunk tracking
        int chunkX = sp.getBlockX() >> 4;
        int chunkZ = sp.getBlockZ() >> 4;
        if (chunkX != fp.getLastChunkX() || chunkZ != fp.getLastChunkZ()) {
            fp.setLastChunkX(chunkX);
            fp.setLastChunkZ(chunkZ);
        }
    }

    private static ServerPlayer findNearestRealPlayer(ServerPlayer bot, double range) {
        ServerLevel level = bot.serverLevel();
        double rangeSq = range * range;
        ServerPlayer nearest = null;
        double nearestDist = rangeSq;

        for (ServerPlayer player : level.players()) {
            if (isFakePlayer(player)) continue;
            double dist = player.distanceToSqr(bot);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    public static void lookAt(ServerPlayer bot, ServerPlayer target, float maxYawChange, float maxPitchChange) {
        double dx = target.getX() - bot.getX();
        double dy = (target.getY() + target.getEyeHeight()) - (bot.getY() + bot.getEyeHeight());
        double dz = target.getZ() - bot.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float targetPitch = (float) -(Math.atan2(dy, dist) * (180.0 / Math.PI));

        float yaw = rotateTowards(bot.getYRot(), targetYaw, maxYawChange);
        float pitch = rotateTowards(bot.getXRot(), targetPitch, maxPitchChange);

        bot.setYRot(yaw);
        bot.setXRot(pitch);
        bot.setYHeadRot(yaw);
    }

    private static float rotateTowards(float current, float target, float maxChange) {
        float diff = target - current;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        if (diff > maxChange) diff = maxChange;
        if (diff < -maxChange) diff = -maxChange;
        return current + diff;
    }
}
