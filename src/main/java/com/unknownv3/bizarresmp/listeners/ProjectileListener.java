package com.unknownv3.bizarresmp.listeners;

import com.unknownv3.bizarresmp.BizarreSMP;
import com.unknownv3.bizarresmp.managers.AbilityManager;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.trims.TrimType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public class ProjectileListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;
    private final AbilityManager abilityManager;

    public ProjectileListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
        this.abilityManager = plugin.getAbilityManager();
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // FLOW: Only trim that can use the mace
        if (event.getEntity().getShooter() instanceof Player player) {
            // Nothing specific needed here for mace - handled via equipment check
        }
    }

    @EventHandler
    public void onEntityDamageByProjectile(EntityDamageByEntityEvent event) {
        // SPIRE: Shulker Strike causes levitation
        if (event.getDamager() instanceof ShulkerBullet bullet) {
            if (bullet.getShooter() instanceof Player shooter) {
                TrimType trim = trimManager.getPlayerTrim(shooter);
                if (trim == TrimType.SPIRE && event.getEntity() instanceof LivingEntity target) {
                    target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.LEVITATION, 60, 1
                    ));
                }
            }
        }
    }
}
