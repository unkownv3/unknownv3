package me.bill.fakePlayerPlugin.fakeplayer;

import com.mojang.authlib.GameProfile;
import me.bill.fakePlayerPlugin.config.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FakePlayer {
    private final String name;
    private final GameProfile profile;
    private final UUID uuid;
    private Vec3 spawnPosition;
    private ServerLevel spawnWorld;
    private ServerPlayer serverPlayer;
    private String spawnedBy = "UNKNOWN";
    private UUID spawnedByUuid = new UUID(0L, 0L);
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
    private boolean headAiEnabled = Config.bodyHeadAi();
    private boolean pickUpItemsEnabled = Config.bodyPickUpItems();
    private boolean pickUpXpEnabled = Config.bodyPickUpXp();
    private boolean navParkour = Config.pathfindingParkour();
    private boolean navBreakBlocks = Config.pathfindingBreakBlocks();
    private boolean navPlaceBlocks = Config.pathfindingPlaceBlocks();
    private boolean navAvoidWater = false;
    private boolean navAvoidLava = false;
    private boolean swimAiEnabled = Config.swimAiEnabled();
    private int chunkLoadRadius = -1;
    private double pveRange = Config.attackMobDefaultRange();
    private String pvePriority = Config.attackMobDefaultPriority();
    private final Set<String> pveMobTypes = new LinkedHashSet<>();
    private final Set<UUID> sharedControllers = ConcurrentHashMap.newKeySet();
    private boolean autoEatEnabled = Config.autoEatEnabled();
    private boolean autoPlaceBedEnabled = Config.autoPlaceBedEnabled();
    private volatile String nameTagNick = null;
    private int ping = -1;
    private volatile boolean tabListDirty = true;
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();

    public FakePlayer(UUID uuid, String name, GameProfile profile) {
        this.uuid = uuid;
        this.name = name;
        this.profile = profile;
    }

    public String getName() { return name; }
    public GameProfile getProfile() { return profile; }
    public UUID getUuid() { return uuid; }
    public Vec3 getSpawnPosition() { return spawnPosition; }
    public void setSpawnPosition(Vec3 pos) { this.spawnPosition = pos; }
    public ServerLevel getSpawnWorld() { return spawnWorld; }
    public void setSpawnWorld(ServerLevel world) { this.spawnWorld = world; }
    public ServerPlayer getServerPlayer() { return serverPlayer; }
    public void setServerPlayer(ServerPlayer player) { this.serverPlayer = player; }
    public String getSpawnedBy() { return spawnedBy; }
    public void setSpawnedBy(String spawnedBy) { this.spawnedBy = spawnedBy; }
    public UUID getSpawnedByUuid() { return spawnedByUuid; }
    public void setSpawnedByUuid(UUID uuid) { this.spawnedByUuid = uuid; }
    public Instant getSpawnTime() { return spawnTime; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRawDisplayName() { return rawDisplayName; }
    public void setRawDisplayName(String name) { this.rawDisplayName = name; }
    public String getSkinName() { return skinName; }
    public void setSkinName(String skinName) { this.skinName = skinName; }
    public SkinProfile getResolvedSkin() { return resolvedSkin; }
    public void setResolvedSkin(SkinProfile skin) { this.resolvedSkin = skin; }
    public double getTotalDamageTaken() { return totalDamageTaken; }
    public void addDamageTaken(double dmg) { this.totalDamageTaken += dmg; }
    public int getDeathCount() { return deathCount; }
    public void incrementDeathCount() { this.deathCount++; }
    public int getLastChunkX() { return lastChunkX; }
    public void setLastChunkX(int x) { this.lastChunkX = x; }
    public int getLastChunkZ() { return lastChunkZ; }
    public void setLastChunkZ(int z) { this.lastChunkZ = z; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public boolean isRespawning() { return respawning; }
    public void setRespawning(boolean respawning) { this.respawning = respawning; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
    public String getLastKnownWorld() { return lastKnownWorld; }
    public void setLastKnownWorld(String world) { this.lastKnownWorld = world; }
    public boolean isBodyless() { return bodyless; }
    public void setBodyless(boolean bodyless) { this.bodyless = bodyless; }
    public boolean isRestoredSpawn() { return restoredSpawn; }
    public void setRestoredSpawn(boolean restored) { this.restoredSpawn = restored; }
    public String getLuckpermsGroup() { return luckpermsGroup; }
    public void setLuckpermsGroup(String group) { this.luckpermsGroup = group; }
    public BotType getBotType() { return botType; }
    public void setBotType(BotType type) { this.botType = type; }
    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean enabled) { this.chatEnabled = enabled; }
    public String getChatTier() { return chatTier; }
    public void setChatTier(String tier) { this.chatTier = tier; }
    public String getAiPersonality() { return aiPersonality; }
    public void setAiPersonality(String personality) { this.aiPersonality = personality; }
    public String getRightClickCommand() { return rightClickCommand; }
    public void setRightClickCommand(String cmd) { this.rightClickCommand = cmd; }
    public boolean isHeadAiEnabled() { return headAiEnabled; }
    public void setHeadAiEnabled(boolean enabled) { this.headAiEnabled = enabled; }
    public boolean isPickUpItemsEnabled() { return pickUpItemsEnabled; }
    public void setPickUpItemsEnabled(boolean enabled) { this.pickUpItemsEnabled = enabled; }
    public boolean isPickUpXpEnabled() { return pickUpXpEnabled; }
    public void setPickUpXpEnabled(boolean enabled) { this.pickUpXpEnabled = enabled; }
    public boolean isNavParkour() { return navParkour; }
    public void setNavParkour(boolean parkour) { this.navParkour = parkour; }
    public boolean isNavBreakBlocks() { return navBreakBlocks; }
    public void setNavBreakBlocks(boolean breakBlocks) { this.navBreakBlocks = breakBlocks; }
    public boolean isNavPlaceBlocks() { return navPlaceBlocks; }
    public void setNavPlaceBlocks(boolean placeBlocks) { this.navPlaceBlocks = placeBlocks; }
    public boolean isNavAvoidWater() { return navAvoidWater; }
    public void setNavAvoidWater(boolean avoid) { this.navAvoidWater = avoid; }
    public boolean isNavAvoidLava() { return navAvoidLava; }
    public void setNavAvoidLava(boolean avoid) { this.navAvoidLava = avoid; }
    public boolean isSwimAiEnabled() { return swimAiEnabled; }
    public void setSwimAiEnabled(boolean enabled) { this.swimAiEnabled = enabled; }
    public int getChunkLoadRadius() { return chunkLoadRadius; }
    public void setChunkLoadRadius(int radius) { this.chunkLoadRadius = radius; }
    public double getPveRange() { return pveRange; }
    public void setPveRange(double range) { this.pveRange = range; }
    public String getPvePriority() { return pvePriority; }
    public void setPvePriority(String priority) { this.pvePriority = priority; }
    public Set<String> getPveMobTypes() { return pveMobTypes; }
    public Set<UUID> getSharedControllers() { return sharedControllers; }
    public boolean isAutoEatEnabled() { return autoEatEnabled; }
    public void setAutoEatEnabled(boolean enabled) { this.autoEatEnabled = enabled; }
    public boolean isAutoPlaceBedEnabled() { return autoPlaceBedEnabled; }
    public void setAutoPlaceBedEnabled(boolean enabled) { this.autoPlaceBedEnabled = enabled; }
    public String getNameTagNick() { return nameTagNick; }
    public void setNameTagNick(String nick) { this.nameTagNick = nick; }
    public int getPing() { return ping; }
    public void setPing(int ping) { this.ping = ping; }
    public boolean isTabListDirty() { return tabListDirty; }
    public void setTabListDirty(boolean dirty) { this.tabListDirty = dirty; }
    public Map<String, Object> getMetadata() { return metadata; }
}
