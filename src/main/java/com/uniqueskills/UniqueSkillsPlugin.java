package com.uniqueskills;

import com.uniqueskills.abilities.BlinkAbility;
import com.uniqueskills.abilities.BlastAbility;
import com.uniqueskills.abilities.TeleportAbility;
import com.uniqueskills.commands.BlinkCommand;
import com.uniqueskills.commands.BlastCommand;
import com.uniqueskills.commands.TeleportCommand;
import com.uniqueskills.commands.SkillCommand;
import com.uniqueskills.gui.SkillSelectorGUI;
import com.uniqueskills.listeners.AbilityListener;
import org.bukkit.plugin.java.JavaPlugin;

public class UniqueSkillsPlugin extends JavaPlugin {

    private BlinkAbility blinkAbility;
    private BlastAbility blastAbility;
    private TeleportAbility teleportAbility;
    private SkillSelectorGUI skillSelectorGUI;

    @Override
    public void onEnable() {
        // Initialize abilities
        blinkAbility = new BlinkAbility(this);
        blastAbility = new BlastAbility(this);
        teleportAbility = new TeleportAbility(this);

        // Initialize GUI
        skillSelectorGUI = new SkillSelectorGUI(this);

        // Register commands
        getCommand("blink").setExecutor(new BlinkCommand(blinkAbility, this));
        getCommand("blast").setExecutor(new BlastCommand(blastAbility, this));
        getCommand("teleport").setExecutor(new TeleportCommand(teleportAbility, this));
        getCommand("uskills").setExecutor(new SkillCommand(skillSelectorGUI));

        // Register listeners
        getServer().getPluginManager()
                .registerEvents(new AbilityListener(blinkAbility, blastAbility, teleportAbility), this);
        getServer().getPluginManager().registerEvents(skillSelectorGUI, this);

        getLogger().info("UniqueSkills プラグインが有効化されました！");
        getLogger().info("Commands: /blink, /blast, /teleport, /uskills");
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
        }
        getLogger().info("UniqueSkills プラグインが無効化されました");
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
}
