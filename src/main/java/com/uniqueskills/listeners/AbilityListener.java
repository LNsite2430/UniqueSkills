package com.uniqueskills.listeners;

import com.uniqueskills.abilities.BlinkAbility;
import com.uniqueskills.abilities.BlastAbility;
import com.uniqueskills.abilities.TeleportAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AbilityListener implements Listener {

    private final BlinkAbility blinkAbility;
    private final BlastAbility blastAbility;
    private final TeleportAbility teleportAbility;

    public AbilityListener(BlinkAbility blinkAbility, BlastAbility blastAbility,
            TeleportAbility teleportAbility) {
        this.blinkAbility = blinkAbility;
        this.blastAbility = blastAbility;
        this.teleportAbility = teleportAbility;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        // Right click detection
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        // Blink - activated with Feather
        if (item.getType() == Material.FEATHER && blinkAbility.isEnabled(player)) {
            event.setCancelled(true);
            blinkAbility.executeDash(player);
            return;
        }

        // Blast - activated with TNT Minecart
        if (item.getType() == Material.TNT_MINECART && blastAbility.isEnabled(player)) {
            event.setCancelled(true);
            blastAbility.executeBlastPack(player);
            return;
        }

        // Teleport - activated with Ender Pearl
        if (item.getType() == Material.ENDER_PEARL && teleportAbility.isEnabled(player)) {
            event.setCancelled(true); // Prevent throwing vanilla pearl
            teleportAbility.execute(player);
            return;
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        blinkAbility.cleanup(uuid);
        blastAbility.cleanup(uuid);
        teleportAbility.cleanup(uuid);
    }
}
