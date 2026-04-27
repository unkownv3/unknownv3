package me.bill.fpp.fakeplayer;

import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.database.BotRecord;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FakePlayer {
    private final String name;
    private final UUID uuid;
    private Vec3d spawnLocation;
    private String spawnWorldName;
    private ServerPlayerEntity playerEntity;
    private String spawnedBy = "UNKNOWN";
    private UUID spawnedByUuid = new UUID(0L, 0L);
    private BotRecord dbRecord;
    private Instant spawnTime = Instant.now();
    private String displayName = null;
    private String rawDisplayName = null;
    private String skinName = null;
    private SkinProfile resolvedSkin = null;
    private double totalDamageTaken = 0.0;
    private int deathCount = 0;
    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;
    private boolean alive = true;
    private boolean respawning = false;
    private boolean frozen = false;
    private String lastKnownWorld = null;
    private boolean bodyless = false;
    private boolean restoredSpawn = false;
    private String luckpermsGroup = null;
    private BotType botType = BotType.AFK;
    private boolean chatEnabled = true;
    private String chatTier = null;
    private String aiPersonality = null;
    private String rightClickCommand = null;
    private boolean headAiEnabled = true;
    private boolean pickUpItemsEnabled;
    private boolean pickUpXpEnabled;
    private boolean navParkour;
    private boolean navBreakBlocks;
    private boolean navPlaceBlocks;
    private boolean navAvoidWater = false;
    private boolean navAvoidLava = false;
    private boolean swimAiEnabled;
    private int chunkLoadRadius = -1;
    private PveSmartAttackMode pveSmartAttackMode = PveSmartAttackMode.OFF;
    private double pveRange;
    private String pvePriority;
    private Set<String> pveMobTypes = new LinkedHashSet<>();
    private final Set<UUID> sharedControllers = ConcurrentHashMap.newKeySet();
    private boolean autoEatEnabled;
    private boolean autoPlaceBedEnabled;
    private int ping = -1;
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();
    private Vec3d sleepOrigin = null;
    private double sleepRadius = 0.0;
    private boolean sleeping = false;

    public enum PveSmartAttackMode { OFF, DEFENSIVE, AGGRESSIVE }

    public FakePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        var config = FakePlayerMod.getInstance().getConfig();
        this.pickUpItemsEnabled = config.bodyPickUpItems();
        this.pickUpXpEnabled = config.bodyPickUpXp();
        this.navParkour = config.pathfindingParkour();
        this.navBreakBlocks = config.pathfindingBreakBlocks();
        this.navPlaceBlocks = config.pathfindingPlaceBlocks();
        this.swimAiEnabled = config.swimAiEnabled();
        this.pveRange = config.attackMobDefaultRange();
        this.pvePriority = config.attackMobDefaultPriority();
        this.autoEatEnabled = config.autoEatEnabled();
        this.autoPlaceBedEnabled = config.autoPlaceBedEnabled();
    }

    // Getters and setters
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public Vec3d getSpawnLocation() { return spawnLocation; }
    public void setSpawnLocation(Vec3d loc) { this.spawnLocation = loc; }
    public String getSpawnWorldName() { return spawnWorldName; }
    public void setSpawnWorldName(String world) { this.spawnWorldName = world; }
    public ServerPlayerEntity getPlayerEntity() { return playerEntity; }
    public void setPlayerEntity(ServerPlayerEntity entity) { this.playerEntity = entity; }
    public int getEntityId() { return playerEntity != null ? playerEntity.getId() : -1; }
    public String getDisplayName() { return displayName != null ? displayName : name; }
    public void setDisplayName(String dn) { this.displayName = dn; }
    public String getRawDisplayName() { return rawDisplayName; }
    public void setRawDisplayName(String rdn) { this.rawDisplayName = rdn; }
    public String getSkinName() { return skinName != null ? skinName : name; }
    public void setSkinName(String sn) { this.skinName = sn; }
    public SkinProfile getResolvedSkin() { return resolvedSkin; }
    public void setResolvedSkin(SkinProfile skin) { this.resolvedSkin = skin; }
    public String getSpawnedBy() { return spawnedBy; }
    public void setSpawnedBy(String by) { this.spawnedBy = by; }
    public UUID getSpawnedByUuid() { return spawnedByUuid; }
    public void setSpawnedByUuid(UUID uuid) { this.spawnedByUuid = uuid; }
    public BotRecord getDbRecord() { return dbRecord; }
    public void setDbRecord(BotRecord rec) { this.dbRecord = rec; }
    public Instant getSpawnTime() { return spawnTime; }
    public void setSpawnTime(Instant t) { this.spawnTime = t; }
    public double getTotalDamageTaken() { return totalDamageTaken; }
    public void addDamageTaken(double d) { this.totalDamageTaken += d; }
    public int getDeathCount() { return deathCount; }
    public void incrementDeathCount() { this.deathCount++; }
    public int getLastChunkX() { return lastChunkX; }
    public void setLastChunkX(int x) { this.lastChunkX = x; }
    public int getLastChunkZ() { return lastChunkZ; }
    public void setLastChunkZ(int z) { this.lastChunkZ = z; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean a) { this.alive = a; }
    public boolean isRespawning() { return respawning; }
    public void setRespawning(boolean r) { this.respawning = r; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean f) { this.frozen = f; }
    public String getLastKnownWorld() { return lastKnownWorld; }
    public void setLastKnownWorld(String w) { this.lastKnownWorld = w; }
    public boolean isBodyless() { return bodyless; }
    public void setBodyless(boolean b) { this.bodyless = b; }
    public boolean isRestoredSpawn() { return restoredSpawn; }
    public void setRestoredSpawn(boolean r) { this.restoredSpawn = r; }
    public String getLuckpermsGroup() { return luckpermsGroup; }
    public void setLuckpermsGroup(String g) { this.luckpermsGroup = g; }
    public BotType getBotType() { return botType; }
    public void setBotType(BotType t) { this.botType = t; }
    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean e) { this.chatEnabled = e; }
    public String getChatTier() { return chatTier; }
    public void setChatTier(String t) { this.chatTier = t; }
    public String getAiPersonality() { return aiPersonality; }
    public void setAiPersonality(String p) { this.aiPersonality = p; }
    public String getRightClickCommand() { return rightClickCommand; }
    public void setRightClickCommand(String cmd) { this.rightClickCommand = cmd; }
    public boolean isHeadAiEnabled() { return headAiEnabled; }
    public void setHeadAiEnabled(boolean e) { this.headAiEnabled = e; }
    public boolean isPickUpItemsEnabled() { return pickUpItemsEnabled; }
    public void setPickUpItemsEnabled(boolean e) { this.pickUpItemsEnabled = e; }
    public boolean isPickUpXpEnabled() { return pickUpXpEnabled; }
    public void setPickUpXpEnabled(boolean e) { this.pickUpXpEnabled = e; }
    public boolean isNavParkour() { return navParkour; }
    public void setNavParkour(boolean p) { this.navParkour = p; }
    public boolean isNavBreakBlocks() { return navBreakBlocks; }
    public void setNavBreakBlocks(boolean b) { this.navBreakBlocks = b; }
    public boolean isNavPlaceBlocks() { return navPlaceBlocks; }
    public void setNavPlaceBlocks(boolean p) { this.navPlaceBlocks = p; }
    public boolean isNavAvoidWater() { return navAvoidWater; }
    public void setNavAvoidWater(boolean a) { this.navAvoidWater = a; }
    public boolean isNavAvoidLava() { return navAvoidLava; }
    public void setNavAvoidLava(boolean a) { this.navAvoidLava = a; }
    public boolean isSwimAiEnabled() { return swimAiEnabled; }
    public void setSwimAiEnabled(boolean e) { this.swimAiEnabled = e; }
    public int getChunkLoadRadius() { return chunkLoadRadius; }
    public void setChunkLoadRadius(int r) { this.chunkLoadRadius = r; }
    public PveSmartAttackMode getPveSmartAttackMode() { return pveSmartAttackMode; }
    public void setPveSmartAttackMode(PveSmartAttackMode m) { this.pveSmartAttackMode = m; }
    public double getPveRange() { return pveRange; }
    public void setPveRange(double r) { this.pveRange = r; }
    public String getPvePriority() { return pvePriority; }
    public void setPvePriority(String p) { this.pvePriority = p; }
    public Set<String> getPveMobTypes() { return pveMobTypes; }
    public void setPveMobTypes(Set<String> types) { this.pveMobTypes = types; }
    public Set<UUID> getSharedControllers() { return sharedControllers; }
    public boolean isAutoEatEnabled() { return autoEatEnabled; }
    public void setAutoEatEnabled(boolean e) { this.autoEatEnabled = e; }
    public boolean isAutoPlaceBedEnabled() { return autoPlaceBedEnabled; }
    public void setAutoPlaceBedEnabled(boolean e) { this.autoPlaceBedEnabled = e; }
    public int getPing() { return ping; }
    public void setPing(int p) { this.ping = p; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Vec3d getSleepOrigin() { return sleepOrigin; }
    public void setSleepOrigin(Vec3d o) { this.sleepOrigin = o; }
    public double getSleepRadius() { return sleepRadius; }
    public void setSleepRadius(double r) { this.sleepRadius = r; }
    public boolean isSleeping() { return sleeping; }
    public void setSleeping(boolean s) { this.sleeping = s; }

    public Vec3d getLiveLocation() {
        if (playerEntity != null) return playerEntity.getPos();
        return spawnLocation;
    }

    public String getLiveWorldName() {
        if (playerEntity != null) {
            return playerEntity.getWorld().getRegistryKey().getValue().toString();
        }
        return spawnWorldName != null ? spawnWorldName : "minecraft:overworld";
    }

    public boolean isOnline() {
        return playerEntity != null && alive;
    }
}
