package com.unknownv3.bizarresmp.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();
    private final Map<String, Map<UUID, Integer>> charges = new HashMap<>();
    private final Map<String, Map<UUID, Long>> chargeTimers = new HashMap<>();

    public boolean isOnCooldown(UUID playerId, String ability) {
        Map<UUID, Long> map = cooldowns.get(ability);
        if (map == null) return false;
        Long expiry = map.get(playerId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            map.remove(playerId);
            return false;
        }
        return true;
    }

    public long getRemainingCooldown(UUID playerId, String ability) {
        Map<UUID, Long> map = cooldowns.get(ability);
        if (map == null) return 0;
        Long expiry = map.get(playerId);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void setCooldown(UUID playerId, String ability, long durationMs) {
        cooldowns.computeIfAbsent(ability, k -> new HashMap<>())
                .put(playerId, System.currentTimeMillis() + durationMs);
    }

    public int getCharges(UUID playerId, String ability, int maxCharges, long rechargeTimeMs) {
        Map<UUID, Integer> chargeMap = charges.computeIfAbsent(ability, k -> new HashMap<>());
        Map<UUID, Long> timerMap = chargeTimers.computeIfAbsent(ability, k -> new HashMap<>());

        int currentCharges = chargeMap.getOrDefault(playerId, maxCharges);
        Long lastUse = timerMap.get(playerId);

        if (currentCharges < maxCharges && lastUse != null) {
            long elapsed = System.currentTimeMillis() - lastUse;
            int recharged = (int) (elapsed / rechargeTimeMs);
            if (recharged > 0) {
                currentCharges = Math.min(maxCharges, currentCharges + recharged);
                chargeMap.put(playerId, currentCharges);
                timerMap.put(playerId, System.currentTimeMillis());
            }
        }

        return currentCharges;
    }

    public void useCharge(UUID playerId, String ability, int maxCharges, long rechargeTimeMs) {
        Map<UUID, Integer> chargeMap = charges.computeIfAbsent(ability, k -> new HashMap<>());
        Map<UUID, Long> timerMap = chargeTimers.computeIfAbsent(ability, k -> new HashMap<>());

        int currentCharges = getCharges(playerId, ability, maxCharges, rechargeTimeMs);
        if (currentCharges > 0) {
            chargeMap.put(playerId, currentCharges - 1);
            timerMap.put(playerId, System.currentTimeMillis());
        }
    }

    public void clearPlayer(UUID playerId) {
        for (Map<UUID, Long> map : cooldowns.values()) {
            map.remove(playerId);
        }
        for (Map<UUID, Integer> map : charges.values()) {
            map.remove(playerId);
        }
        for (Map<UUID, Long> map : chargeTimers.values()) {
            map.remove(playerId);
        }
    }
}
