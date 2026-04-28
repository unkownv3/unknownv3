package com.unknownv3.bizarresmp.listeners;

import com.unknownv3.bizarresmp.BizarreSMP;
import com.unknownv3.bizarresmp.managers.AbilityManager;
import com.unknownv3.bizarresmp.managers.CooldownManager;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.trims.TrimType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class UseAbilityListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;
    private final CooldownManager cooldownManager;
    private final AbilityManager abilityManager;

    public UseAbilityListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.abilityManager = plugin.getAbilityManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        TrimType trim = trimManager.getPlayerTrim(player);
        if (trim == null) return;

        // Don't trigger if holding certain items that have their own right-click
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!hand.getType().isAir() && isInteractableItem(hand.getType())) {
            // Allow some trims to override
            if (trim != TrimType.SNOUT && trim != TrimType.DUNE && trim != TrimType.HOST) {
                return;
            }
        }

        switch (trim) {
            case SILENCE -> handleSilence(player);
            case EYE -> handleEye(player);
            case SNOUT -> handleSnout(player);
            case RIB -> handleRib(player);
            case FLOW -> handleFlow(player);
            case SPIRE -> handleSpire(player);
            case BOLT -> handleBolt(player);
            case HOST -> handleHost(player);
            case RAISER -> handleRaiser(player);
            case SHAPER -> handleShaper(player);
            case TIDE -> handleTide(player);
            case WARD -> handleWard(player);
            case DUNE -> handleDune(player);
            case WAYFINDER -> handleWayfinder(player);
            case COAST -> handleCoast(player);
            case WILD -> handleWild(player);
            case VEX -> handleVex(player);
            case SENTRY -> handleSentry(player);
        }
    }

    private boolean isInteractableItem(Material mat) {
        return mat.isEdible() || mat == Material.BOW || mat == Material.CROSSBOW
                || mat == Material.TRIDENT || mat == Material.SHIELD
                || mat == Material.ENDER_PEARL || mat == Material.ENDER_EYE
                || mat == Material.SNOWBALL || mat == Material.EGG
                || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION
                || mat == Material.FISHING_ROD || mat == Material.SPYGLASS;
    }

    // ===== SILENCE: Sonic Boom =====
    private void handleSilence(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "silence_boom")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "silence_boom") / 1000;
            player.sendActionBar(Component.text("Sonic Boom on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "silence_boom", 5000);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();

        player.getWorld().playSound(eyeLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, eyeLoc.clone().add(direction.clone().multiply(2)), 1);

        RayTraceResult result = player.getWorld().rayTraceEntities(
                eyeLoc, direction, 20, 1.0,
                entity -> entity instanceof Player && entity != player
        );

        if (result != null && result.getHitEntity() instanceof Player target) {
            target.damage(10.0, player);
            target.setVelocity(direction.clone().multiply(1.5).setY(0.5));
            target.getWorld().spawnParticle(Particle.SONIC_BOOM, target.getLocation(), 1);

            int hits = abilityManager.addWardenBlastHit(id, target.getUniqueId());
            if (hits >= 3) {
                abilityManager.clearWardenBlastHits(id, target.getUniqueId());
                activateWardenForm(player);
            }
        }
    }

    private void activateWardenForm(Player player) {
        UUID id = player.getUniqueId();
        abilityManager.setWardenTransformed(id, true);

        player.sendMessage(Component.text("You have become the WARDEN!", NamedTextColor.DARK_AQUA));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 500, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 500, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 500, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 500, 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 2.0f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                abilityManager.setWardenTransformed(id, false);
                player.sendMessage(Component.text("Warden form has ended.", NamedTextColor.GRAY));
            }
        }.runTaskLater(plugin, 500); // 25 seconds
    }

    // ===== EYE: Piercing Stare =====
    private void handleEye(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "eye_beam")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "eye_beam") / 1000;
            player.sendActionBar(Component.text("Piercing Stare on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "eye_beam", 8000);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();

        // Visual beam
        for (double d = 0; d < 30; d += 0.5) {
            Location point = eyeLoc.clone().add(direction.clone().multiply(d));
            player.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
        }

        player.getWorld().playSound(eyeLoc, Sound.ENTITY_ENDER_EYE_DEATH, 2.0f, 0.5f);

        RayTraceResult result = player.getWorld().rayTraceEntities(
                eyeLoc, direction, 30, 1.0,
                entity -> entity instanceof Player && entity != player
        );

        if (result != null && result.getHitEntity() instanceof Player target) {
            target.damage(8.0, player);
            target.setVelocity(direction.clone().multiply(2.0).setY(0.8));
            target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 30, 0.5, 1, 0.5, 0.1);

            abilityManager.setEyeBeamTarget(id, target.getUniqueId());
            player.sendMessage(Component.text("All-Seeing Eye: Ender eyes will track " + target.getName() + " for 300 seconds!", NamedTextColor.LIGHT_PURPLE));
        }
    }

    // ===== SNOUT: Chop Lop =====
    private void handleSnout(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "snout_axe")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "snout_axe") / 1000;
            player.sendActionBar(Component.text("Chop Lop on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "snout_axe", 6000);

        ItemStack axe = new ItemStack(Material.GOLDEN_AXE);
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize().multiply(1.5);

        Item thrownAxe = player.getWorld().dropItem(eyeLoc, axe);
        thrownAxe.setVelocity(direction);
        thrownAxe.setPickupDelay(Integer.MAX_VALUE);
        thrownAxe.setOwner(player.getUniqueId());

        player.getWorld().playSound(eyeLoc, Sound.ITEM_TRIDENT_THROW, 1.0f, 0.8f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (thrownAxe.isDead() || ticks > 60) {
                    thrownAxe.remove();
                    cancel();
                    return;
                }

                thrownAxe.getWorld().spawnParticle(Particle.CRIT, thrownAxe.getLocation(), 3, 0.1, 0.1, 0.1, 0);

                for (Entity entity : thrownAxe.getNearbyEntities(1, 1, 1)) {
                    if (entity instanceof Player target && target != player) {
                        target.damage(8.0, player);
                        // Disable shield
                        if (target.isBlocking()) {
                            target.setCooldown(Material.SHIELD, 100);
                        }
                        target.setCooldown(Material.SHIELD, 100);
                        target.getWorld().playSound(target.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                        thrownAxe.remove();
                        cancel();
                        return;
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    // ===== RIB: Rib Rods =====
    private void handleRib(Player player) {
        UUID id = player.getUniqueId();
        int charges = cooldownManager.getCharges(id, "rib_rods", 2, 10000);

        if (charges <= 0) {
            player.sendActionBar(Component.text("Rib Rods: No charges available!", NamedTextColor.RED));
            return;
        }

        cooldownManager.useCharge(id, "rib_rods", 2, 10000);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize().multiply(1.5);

        Fireball fireball = player.getWorld().spawn(eyeLoc.clone().add(direction.clone().normalize()), Fireball.class);
        fireball.setDirection(direction.normalize().multiply(0.1));
        fireball.setShooter(player);
        fireball.setIsIncendiary(true);
        fireball.setYield(2.0f);
        fireball.setVisualFire(true);

        // Set the player on fire
        player.setFireTicks(60);

        player.getWorld().playSound(eyeLoc, Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.5f);
        player.sendActionBar(Component.text("Rib Rods: " + (charges - 1) + " charges remaining", NamedTextColor.GOLD));
    }

    // ===== FLOW: Air Bubble =====
    private void handleFlow(Player player) {
        UUID id = player.getUniqueId();
        Entity bubble = abilityManager.getAirBubble(id);

        if (bubble != null && !bubble.isDead()) {
            // If riding the bubble, unleash air burst
            if (bubble.getPassengers().contains(player)) {
                unleashAirBurst(player, bubble);
                return;
            }
            // Despawn and heal
            abilityManager.removeAirBubble(id);
            player.sendMessage(Component.text("Air Bubble despawned. Healing...", NamedTextColor.AQUA));
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(maxHealth, player.getHealth() + 6.0));
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0);
            return;
        }

        // Summon air bubble
        if (cooldownManager.isOnCooldown(id, "flow_bubble")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "flow_bubble") / 1000;
            player.sendActionBar(Component.text("Air Bubble on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        ArmorStand bubbleEntity = player.getWorld().spawn(player.getLocation().add(0, 0.5, 0), ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setMarker(false);
            stand.setCustomName("AirBubble");
            stand.setCustomNameVisible(false);
        });

        bubbleEntity.addPassenger(player);
        abilityManager.setAirBubble(id, bubbleEntity);

        player.getWorld().spawnParticle(Particle.BUBBLE_POP, player.getLocation(), 20, 1, 1, 1, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0f, 1.0f);
        player.sendMessage(Component.text("Air Bubble summoned! Right-click again to unleash air burst.", NamedTextColor.AQUA));

        // Bubble movement task
        new BukkitRunnable() {
            @Override
            public void run() {
                Entity currentBubble = abilityManager.getAirBubble(id);
                if (currentBubble == null || currentBubble.isDead() || !currentBubble.getPassengers().contains(player)) {
                    cancel();
                    return;
                }
                currentBubble.getWorld().spawnParticle(Particle.BUBBLE_POP, currentBubble.getLocation(), 5, 0.5, 0.5, 0.5, 0);
                Vector dir = player.getLocation().getDirection().normalize().multiply(0.3);
                currentBubble.setVelocity(dir);
            }
        }.runTaskTimer(plugin, 1, 2);
    }

    private void unleashAirBurst(Player player, Entity bubble) {
        Location loc = bubble.getLocation();
        player.leaveVehicle();
        abilityManager.removeAirBubble(player.getUniqueId());

        player.getWorld().playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 2.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 50, 2, 2, 2, 0.5);

        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 8, 8, 8)) {
            if (entity instanceof Player target && target != player) {
                Vector knockback = target.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(2.5).setY(1.0);
                target.setVelocity(knockback);
                target.damage(6.0, player);
            }
        }

        cooldownManager.setCooldown(player.getUniqueId(), "flow_bubble", 15000);
    }

    // ===== SPIRE: Shadow Clone =====
    private void handleSpire(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "spire_clone")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "spire_clone") / 1000;
            player.sendActionBar(Component.text("Shadow Clone on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "spire_clone", 20000);
        abilityManager.removeShadowClones(id);

        List<Entity> clones = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            ArmorStand clone = player.getWorld().spawn(player.getLocation(), ArmorStand.class, stand -> {
                stand.setInvisible(false);
                stand.setInvulnerable(true);
                stand.setGravity(true);
                stand.setArms(true);
                stand.setCustomName(player.getName());
                stand.setCustomNameVisible(true);
                // Copy armor
                for (int j = 0; j < 4; j++) {
                    ItemStack armor = player.getInventory().getArmorContents()[j];
                    if (armor != null) {
                        switch (j) {
                            case 0 -> stand.getEquipment().setBoots(armor.clone());
                            case 1 -> stand.getEquipment().setLeggings(armor.clone());
                            case 2 -> stand.getEquipment().setChestplate(armor.clone());
                            case 3 -> stand.getEquipment().setHelmet(armor.clone());
                        }
                    }
                }
            });
            clones.add(clone);
        }

        abilityManager.setShadowClones(id, clones);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);

        // Make clones run in random directions
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 200) {
                    abilityManager.removeShadowClones(id);
                    cancel();
                    return;
                }
                for (Entity clone : clones) {
                    if (clone == null || clone.isDead()) continue;
                    double angle = random.nextDouble() * Math.PI * 2;
                    double speed = 0.2 + random.nextDouble() * 0.3;
                    clone.setVelocity(new Vector(Math.cos(angle) * speed, 0, Math.sin(angle) * speed));
                }
                ticks += 10;
            }
        }.runTaskTimer(plugin, 1, 10);
    }

    // ===== BOLT: Thunderbolt =====
    private void handleBolt(Player player) {
        UUID id = player.getUniqueId();
        int charges = cooldownManager.getCharges(id, "bolt_thunder", 3, 16000);

        if (charges <= 0) {
            player.sendActionBar(Component.text("Thunderbolt: No charges! Recharging...", NamedTextColor.RED));
            return;
        }

        cooldownManager.useCharge(id, "bolt_thunder", 3, 16000);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();

        RayTraceResult result = player.getWorld().rayTrace(
                eyeLoc, direction, 50, FluidCollisionMode.NEVER, true, 1.0,
                entity -> entity instanceof LivingEntity && entity != player
        );

        Location strikeLocation;
        if (result != null) {
            strikeLocation = result.getHitPosition().toLocation(player.getWorld());
        } else {
            strikeLocation = eyeLoc.clone().add(direction.multiply(50));
        }

        player.getWorld().strikeLightning(strikeLocation);

        // Stun nearby players
        for (Entity entity : strikeLocation.getWorld().getNearbyEntities(strikeLocation, 3, 3, 3)) {
            if (entity instanceof Player target && target != player) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 5));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));
            }
        }

        player.sendActionBar(Component.text("Thunderbolt: " + (charges - 1) + " charges remaining", NamedTextColor.YELLOW));
    }

    // ===== HOST: Disguise Kit =====
    private void handleHost(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "host_disguise")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "host_disguise") / 1000;
            player.sendActionBar(Component.text("Disguise Kit on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        // Get target name from armor item name
        ItemStack chestplate = player.getInventory().getChestplate();
        String targetName = null;
        if (chestplate != null && chestplate.hasItemMeta() && chestplate.getItemMeta().hasDisplayName()) {
            targetName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(chestplate.getItemMeta().displayName());
        }

        if (targetName == null || targetName.isEmpty()) {
            player.sendMessage(Component.text("Name your armor piece to set a disguise target!", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "host_disguise", 30000);

        String finalTarget = targetName;
        abilityManager.setDisguise(id, targetName);

        player.setDisplayName(targetName);
        player.playerListName(Component.text(targetName));
        player.setCustomName(targetName);
        player.setCustomNameVisible(true);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        player.sendMessage(Component.text("Disguised as " + finalTarget + "!", NamedTextColor.GREEN));

        // Auto-remove after 60 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abilityManager.getDisguise(id) != null) {
                    abilityManager.removeDisguise(id);
                    player.setDisplayName(player.getName());
                    player.playerListName(null);
                    player.setCustomName(null);
                    player.setCustomNameVisible(false);
                    player.sendMessage(Component.text("Your disguise has worn off.", NamedTextColor.GRAY));
                }
            }
        }.runTaskLater(plugin, 1200);
    }

    // ===== RAISER: Remote Detonation =====
    private void handleRaiser(Player player) {
        UUID id = player.getUniqueId();
        Entity existingBomb = abilityManager.getThrownBomb(id);

        if (existingBomb != null && !existingBomb.isDead()) {
            // Check if player is looking at the bomb (detonation)
            Location eyeLoc = player.getEyeLocation();
            Vector towardsBomb = existingBomb.getLocation().toVector().subtract(eyeLoc.toVector()).normalize();
            Vector lookDir = eyeLoc.getDirection().normalize();
            double dot = lookDir.dot(towardsBomb);

            if (dot > 0.5) {
                // Detonate!
                Location bombLoc = existingBomb.getLocation();
                abilityManager.removeThrownBomb(id);

                bombLoc.getWorld().createExplosion(bombLoc, 4.0f, false, false, player);
                bombLoc.getWorld().spawnParticle(Particle.EXPLOSION, bombLoc, 5, 1, 1, 1, 0);

                // Damage nearby but not the player
                for (Entity entity : bombLoc.getWorld().getNearbyEntities(bombLoc, 5, 5, 5)) {
                    if (entity instanceof Player target && target != player) {
                        target.damage(10.0, player);
                        Vector knockback = target.getLocation().toVector().subtract(bombLoc.toVector()).normalize().multiply(1.5).setY(0.5);
                        target.setVelocity(knockback);
                    }
                }
                return;
            } else {
                // Not looking at bomb - don't allow throwing a new one while one exists
                player.sendActionBar(Component.text("Look at your bomb and right-click to detonate it!", NamedTextColor.GOLD));
                return;
            }
        }

        if (cooldownManager.isOnCooldown(id, "raiser_bomb")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "raiser_bomb") / 1000;
            player.sendActionBar(Component.text("Remote Detonation on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "raiser_bomb", 10000);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize().multiply(1.2);

        TNTPrimed tnt = player.getWorld().spawn(eyeLoc.clone().add(direction), TNTPrimed.class);
        tnt.setFuseTicks(Integer.MAX_VALUE);
        tnt.setVelocity(direction);
        tnt.setSource(player);

        abilityManager.setThrownBomb(id, tnt);
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.2f);
        player.sendMessage(Component.text("Bomb thrown! Right-click while looking at it to detonate!", NamedTextColor.GOLD));

        // Auto-despawn after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abilityManager.getThrownBomb(id) == tnt) {
                    abilityManager.removeThrownBomb(id);
                }
            }
        }.runTaskLater(plugin, 600);
    }

    // ===== SHAPER: Shape Shifty =====
    private void handleShaper(Player player) {
        UUID id = player.getUniqueId();
        double currentScale = abilityManager.getPlayerScale(id);
        float pitch = player.getLocation().getPitch();

        if (pitch < -30) {
            // Looking up = grow
            currentScale = Math.min(3.0, currentScale + 0.5);
        } else if (pitch > 30) {
            // Looking down = shrink
            currentScale = Math.max(0.25, currentScale - 0.5);
        } else {
            player.sendActionBar(Component.text("Look UP to grow, DOWN to shrink!", NamedTextColor.YELLOW));
            return;
        }

        abilityManager.setPlayerScale(id, currentScale);

        player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(currentScale);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, currentScale > 1.0 ? 0.5f : 1.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.5, 0.3, 0.05);
        player.sendActionBar(Component.text("Scale: " + String.format("%.1f", currentScale) + "x", NamedTextColor.GREEN));
    }

    // ===== TIDE: Riptide Rush =====
    private void handleTide(Player player) {
        UUID id = player.getUniqueId();
        int charges = cooldownManager.getCharges(id, "tide_riptide", 3, 8000);

        if (charges <= 0) {
            player.sendActionBar(Component.text("Riptide Rush: No charges!", NamedTextColor.RED));
            return;
        }

        cooldownManager.useCharge(id, "tide_riptide", 3, 8000);

        Vector direction = player.getLocation().getDirection().normalize().multiply(2.5);
        direction.setY(Math.max(direction.getY(), 0.5));
        player.setVelocity(direction);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);

        player.sendActionBar(Component.text("Riptide Rush: " + (charges - 1) + " charges", NamedTextColor.AQUA));
    }

    // ===== WARD: Sound Power =====
    private void handleWard(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "ward_sound")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "ward_sound") / 1000;
            player.sendActionBar(Component.text("Sound Power on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "ward_sound", 30000);

        // Grant huge stat boosts
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));

        // Play sound variety
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2.0f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 2.0f);
        player.getWorld().spawnParticle(Particle.NOTE, player.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 1);

        player.sendMessage(Component.text("Sound Power activated! Massive stat boost for 10 seconds!", NamedTextColor.LIGHT_PURPLE));
    }

    // ===== DUNE: Burrow =====
    private void handleDune(Player player) {
        UUID id = player.getUniqueId();

        if (abilityManager.isBurrowed(id)) {
            return; // Handled by jump in movement listener
        }

        if (cooldownManager.isOnCooldown(id, "dune_burrow")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "dune_burrow") / 1000;
            player.sendActionBar(Component.text("Burrow on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        // Check if standing on ground
        Block below = player.getLocation().subtract(0, 1, 0).getBlock();
        if (below.getType().isAir()) {
            player.sendActionBar(Component.text("Must be on ground to burrow!", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "dune_burrow", 15000);
        abilityManager.setBurrowed(id, true);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 30, 0.5, 0.3, 0.5, 0,
                below.getBlockData());

        player.sendMessage(Component.text("You burrowed underground! Jump to emerge!", NamedTextColor.YELLOW));

        // Auto-unburrow after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abilityManager.isBurrowed(id)) {
                    emergeBurrow(player);
                }
            }
        }.runTaskLater(plugin, 100);
    }

    public void emergeBurrow(Player player) {
        UUID id = player.getUniqueId();
        if (!abilityManager.isBurrowed(id)) return;

        abilityManager.setBurrowed(id, false);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 3, 0.5, 0.5, 0.5, 0);

        // Stun nearby players (Emergence)
        for (Entity entity : player.getNearbyEntities(7, 7, 7)) {
            if (entity instanceof Player target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 5));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                target.damage(2.6, player); // 1.3 hearts = 2.6 damage
                target.sendMessage(Component.text("You were stunned by " + player.getName() + "'s emergence!", NamedTextColor.YELLOW));
            }
        }

        player.sendMessage(Component.text("You emerged from the ground!", NamedTextColor.GREEN));
    }

    // ===== WAYFINDER: Clever Cloaking =====
    private void handleWayfinder(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "wayfinder_cloak")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "wayfinder_cloak") / 1000;
            player.sendActionBar(Component.text("Clever Cloaking on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "wayfinder_cloak", 15000);
        abilityManager.setCloaked(id, true);

        // True invisibility - hide from all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != player) {
                online.hidePlayer(plugin, player);
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 2.0f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 1, 0.5, 0.05);
        player.sendMessage(Component.text("You are truly invisible!", NamedTextColor.GREEN));

        // Auto-uncloak after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abilityManager.isCloaked(id)) {
                    abilityManager.setCloaked(id, false);
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.showPlayer(plugin, player);
                    }
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    player.sendMessage(Component.text("Your cloak has faded.", NamedTextColor.GRAY));
                }
            }
        }.runTaskLater(plugin, 200);
    }

    // ===== COAST: Wave =====
    private void handleCoast(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "coast_wave")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "coast_wave") / 1000;
            player.sendActionBar(Component.text("Wave on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "coast_wave", 8000);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        direction.setY(0);
        direction.normalize();

        player.getWorld().playSound(eyeLoc, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.8f);

        // Create wave particles
        new BukkitRunnable() {
            double distance = 0;

            @Override
            public void run() {
                if (distance > 15) {
                    cancel();
                    return;
                }

                Location waveLoc = eyeLoc.clone().add(direction.clone().multiply(distance));
                waveLoc.setY(player.getLocation().getY());

                // Wide wave effect
                Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX());
                for (double w = -3; w <= 3; w += 0.5) {
                    Location partLoc = waveLoc.clone().add(perpendicular.clone().multiply(w));
                    partLoc.getWorld().spawnParticle(Particle.SPLASH, partLoc, 3, 0.1, 0.3, 0.1, 0);
                    partLoc.getWorld().spawnParticle(Particle.BUBBLE_POP, partLoc, 2, 0.1, 0.3, 0.1, 0);
                }

                // Hit entities
                for (Entity entity : waveLoc.getWorld().getNearbyEntities(waveLoc, 3, 2, 3)) {
                    if (entity instanceof Player target && target != player) {
                        target.damage(6.0, player);
                        target.setVelocity(direction.clone().multiply(1.5).setY(0.5));
                    }
                }

                distance += 1.5;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    // ===== WILD: Vine Hook =====
    private void handleWild(Player player) {
        UUID id = player.getUniqueId();

        // Check if already hooked - pull mechanics
        UUID hookTarget = abilityManager.getVineHookTarget(id);
        if (hookTarget != null) {
            Player target = Bukkit.getPlayer(hookTarget);
            if (target != null && target.isOnline()) {
                // Right-click while hooked = pull yourself toward target
                Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5);
                player.setVelocity(direction);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_STEP, 1.0f, 1.5f);
                return;
            }
        }

        if (cooldownManager.isOnCooldown(id, "wild_vine")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "wild_vine") / 1000;
            player.sendActionBar(Component.text("Vine Hook on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "wild_vine", 12000);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();

        player.getWorld().playSound(eyeLoc, Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.8f);

        // Vine projectile
        new BukkitRunnable() {
            double distance = 0;
            final Location start = eyeLoc.clone();

            @Override
            public void run() {
                if (distance > 25) {
                    cancel();
                    return;
                }

                Location current = start.clone().add(direction.clone().multiply(distance));
                current.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, current, 2, 0.1, 0.1, 0.1, 0);

                for (Entity entity : current.getWorld().getNearbyEntities(current, 1, 1, 1)) {
                    if (entity instanceof Player target && target != player) {
                        abilityManager.setVineHookTarget(id, target.getUniqueId());
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                        player.sendMessage(Component.text("Vine Hook attached to " + target.getName() + "! Left-click to pull them, right-click to pull yourself!", NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You've been hooked by " + player.getName() + "'s vine!", NamedTextColor.RED));

                        // Auto-detach after 5 seconds
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                abilityManager.removeVineHook(id);
                            }
                        }.runTaskLater(plugin, 100);

                        cancel();
                        return;
                    }
                }

                distance += 1.5;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    // ===== VEX: Fangfooted =====
    private void handleVex(Player player) {
        UUID id = player.getUniqueId();
        if (cooldownManager.isOnCooldown(id, "vex_fang")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "vex_fang") / 1000;
            player.sendActionBar(Component.text("Fangfooted on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        boolean active = abilityManager.isFangTrailActive(id);
        abilityManager.setFangTrailActive(id, !active);

        if (!active) {
            cooldownManager.setCooldown(id, "vex_fang", 25000);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
            player.sendMessage(Component.text("Fangfooted activated! You now leave a trail of evoker fangs!", NamedTextColor.DARK_PURPLE));

            // Trail task
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (!abilityManager.isFangTrailActive(id) || ticks > 200) {
                        abilityManager.setFangTrailActive(id, false);
                        cancel();
                        return;
                    }

                    Location loc = player.getLocation();
                    player.getWorld().spawn(loc, EvokerFangs.class, fangs -> {
                        fangs.setOwner(player);
                    });

                    ticks += 4;
                }
            }.runTaskTimer(plugin, 0, 4);
        } else {
            abilityManager.setFangTrailActive(id, false);
            player.sendMessage(Component.text("Fangfooted deactivated.", NamedTextColor.GRAY));
        }
    }

    // ===== SENTRY: Ravager =====
    private void handleSentry(Player player) {
        UUID id = player.getUniqueId();
        Entity ravager = abilityManager.getRavagerMount(id);

        if (ravager != null && !ravager.isDead()) {
            if (ravager.getPassengers().contains(player)) {
                // Riding ravager - summon raid party
                if (cooldownManager.isOnCooldown(id, "sentry_raid")) {
                    long remaining = cooldownManager.getRemainingCooldown(id, "sentry_raid") / 1000;
                    player.sendActionBar(Component.text("Raid Party on cooldown: " + remaining + "s", NamedTextColor.RED));
                    return;
                }

                cooldownManager.setCooldown(id, "sentry_raid", 60000);
                summonRaidParty(player);
                return;
            }

            // Despawn ravager (heals over time)
            abilityManager.removeRavagerMount(id);
            player.sendMessage(Component.text("Ravager despawned. It will heal while away.", NamedTextColor.GRAY));
            return;
        }

        if (cooldownManager.isOnCooldown(id, "sentry_ravager")) {
            long remaining = cooldownManager.getRemainingCooldown(id, "sentry_ravager") / 1000;
            player.sendActionBar(Component.text("Ravager on cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        cooldownManager.setCooldown(id, "sentry_ravager", 30000);

        Ravager ravagerEntity = player.getWorld().spawn(player.getLocation(), Ravager.class, r -> {
            r.setAI(false);
        });

        ravagerEntity.addPassenger(player);
        abilityManager.setRavagerMount(id, ravagerEntity);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 1.0f);
        player.sendMessage(Component.text("Ravager summoned! Right-click again while riding to summon a Raid Party!", NamedTextColor.DARK_GREEN));
    }

    private void summonRaidParty(Player player) {
        Location loc = player.getLocation();
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            Location spawnLoc = loc.clone().add(random.nextInt(6) - 3, 0, random.nextInt(6) - 3);
            EntityType[] raiders = {EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER};
            player.getWorld().spawnEntity(spawnLoc, raiders[random.nextInt(raiders.length)]);
        }

        player.getWorld().playSound(loc, Sound.EVENT_RAID_HORN, 2.0f, 1.0f);
        player.sendMessage(Component.text("Raid Party summoned!", NamedTextColor.DARK_RED));
    }
}
