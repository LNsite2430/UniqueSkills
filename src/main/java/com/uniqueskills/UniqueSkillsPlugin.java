package com.uniqueskills;

import com.uniqueskills.abilities.BlinkAbility;
import com.uniqueskills.abilities.BlastAbility;
import com.uniqueskills.abilities.TeleportAbility;
import com.uniqueskills.abilities.DashAbility;
import com.uniqueskills.commands.BlinkCommand;
import com.uniqueskills.commands.BlastCommand;
import com.uniqueskills.commands.TeleportCommand;
import com.uniqueskills.commands.DashCommand;
import com.uniqueskills.commands.SkillCommand;
import com.uniqueskills.gui.SkillSelectorGUI;
import com.uniqueskills.listeners.AbilityListener;
import org.bukkit.plugin.java.JavaPlugin;

public class UniqueSkillsPlugin extends JavaPlugin {

    private BlinkAbility blinkAbility;
    private BlastAbility blastAbility;
    private TeleportAbility teleportAbility;
    private DashAbility dashAbility;
    private SkillSelectorGUI skillSelectorGUI;

    @Override
    public void onEnable() {
        // Initialize Abilities
        this.blinkAbility = new BlinkAbility(this);
        this.blastAbility = new BlastAbility(this);
        this.teleportAbility = new TeleportAbility(this);
        this.dashAbility = new DashAbility(this);

        // Initialize GUI
        this.skillSelectorGUI = new SkillSelectorGUI(this);

        // Register Commands
        getCommand("uskills").setExecutor(new SkillCommand(skillSelectorGUI));
        getCommand("blink").setExecutor(new BlinkCommand(blinkAbility, this));
        getCommand("blast").setExecutor(new BlastCommand(blastAbility, this));
        getCommand("teleport").setExecutor(new TeleportCommand(teleportAbility, this));
        getCommand("dash").setExecutor(new DashCommand(dashAbility, this));

        // Register Listeners
        getServer().getPluginManager()
                .registerEvents(new AbilityListener(this, blinkAbility, blastAbility, teleportAbility, dashAbility),
                        this);
        getServer().getPluginManager().registerEvents(skillSelectorGUI, this);

        getLogger().info("UniqueSkills Plugin Enabled! Commands: /uskills, /blink, /blast, /teleport, /dash");
    }

    @Override
    public void onDisable() {
        // Cleanup all players to remove entities
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            java.util.UUID uuid = player.getUniqueId();
            if (blinkAbility != null)
                blinkAbility.cleanup(uuid);
            if (blastAbility != null)
                blastAbility.cleanup(uuid);
            if (teleportAbility != null)
                teleportAbility.cleanup(uuid);
            if (dashAbility != null)
                dashAbility.cleanup(uuid);
        }
        getLogger().info("UniqueSkills プラグインが無効化されました");
    }

    public void disableAllAbilities(org.bukkit.entity.Player player) {
        if (blinkAbility != null)
            blinkAbility.setEnabled(player, false);
        if (blastAbility != null)
            blastAbility.setEnabled(player, false);
        if (teleportAbility != null)
            teleportAbility.setEnabled(player, false);
        if (dashAbility != null)
            dashAbility.setEnabled(player, false);
    }

    public BlinkAbility getBlinkAbility() {
        return blinkAbility;
    }

    public BlastAbility getBlastAbility() {
        return blastAbility;
    }

    public TeleportAbility getTeleportAbility() {
        return teleportAbility;
    }

    public DashAbility getDashAbility() {
        return dashAbility;
    }
}
