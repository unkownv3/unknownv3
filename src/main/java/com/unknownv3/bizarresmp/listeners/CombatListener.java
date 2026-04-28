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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class CombatListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;
    private final AbilityManager abilityManager;

    public CombatListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
        this.abilityManager = plugin.getAbilityManager();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // ===== SPIRE: Shulker Strike - Summon shulker bullet on attack =====
        if (event.getDamager() instanceof Player attacker) {
            TrimType trim = trimManager.getPlayerTrim(attacker);

            if (trim == TrimType.SPIRE && event.getEntity() instanceof LivingEntity target) {
                ShulkerBullet bullet = attacker.getWorld().spawn(
                        attacker.getEyeLocation().add(attacker.getLocation().getDirection().multiply(2)),
                        ShulkerBullet.class
                );
                bullet.setShooter(attacker);
                bullet.setTarget(target);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.0f);
            }

            // ===== VEX: Thorned Strike - Fully charged attack summons evoker fang =====
            if (trim == TrimType.VEX && event.getEntity() instanceof LivingEntity target) {
                if (attacker.getAttackCooldown() >= 0.9f) {
                    Location fangLoc = target.getLocation();
                    attacker.getWorld().spawn(fangLoc, EvokerFangs.class, fangs -> {
                        fangs.setOwner(attacker);
                    });
                    attacker.getWorld().playSound(fangLoc, Sound.ENTITY_EVOKER_FANGS_ATTACK, 1.0f, 1.0f);
                }
            }

            // ===== WILD: Grabber - Left-click while vine hooked pulls target to you =====
            if (trim == TrimType.WILD) {
                UUID hookTarget = abilityManager.getVineHookTarget(attacker.getUniqueId());
                if (hookTarget != null && event.getEntity() instanceof Player target && target.getUniqueId().equals(hookTarget)) {
                    Vector pullDir = attacker.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.5);
                    target.setVelocity(pullDir);
                    attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_CHAIN_STEP, 1.0f, 0.8f);
                }
            }
        }
    }

    // ===== SNOUT: Midas Touch - Killed players turn to gold =====
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        TrimType trim = trimManager.getPlayerTrim(killer);
        if (trim == TrimType.SNOUT) {
            Location deathLoc = victim.getLocation();

            // Drop gold blocks as a "pile of gold"
            deathLoc.getWorld().dropItemNaturally(deathLoc, new ItemStack(Material.GOLD_BLOCK, 3));
            deathLoc.getWorld().dropItemNaturally(deathLoc, new ItemStack(Material.GOLD_INGOT, 5));
            deathLoc.getWorld().dropItemNaturally(deathLoc, new ItemStack(Material.GOLD_NUGGET, 12));

            deathLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, deathLoc, 30, 0.5, 1, 0.5, 0);
            deathLoc.getWorld().playSound(deathLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 1.0f);

            killer.sendMessage(Component.text("Midas Touch: " + victim.getName() + " turned to gold!", NamedTextColor.GOLD));
        }
    }
}
