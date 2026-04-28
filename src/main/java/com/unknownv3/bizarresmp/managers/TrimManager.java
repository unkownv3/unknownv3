package com.unknownv3.bizarresmp.managers;

import com.unknownv3.bizarresmp.trims.TrimType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class TrimManager {

    public TrimType getPlayerTrim(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().isAir()) continue;
            if (!(armor.getItemMeta() instanceof ArmorMeta armorMeta)) continue;
            if (!armorMeta.hasTrim()) continue;
            ArmorTrim trim = armorMeta.getTrim();
            TrimType type = TrimType.fromPattern(trim.getPattern());
            if (type != null) return type;
        }
        return null;
    }

    public boolean hasTrim(Player player, TrimType trimType) {
        return getPlayerTrim(player) == trimType;
    }
}
