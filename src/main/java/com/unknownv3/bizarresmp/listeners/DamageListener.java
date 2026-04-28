package com.unknownv3.bizarresmp.listeners;

import com.unknownv3.bizarresmp.BizarreSMP;
import com.unknownv3.bizarresmp.managers.AbilityManager;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.trims.TrimType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;
    private final AbilityManager abilityManager;

    public DamageListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
        this.abilityManager = plugin.getAbilityManager();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        TrimType trim = trimManager.getPlayerTrim(player);
        if (trim == null) return;

        switch (trim) {
            case RIB -> handleRibDamage(player, event);
            case FLOW -> handleFlowDamage(player, event);
            case SPIRE -> handleSpireDamage(player, event);
            case TIDE -> handleTideDamage(player, event);
            case WILD -> handleWildDamage(player, event);
            case VEX -> handleVexDamage(player, event);
            default -> {}
        }
    }

    // RIB: Flaming Spirit - Soul fire heals, Roast Resistant - No fire/lava damage
    private void handleRibDamage(Player player, EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            event.setCancelled(true);
            // Heal from soul fire (any fire heals for simplicity)
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(maxHealth, player.getHealth() + 1.0));
        }
    }

    // FLOW: Projectile Immunity
    private void handleFlowDamage(Player player, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            event.setCancelled(true);
        }
    }

    // SPIRE: Elytra Equipped - No kinetic damage
    private void handleSpireDamage(Player player, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
            event.setCancelled(true);
        }
    }

    // TIDE: Splash Landing - No fall damage
    private void handleTideDamage(Player player, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    // WILD: Jungle Immunity - Immune to debuff effects
    private void handleWildDamage(Player player, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.POISON
                || event.getCause() == EntityDamageEvent.DamageCause.WITHER) {
            event.setCancelled(true);
        }
    }

    // VEX: Low HP Allay heal
    private void handleVexDamage(Player player, EntityDamageEvent event) {
        double healthAfter = player.getHealth() - event.getFinalDamage();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (healthAfter <= maxHealth * 0.3) {
            // Heal from allay companion
            org.bukkit.entity.Entity allay = abilityManager.getAllayCompanion(player.getUniqueId());
            if (allay == null || allay.isDead()) {
                allay = player.getWorld().spawn(player.getLocation().add(0, 2, 0), org.bukkit.entity.Allay.class);
                abilityManager.setAllayCompanion(player.getUniqueId(), allay);
            }
            // Schedule heal for after damage is applied
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline() && !player.isDead()) {
                    player.setHealth(Math.min(maxHealth, player.getHealth() + 4.0));
                    player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0);
                }
            });
        }
    }
}
