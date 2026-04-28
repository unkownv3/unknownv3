package com.unknownv3.bizarresmp.listeners;

import com.unknownv3.bizarresmp.BizarreSMP;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.trims.TrimType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class EffectListener implements Listener {

    private final BizarreSMP plugin;
    private final TrimManager trimManager;

    private static final Set<PotionEffectType> DEBUFF_EFFECTS = Set.of(
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.NAUSEA,
            PotionEffectType.BLINDNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.BAD_OMEN,
            PotionEffectType.DARKNESS,
            PotionEffectType.UNLUCK
    );

    public EffectListener(BizarreSMP plugin) {
        this.plugin = plugin;
        this.trimManager = plugin.getTrimManager();
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;

        TrimType trim = trimManager.getPlayerTrim(player);

        // WILD: Jungle Immunity - Immune to debuff status effects
        if (trim == TrimType.WILD) {
            if (event.getNewEffect() != null && DEBUFF_EFFECTS.contains(event.getNewEffect().getType())) {
                event.setCancelled(true);
            }
        }
    }
}
