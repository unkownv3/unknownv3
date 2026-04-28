package com.unknownv3.bizarresmp.managers;

import com.unknownv3.bizarresmp.BizarreSMP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class AbilityManager {

    private final BizarreSMP plugin;

    // Silence: track warden blast hits
    private final Map<UUID, Map<UUID, Integer>> wardenBlastHits = new HashMap<>();
    private final Set<UUID> wardenTransformed = new HashSet<>();

    // Eye: track beam targets for ender eye tracking
    private final Map<UUID, UUID> eyeBeamTargets = new HashMap<>();
    private final Map<UUID, Long> eyeBeamTimestamps = new HashMap<>();

    // Rib: soul fireball charges
    // (handled via CooldownManager charges)

    // Flow: air bubble entities
    private final Map<UUID, Entity> airBubbles = new HashMap<>();
    private final Set<UUID> doubleJumpReady = new HashSet<>();

    // Spire: shadow clones
    private final Map<UUID, List<Entity>> shadowClones = new HashMap<>();

    // Host: disguise data
    private final Map<UUID, String> disguises = new HashMap<>();

    // Raiser: thrown bombs
    private final Map<UUID, Entity> thrownBombs = new HashMap<>();

    // Shaper: player scale
    private final Map<UUID, Double> playerScales = new HashMap<>();

    // Ward: sound power active
    private final Map<UUID, BukkitTask> soundPowerTasks = new HashMap<>();

    // Dune: burrowed players
    private final Set<UUID> burrowedPlayers = new HashSet<>();
    private final Map<UUID, Long> burrowTimestamps = new HashMap<>();

    // Wayfinder: cloaked players
    private final Set<UUID> cloakedPlayers = new HashSet<>();

    // Wild: vine hook targets
    private final Map<UUID, UUID> vineHookTargets = new HashMap<>();
    private final Map<UUID, Long> vineHookTimestamps = new HashMap<>();

    // Vex: fang trail active
    private final Set<UUID> fangTrailActive = new HashSet<>();
    private final Map<UUID, Entity> allayCompanions = new HashMap<>();

    // Sentry: ravager mounts
    private final Map<UUID, Entity> ravagerMounts = new HashMap<>();

    // Coast: waterwalking
    // (passive, detected in movement listener)

    // Bolt: thunderbolt charges tracked via CooldownManager

    private final List<BukkitTask> activeTasks = new ArrayList<>();

    public AbilityManager(BizarreSMP plugin) {
        this.plugin = plugin;
    }

    // ---- Silence ----
    public int addWardenBlastHit(UUID attacker, UUID target) {
        Map<UUID, Integer> hits = wardenBlastHits.computeIfAbsent(attacker, k -> new HashMap<>());
        int count = hits.getOrDefault(target, 0) + 1;
        hits.put(target, count);
        return count;
    }

    public void clearWardenBlastHits(UUID attacker, UUID target) {
        Map<UUID, Integer> hits = wardenBlastHits.get(attacker);
        if (hits != null) hits.remove(target);
    }

    public boolean isWardenTransformed(UUID playerId) {
        return wardenTransformed.contains(playerId);
    }

    public void setWardenTransformed(UUID playerId, boolean transformed) {
        if (transformed) wardenTransformed.add(playerId);
        else wardenTransformed.remove(playerId);
    }

    // ---- Eye ----
    public void setEyeBeamTarget(UUID shooter, UUID target) {
        eyeBeamTargets.put(shooter, target);
        eyeBeamTimestamps.put(shooter, System.currentTimeMillis());
    }

    public UUID getEyeBeamTarget(UUID shooter) {
        Long timestamp = eyeBeamTimestamps.get(shooter);
        if (timestamp == null) return null;
        if (System.currentTimeMillis() - timestamp > 300_000) {
            eyeBeamTargets.remove(shooter);
            eyeBeamTimestamps.remove(shooter);
            return null;
        }
        return eyeBeamTargets.get(shooter);
    }

    // ---- Flow ----
    public void setAirBubble(UUID playerId, Entity bubble) {
        airBubbles.put(playerId, bubble);
    }

    public Entity getAirBubble(UUID playerId) {
        return airBubbles.get(playerId);
    }

    public void removeAirBubble(UUID playerId) {
        Entity bubble = airBubbles.remove(playerId);
        if (bubble != null && !bubble.isDead()) bubble.remove();
    }

    public boolean isDoubleJumpReady(UUID playerId) {
        return doubleJumpReady.contains(playerId);
    }

    public void setDoubleJumpReady(UUID playerId, boolean ready) {
        if (ready) doubleJumpReady.add(playerId);
        else doubleJumpReady.remove(playerId);
    }

    // ---- Spire ----
    public void setShadowClones(UUID playerId, List<Entity> clones) {
        shadowClones.put(playerId, clones);
    }

    public void removeShadowClones(UUID playerId) {
        List<Entity> clones = shadowClones.remove(playerId);
        if (clones != null) {
            for (Entity clone : clones) {
                if (clone != null && !clone.isDead()) clone.remove();
            }
        }
    }

    // ---- Host ----
    public void setDisguise(UUID playerId, String targetName) {
        disguises.put(playerId, targetName);
    }

    public String getDisguise(UUID playerId) {
        return disguises.get(playerId);
    }

    public void removeDisguise(UUID playerId) {
        disguises.remove(playerId);
    }

    // ---- Raiser ----
    public void setThrownBomb(UUID playerId, Entity bomb) {
        thrownBombs.put(playerId, bomb);
    }

    public Entity getThrownBomb(UUID playerId) {
        return thrownBombs.get(playerId);
    }

    public void removeThrownBomb(UUID playerId) {
        Entity bomb = thrownBombs.remove(playerId);
        if (bomb != null && !bomb.isDead()) bomb.remove();
    }

    // ---- Shaper ----
    public double getPlayerScale(UUID playerId) {
        return playerScales.getOrDefault(playerId, 1.0);
    }

    public void setPlayerScale(UUID playerId, double scale) {
        playerScales.put(playerId, scale);
    }

    // ---- Ward ----
    public void setSoundPowerTask(UUID playerId, BukkitTask task) {
        BukkitTask old = soundPowerTasks.put(playerId, task);
        if (old != null) old.cancel();
    }

    public void removeSoundPowerTask(UUID playerId) {
        BukkitTask task = soundPowerTasks.remove(playerId);
        if (task != null) task.cancel();
    }

    // ---- Dune ----
    public boolean isBurrowed(UUID playerId) {
        return burrowedPlayers.contains(playerId);
    }

    public void setBurrowed(UUID playerId, boolean burrowed) {
        if (burrowed) {
            burrowedPlayers.add(playerId);
            burrowTimestamps.put(playerId, System.currentTimeMillis());
        } else {
            burrowedPlayers.remove(playerId);
            burrowTimestamps.remove(playerId);
        }
    }

    public long getBurrowTimestamp(UUID playerId) {
        return burrowTimestamps.getOrDefault(playerId, 0L);
    }

    // ---- Wayfinder ----
    public boolean isCloaked(UUID playerId) {
        return cloakedPlayers.contains(playerId);
    }

    public void setCloaked(UUID playerId, boolean cloaked) {
        if (cloaked) cloakedPlayers.add(playerId);
        else cloakedPlayers.remove(playerId);
    }

    // ---- Wild ----
    public void setVineHookTarget(UUID playerId, UUID targetId) {
        vineHookTargets.put(playerId, targetId);
        vineHookTimestamps.put(playerId, System.currentTimeMillis());
    }

    public UUID getVineHookTarget(UUID playerId) {
        Long timestamp = vineHookTimestamps.get(playerId);
        if (timestamp == null) return null;
        if (System.currentTimeMillis() - timestamp > 5000) {
            vineHookTargets.remove(playerId);
            vineHookTimestamps.remove(playerId);
            return null;
        }
        return vineHookTargets.get(playerId);
    }

    public void removeVineHook(UUID playerId) {
        vineHookTargets.remove(playerId);
        vineHookTimestamps.remove(playerId);
    }

    // ---- Vex ----
    public boolean isFangTrailActive(UUID playerId) {
        return fangTrailActive.contains(playerId);
    }

    public void setFangTrailActive(UUID playerId, boolean active) {
        if (active) fangTrailActive.add(playerId);
        else fangTrailActive.remove(playerId);
    }

    public void setAllayCompanion(UUID playerId, Entity allay) {
        allayCompanions.put(playerId, allay);
    }

    public Entity getAllayCompanion(UUID playerId) {
        return allayCompanions.get(playerId);
    }

    // ---- Sentry ----
    public void setRavagerMount(UUID playerId, Entity ravager) {
        ravagerMounts.put(playerId, ravager);
    }

    public Entity getRavagerMount(UUID playerId) {
        return ravagerMounts.get(playerId);
    }

    public void removeRavagerMount(UUID playerId) {
        Entity ravager = ravagerMounts.remove(playerId);
        if (ravager != null && !ravager.isDead()) ravager.remove();
    }

    // ---- Task Management ----
    public void addTask(BukkitTask task) {
        activeTasks.add(task);
    }

    public void cleanup() {
        for (BukkitTask task : activeTasks) {
            if (task != null) task.cancel();
        }
        activeTasks.clear();
        for (Entity bubble : airBubbles.values()) {
            if (bubble != null && !bubble.isDead()) bubble.remove();
        }
        airBubbles.clear();
        for (List<Entity> clones : shadowClones.values()) {
            if (clones != null) clones.forEach(e -> { if (e != null && !e.isDead()) e.remove(); });
        }
        shadowClones.clear();
        for (Entity bomb : thrownBombs.values()) {
            if (bomb != null && !bomb.isDead()) bomb.remove();
        }
        thrownBombs.clear();
        for (Entity ravager : ravagerMounts.values()) {
            if (ravager != null && !ravager.isDead()) ravager.remove();
        }
        ravagerMounts.clear();
        for (Entity allay : allayCompanions.values()) {
            if (allay != null && !allay.isDead()) allay.remove();
        }
        allayCompanions.clear();
        for (BukkitTask task : soundPowerTasks.values()) {
            if (task != null) task.cancel();
        }
        soundPowerTasks.clear();
    }

    public void clearPlayer(UUID playerId) {
        wardenBlastHits.remove(playerId);
        wardenTransformed.remove(playerId);
        eyeBeamTargets.remove(playerId);
        eyeBeamTimestamps.remove(playerId);
        removeAirBubble(playerId);
        doubleJumpReady.remove(playerId);
        removeShadowClones(playerId);
        disguises.remove(playerId);
        removeThrownBomb(playerId);
        playerScales.remove(playerId);
        removeSoundPowerTask(playerId);
        burrowedPlayers.remove(playerId);
        burrowTimestamps.remove(playerId);
        cloakedPlayers.remove(playerId);
        removeVineHook(playerId);
        fangTrailActive.remove(playerId);
        Entity allay = allayCompanions.remove(playerId);
        if (allay != null && !allay.isDead()) allay.remove();
        removeRavagerMount(playerId);
    }
}
