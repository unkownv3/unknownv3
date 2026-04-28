package com.unknownv3.bizarresmp.listeners;

import com.unknownv3.bizarresmp.BizarreSMP;
import com.unknownv3.bizarresmp.managers.AbilityManager;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.trims.TrimType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class JoinLeaveListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;
    private final AbilityManager abilityManager;

    public JoinLeaveListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
        this.abilityManager = plugin.getAbilityManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // HOST: Silent Arrival - suppress join message synchronously
        TrimType syncTrim = trimManager.getPlayerTrim(player);
        if (syncTrim == TrimType.HOST) {
            event.joinMessage(null);
        }

        // Delayed check for trim-based setup (armor may not be loaded instantly)
        new BukkitRunnable() {
            @Override
            public void run() {
                TrimType trim = trimManager.getPlayerTrim(player);
                if (trim == null) return;

                // HOST: Never Online - Don't appear in player list
                if (trim == TrimType.HOST) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (online != player) {
                            online.hidePlayer(plugin, player);
                        }
                    }
                }

                // TIDE: Aquatic Affinity - Breathe underwater
                if (trim == TrimType.TIDE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false, true));
                }

                // VEX: Summon allay companion
                if (trim == TrimType.VEX) {
                    if (abilityManager.getAllayCompanion(player.getUniqueId()) == null) {
                        org.bukkit.entity.Allay allay = player.getWorld().spawn(
                                player.getLocation().add(0, 2, 0), org.bukkit.entity.Allay.class
                        );
                        abilityManager.setAllayCompanion(player.getUniqueId(), allay);
                    }
                }
            }
        }.runTaskLater(plugin, 10);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TrimType trim = trimManager.getPlayerTrim(player);

        // HOST: Silent Arrival - No leave message
        if (trim == TrimType.HOST) {
            event.quitMessage(null);
        }

        // Show player again to everyone
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }

        // Clean up abilities
        abilityManager.clearPlayer(player.getUniqueId());
        plugin.getCooldownManager().clearPlayer(player.getUniqueId());
    }
}
