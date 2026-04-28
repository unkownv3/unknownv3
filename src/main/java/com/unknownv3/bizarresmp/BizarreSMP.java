package com.unknownv3.bizarresmp;

import com.unknownv3.bizarresmp.listeners.*;
import com.unknownv3.bizarresmp.managers.CooldownManager;
import com.unknownv3.bizarresmp.managers.TrimManager;
import com.unknownv3.bizarresmp.managers.AbilityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BizarreSMP extends JavaPlugin {

    private static BizarreSMP instance;
    private TrimManager trimManager;
    private CooldownManager cooldownManager;
    private AbilityManager abilityManager;

    @Override
    public void onEnable() {
        instance = this;
        trimManager = new TrimManager();
        cooldownManager = new CooldownManager();
        abilityManager = new AbilityManager(this);

        registerListeners();

        getLogger().info("Unknownv3's Bizzare SMP has been enabled!");
    }

    @Override
    public void onDisable() {
        abilityManager.cleanup();
        getLogger().info("Unknownv3's Bizzare SMP has been disabled!");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new UseAbilityListener(this), this);
        pm.registerEvents(new PassiveAbilityListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new MovementListener(this), this);
        pm.registerEvents(new JoinLeaveListener(this), this);
        pm.registerEvents(new DamageListener(this), this);
        pm.registerEvents(new ProjectileListener(this), this);
        pm.registerEvents(new EffectListener(this), this);
    }

    public static BizarreSMP getInstance() {
        return instance;
    }

    public TrimManager getTrimManager() {
        return trimManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
}
