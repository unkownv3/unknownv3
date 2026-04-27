package me.bill.fpp.fakeplayer;

import me.bill.fpp.util.FppLogger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class BotPersistence {
    private final File configDir;
    private final FakePlayerManager manager;
    private final File persistFile;

    public BotPersistence(File configDir, FakePlayerManager manager) {
        this.configDir = configDir;
        this.manager = manager;
        this.persistFile = new File(configDir, "active-bots.yml");
    }

    public void saveBots(Collection<FakePlayer> bots) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (FakePlayer fp : bots) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", fp.getName());
            entry.put("uuid", fp.getUuid().toString());
            entry.put("display-name", fp.getDisplayName());
            entry.put("spawned-by", fp.getSpawnedBy());
            entry.put("spawned-by-uuid", fp.getSpawnedByUuid().toString());
            entry.put("world", fp.getLiveWorldName());
            Vec3d pos = fp.getLiveLocation();
            if (pos != null) {
                entry.put("x", pos.x);
                entry.put("y", pos.y);
                entry.put("z", pos.z);
            }
            if (fp.getPlayerEntity() != null) {
                entry.put("yaw", (double) fp.getPlayerEntity().getYaw());
                entry.put("pitch", (double) fp.getPlayerEntity().getPitch());
            }
            entry.put("frozen", fp.isFrozen());
            entry.put("chat-enabled", fp.isChatEnabled());
            entry.put("chat-tier", fp.getChatTier());
            entry.put("ai-personality", fp.getAiPersonality());
            entry.put("bot-type", fp.getBotType().name());
            entry.put("luckperms-group", fp.getLuckpermsGroup());
            entry.put("right-click-cmd", fp.getRightClickCommand());
            entry.put("pickup-items", fp.isPickUpItemsEnabled());
            entry.put("pickup-xp", fp.isPickUpXpEnabled());
            entry.put("head-ai", fp.isHeadAiEnabled());
            entry.put("nav-parkour", fp.isNavParkour());
            entry.put("nav-break", fp.isNavBreakBlocks());
            entry.put("nav-place", fp.isNavPlaceBlocks());
            entry.put("swim-ai", fp.isSwimAiEnabled());
            entry.put("auto-eat", fp.isAutoEatEnabled());
            if (fp.getResolvedSkin() != null && fp.getResolvedSkin().isValid()) {
                entry.put("skin-texture", fp.getResolvedSkin().texture());
                entry.put("skin-signature", fp.getResolvedSkin().signature());
            }
            list.add(entry);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("bots", list);

        try (FileWriter writer = new FileWriter(persistFile)) {
            new Yaml().dump(root, writer);
        } catch (IOException e) {
            FppLogger.error("Failed to save persistent bots: " + e.getMessage());
        }
        FppLogger.info("Saved " + list.size() + " bot(s) for persistence.");
    }

    @SuppressWarnings("unchecked")
    public void restoreBots(MinecraftServer server) {
        if (!persistFile.exists()) return;
        try (InputStream is = new FileInputStream(persistFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(is);
            if (root == null || !root.containsKey("bots")) return;
            List<Map<String, Object>> list = (List<Map<String, Object>>) root.get("bots");
            if (list == null) return;

            int restored = 0;
            for (Map<String, Object> entry : list) {
                try {
                    String name = (String) entry.get("name");
                    String worldName = (String) entry.getOrDefault("world", "minecraft:overworld");
                    double x = ((Number) entry.getOrDefault("x", 0.0)).doubleValue();
                    double y = ((Number) entry.getOrDefault("y", 64.0)).doubleValue();
                    double z = ((Number) entry.getOrDefault("z", 0.0)).doubleValue();
                    float yaw = ((Number) entry.getOrDefault("yaw", 0.0)).floatValue();
                    float pitch = ((Number) entry.getOrDefault("pitch", 0.0)).floatValue();
                    String spawnedBy = (String) entry.getOrDefault("spawned-by", "SYSTEM");
                    String spawnedByUuidStr = (String) entry.getOrDefault("spawned-by-uuid", new UUID(0, 0).toString());
                    UUID spawnedByUuid = UUID.fromString(spawnedByUuidStr);

                    ServerWorld world = server.getOverworld();
                    for (ServerWorld w : server.getWorlds()) {
                        if (w.getRegistryKey().getValue().toString().equals(worldName)) {
                            world = w;
                            break;
                        }
                    }

                    FakePlayer fp = manager.spawnBot(name, world, new Vec3d(x, y, z), yaw, pitch, spawnedBy, spawnedByUuid);
                    if (fp != null) {
                        fp.setFrozen(getBool(entry, "frozen", false));
                        fp.setChatEnabled(getBool(entry, "chat-enabled", true));
                        fp.setChatTier((String) entry.get("chat-tier"));
                        fp.setAiPersonality((String) entry.get("ai-personality"));
                        fp.setRightClickCommand((String) entry.get("right-click-cmd"));
                        fp.setPickUpItemsEnabled(getBool(entry, "pickup-items", false));
                        fp.setPickUpXpEnabled(getBool(entry, "pickup-xp", true));
                        fp.setHeadAiEnabled(getBool(entry, "head-ai", true));
                        fp.setNavParkour(getBool(entry, "nav-parkour", false));
                        fp.setNavBreakBlocks(getBool(entry, "nav-break", false));
                        fp.setNavPlaceBlocks(getBool(entry, "nav-place", false));
                        fp.setSwimAiEnabled(getBool(entry, "swim-ai", true));
                        fp.setAutoEatEnabled(getBool(entry, "auto-eat", true));

                        String tex = (String) entry.get("skin-texture");
                        String sig = (String) entry.get("skin-signature");
                        if (tex != null) fp.setResolvedSkin(new SkinProfile(tex, sig));

                        fp.setRestoredSpawn(true);
                        restored++;
                    }
                } catch (Exception e) {
                    FppLogger.error("Failed to restore bot: " + e.getMessage());
                }
            }
            FppLogger.info("Restored " + restored + " bot(s) from persistence.");
        } catch (Exception e) {
            FppLogger.error("Failed to load persistent bots: " + e.getMessage());
        }
    }

    private static boolean getBool(Map<String, Object> map, String key, boolean def) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        return def;
    }
}
