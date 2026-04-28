package com.unknownv3.bizarresmp.listeners;

import com.unknownv3.bizarresmp.BizarreSMP;
import com.unknownv3.bizarresmp.managers.AbilityManager;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.trims.TrimType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PassiveAbilityListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;
    private final AbilityManager abilityManager;

    public PassiveAbilityListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
        this.abilityManager = plugin.getAbilityManager();
    }

    // ===== BOLT: Overclock - Passive Speed Boost =====
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        TrimType trim = trimManager.getPlayerTrim(player);

        // WARD: Blending In - won't be targeted by mobs
        if (trim == TrimType.WARD) {
            if (!(event.getEntity() instanceof Player)) {
                event.setCancelled(true);
            }
        }

        // SILENCE: Unnoticeable - won't be noticed by Sculk Sensors or Wardens
        if (trim == TrimType.SILENCE) {
            if (event.getEntity() instanceof Warden) {
                event.setCancelled(true);
            }
        }
    }

    // ===== EYE: All-Seeing Eye - Ender eyes track beam target =====
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;

        // EYE: All-Seeing Eye
        if (event.getEntity().getType() == EntityType.EYE_OF_ENDER) {
            TrimType trim = trimManager.getPlayerTrim(player);
            if (trim == TrimType.EYE) {
                UUID targetId = abilityManager.getEyeBeamTarget(player.getUniqueId());
                if (targetId != null) {
                    Player target = Bukkit.getPlayer(targetId);
                    if (target != null && target.isOnline()) {
                        event.setCancelled(true);
                        // Launch a custom ender eye that tracks the player
                        Location eyeLoc = player.getEyeLocation();
                        EnderSignal enderEye = player.getWorld().spawn(eyeLoc, EnderSignal.class);
                        enderEye.setTargetLocation(target.getLocation());

                        // Keep updating target location
                        new BukkitRunnable() {
                            int ticks = 0;

                            @Override
                            public void run() {
                                if (enderEye.isDead() || ticks > 600 || !target.isOnline()) {
                                    cancel();
                                    return;
                                }
                                enderEye.setTargetLocation(target.getLocation());
                                ticks += 5;
                            }
                        }.runTaskTimer(plugin, 5, 5);

                        player.sendMessage(Component.text("Ender eye tracking " + target.getName() + "!", NamedTextColor.LIGHT_PURPLE));
                    }
                }
            }
        }

        // DUNE: Dynamite - Load crossbow with TNT
        if (event.getEntity() instanceof Firework) return;
        if (event.getEntity().getShooter() instanceof Player shooter) {
            TrimType trim = trimManager.getPlayerTrim(shooter);
            if (trim == TrimType.DUNE) {
                ItemStack mainHand = shooter.getInventory().getItemInMainHand();
                if (mainHand.getType() == Material.CROSSBOW) {
                    // Check if they loaded TNT
                    ItemStack offHand = shooter.getInventory().getItemInOffHand();
                    if (offHand.getType() == Material.TNT) {
                        event.setCancelled(true);
                        offHand.setAmount(offHand.getAmount() - 1);

                        Location eyeLoc = shooter.getEyeLocation();
                        Vector direction = eyeLoc.getDirection().normalize().multiply(1.5);

                        TNTPrimed tnt = shooter.getWorld().spawn(eyeLoc.clone().add(direction.clone().normalize()), TNTPrimed.class);
                        tnt.setFuseTicks(40);
                        tnt.setVelocity(direction);
                        tnt.setSource(shooter);

                        shooter.getWorld().playSound(eyeLoc, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.5f);
                    }
                }
            }
        }
    }

    // ===== RAISER: Delicate Drop - Crouching gives slow falling =====
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        TrimType trim = trimManager.getPlayerTrim(player);

        if (trim == TrimType.RAISER) {
            if (event.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
            } else {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            }
        }
    }
}
