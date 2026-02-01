package com.uniqueskills.listeners;

import com.uniqueskills.abilities.BlinkAbility;
import com.uniqueskills.abilities.BlastAbility;
import com.uniqueskills.abilities.TeleportAbility;
import com.uniqueskills.abilities.DashAbility;
import com.uniqueskills.UniqueSkillsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AbilityListener implements Listener {

    private final UniqueSkillsPlugin plugin;
    private final BlinkAbility blinkAbility;
    private final BlastAbility blastAbility;
    private final TeleportAbility teleportAbility;
    private final DashAbility dashAbility;

    public AbilityListener(UniqueSkillsPlugin plugin, BlinkAbility blink, BlastAbility blast, TeleportAbility teleport,
            DashAbility dash) {
        this.plugin = plugin;
        this.blinkAbility = blink;
        this.blastAbility = blast;
        this.teleportAbility = teleport;
        this.dashAbility = dash;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Prevent double execution from both hands
        if (event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR)
            return;

        // Blink - activated with Feather
        if (item.getType() == Material.FEATHER && blinkAbility.isEnabled(player)) {
            blinkAbility.executeDash(player);
            return;
        }

        // Blast - activated with TNT Minecart
        if (item.getType() == Material.TNT_MINECART && blastAbility.isEnabled(player)) {
            event.setCancelled(true); // Prevent placing minecart
            blastAbility.executeBlastPack(player);
            return;
        }

        // Teleport - activated with Ender Pearl
        if (item.getType() == Material.ENDER_PEARL && teleportAbility.isEnabled(player)) {
            event.setCancelled(true); // Prevent throwing vanilla pearl
            teleportAbility.execute(player);
            return;
        }

        // Dash - activated with Blaze Rod
        if (item.getType() == Material.BLAZE_ROD && dashAbility.isEnabled(player)) {
            event.setCancelled(true);
            dashAbility.execute(player);
            return;
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) { // Only when starting to sneak
            dashAbility.handleSneak(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        blinkAbility.cleanup(uuid);
        blastAbility.cleanup(uuid);
        teleportAbility.cleanup(uuid);
        dashAbility.cleanup(uuid);
    }
}
