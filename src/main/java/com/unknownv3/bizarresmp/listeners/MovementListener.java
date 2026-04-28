package com.unknownv3.bizarresmp.listeners;

import com.unknownv3.bizarresmp.BizarreSMP;
import com.unknownv3.bizarresmp.managers.AbilityManager;
import com.unknownv3.bizarresmp.managers.CooldownManager;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.trims.TrimType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MovementListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    public MovementListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
        this.abilityManager = plugin.getAbilityManager();
        this.cooldownManager = plugin.getCooldownManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        TrimType trim = trimManager.getPlayerTrim(player);
        if (trim == null) return;

        switch (trim) {
            case BOLT -> handleBoltPassive(player);
            case FLOW -> handleFlowDoubleJump(player);
            case COAST -> handleWaterwalking(player, event);
            case SENTRY -> handleHeroPassive(player);
            case SPIRE -> handleElytraPassive(player);
            case DUNE -> handleBurrowMovement(player);
            case SILENCE -> handleSilencePassive(player);
            case VEX -> handleVexGlideReady(player);
            default -> {}
        }
    }

    // BOLT: Overclock - Passive speed boost
    private void handleBoltPassive(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false, true));
        }
    }

    // FLOW: Double Jump
    private void handleFlowDoubleJump(Player player) {
        if (player.isOnGround()) {
            if (!abilityManager.isDoubleJumpReady(player.getUniqueId())) {
                abilityManager.setDoubleJumpReady(player.getUniqueId(), true);
                player.setAllowFlight(true);
            }
        }
    }

    // VEX: Enable flight toggle on ground so Allay Glide can trigger mid-air
    private void handleVexGlideReady(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isOnGround() && !player.getAllowFlight()) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        TrimType trim = trimManager.getPlayerTrim(player);

        // FLOW: Double Jump
        if (trim == TrimType.FLOW && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            if (abilityManager.isDoubleJumpReady(player.getUniqueId())) {
                event.setCancelled(true);
                abilityManager.setDoubleJumpReady(player.getUniqueId(), false);
                player.setAllowFlight(false);

                Vector velocity = player.getLocation().getDirection().normalize().multiply(0.5);
                velocity.setY(0.8);
                player.setVelocity(velocity);

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1.0f, 1.2f);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.1, 0.3, 0.05);
            }
        }

        // VEX: Allay Glide - Press space in air to glide
        if (trim == TrimType.VEX && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            if (!player.isOnGround()) {
                event.setCancelled(true);
                player.setAllowFlight(false);
                player.setGliding(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 5, 0.3, 0.3, 0.3, 0.02);
            }
        }
    }

    // COAST: Waterwalking
    private void handleWaterwalking(Player player, PlayerMoveEvent event) {
        if (player.isSneaking()) return; // Sneak to sink

        Block below = player.getLocation().subtract(0, 0.1, 0).getBlock();
        Block standOn = player.getLocation().getBlock();

        if (below.getType() == Material.WATER || standOn.getType() == Material.WATER) {
            if (!player.isSneaking()) {
                Vector velocity = player.getVelocity();
                if (velocity.getY() < 0) {
                    velocity.setY(0.1);
                    player.setVelocity(velocity);
                }
                if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, true));
                }
                player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 3, 0.3, 0, 0.3, 0);
            }
        }
    }

    // SENTRY: Hero of the Village 10
    private void handleHeroPassive(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 9, false, false, true));
        }
    }

    // SPIRE: Elytra Equipped - Always able to use elytra
    private void handleElytraPassive(Player player) {
        if (!player.isGliding() && !player.isOnGround() && player.isSneaking()) {
            player.setGliding(true);
        }
    }

    // DUNE: Fast movement on shovel-breakable blocks
    private void handleBurrowMovement(Player player) {
        if (abilityManager.isBurrowed(player.getUniqueId())) {
            return; // Already has speed from burrowing
        }
        Block below = player.getLocation().subtract(0, 1, 0).getBlock();
        if (isShovelBlock(below.getType())) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, true));
            }
        }
    }

    // SILENCE: Unnoticeable - No vibrations
    private void handleSilencePassive(Player player) {
        // Apply permanent wool-walking effect (no vibrations)
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            // Keep the player silent - handled in other events
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getY() >= event.getTo().getY()) return;

        TrimType trim = trimManager.getPlayerTrim(player);
        if (trim == TrimType.DUNE && abilityManager.isBurrowed(player.getUniqueId())) {
            // Emerge from burrow on jump
            UseAbilityListener useAbilityListener = new UseAbilityListener(plugin);
            useAbilityListener.emergeBurrow(player);
        }
    }

    private boolean isShovelBlock(Material material) {
        return material == Material.DIRT || material == Material.GRASS_BLOCK
                || material == Material.SAND || material == Material.GRAVEL
                || material == Material.SOUL_SAND || material == Material.SOUL_SOIL
                || material == Material.CLAY || material == Material.FARMLAND
                || material == Material.DIRT_PATH || material == Material.COARSE_DIRT
                || material == Material.PODZOL || material == Material.MYCELIUM
                || material == Material.RED_SAND || material == Material.SNOW_BLOCK
                || material == Material.SNOW || material == Material.MUD
                || material == Material.MUDDY_MANGROVE_ROOTS;
    }
}
