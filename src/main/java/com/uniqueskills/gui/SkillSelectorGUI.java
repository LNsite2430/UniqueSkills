package com.uniqueskills.gui;

import com.uniqueskills.UniqueSkillsPlugin;
import com.uniqueskills.abilities.BlinkAbility;
import com.uniqueskills.abilities.BlastAbility;
import com.uniqueskills.abilities.TeleportAbility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillSelectorGUI implements Listener {

    private final UniqueSkillsPlugin plugin;
    private final String GUI_TITLE = "§8Select Skill";

    public SkillSelectorGUI(UniqueSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);

        // Blink Item
        ItemStack blinkItem = new ItemStack(Material.FEATHER);
        ItemMeta blinkMeta = blinkItem.getItemMeta();
        blinkMeta.setDisplayName("§b§lBlink");
        List<String> blinkLore = new ArrayList<>();
        blinkLore.add("§7Click to select Blink ability.");
        blinkLore.add("");
        blinkLore.add("§e[English]");
        blinkLore.add("§7Dash forward instantly.");
        blinkLore.add("§7Right-click with Feather to use.");
        blinkLore.add("");
        blinkLore.add("§e[日本語]");
        blinkLore.add("§7向いている方向に高速移動します。");
        blinkLore.add("§7羽根を持って右クリックで発動。");
        blinkLore.add("");
        if (plugin.getBlinkAbility().isEnabled(player)) {
            blinkLore.add("§a§l[SELECTED]");
        }
        blinkMeta.setLore(blinkLore);
        blinkItem.setItemMeta(blinkMeta);

        // Blast Item
        ItemStack blastItem = new ItemStack(Material.TNT_MINECART);
        ItemMeta blastMeta = blastItem.getItemMeta();
        blastMeta.setDisplayName("§e§lBlast");
        List<String> blastLore = new ArrayList<>();
        blastLore.add("§7Click to select Blast ability.");
        blastLore.add("");
        blastLore.add("§e[English]");
        blastLore.add("§7Throw a blast pack to boost jump.");
        blastLore.add("§7Right-click to throw, right-click again to detonate.");
        blastLore.add("§7Stronger boost in mid-air!");
        blastLore.add("");
        blastLore.add("§e[日本語]");
        blastLore.add("§7爆発パックを使ってブーストジャンプします。");
        blastLore.add("§7TNTトロッコを持って右クリックで投擲、再度右クリックで起爆。");
        blastLore.add("§7空中での起爆はブースト力が大幅に強化されます！");
        blastLore.add("");
        if (plugin.getBlastAbility().isEnabled(player)) {
            blastLore.add("§a§l[SELECTED]");
        }
        blastMeta.setLore(blastLore);
        blastItem.setItemMeta(blastMeta);

        // Teleport Item
        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        teleportMeta.setDisplayName("§9§lTeleport");
        List<String> teleportLore = new ArrayList<>();
        teleportLore.add("§7Click to select Teleport ability.");
        teleportLore.add("");
        teleportLore.add("§e[English]");
        teleportLore.add("§7Send a moving beacon to teleport.");
        teleportLore.add("§7Right-click to send, right-click again to teleport.");
        teleportLore.add("§7Shift + Right-click for Fake Teleport.");
        teleportLore.add("");
        teleportLore.add("§e[日本語]");
        teleportLore.add("§7壁沿いに進むビーコンを出してテレポートします。");
        teleportLore.add("§7エンダーパールを持って右クリックで発射、再度右クリックでテレポート。");
        teleportLore.add("§7スニーク + 右クリックでフェイクテレポートを発動できます。");
        teleportLore.add("");
        if (plugin.getTeleportAbility().isEnabled(player)) {
            teleportLore.add("§a§l[SELECTED]");
        }
        teleportMeta.setLore(teleportLore);
        teleportItem.setItemMeta(teleportMeta);

        // Disable Item
        ItemStack disableItem = new ItemStack(Material.BARRIER);
        ItemMeta disableMeta = disableItem.getItemMeta();
        disableMeta.setDisplayName("§c§lDisable All");
        List<String> disableLore = new ArrayList<>();
        disableLore.add("§7Click to disable all abilities.");
        disableMeta.setLore(disableLore);
        disableItem.setItemMeta(disableMeta);

        // Place items
        gui.setItem(1, blinkItem);
        gui.setItem(3, blastItem);
        gui.setItem(5, teleportItem);
        gui.setItem(7, disableItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR)
            return;

        BlinkAbility blink = plugin.getBlinkAbility();
        BlastAbility blast = plugin.getBlastAbility();
        TeleportAbility teleport = plugin.getTeleportAbility();

        if (clickedItem.getType() == Material.FEATHER) {
            // Select Blink
            blast.setEnabled(player, false);
            teleport.setEnabled(player, false);
            if (!blink.isEnabled(player)) {
                blink.setEnabled(player, true);
                player.sendMessage("§b§l[Blink] §aAbility selected!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            player.closeInventory();

        } else if (clickedItem.getType() == Material.TNT_MINECART) {
            // Select Blast
            blink.setEnabled(player, false);
            teleport.setEnabled(player, false);
            if (!blast.isEnabled(player)) {
                blast.setEnabled(player, true);
                player.sendMessage("§e§l[Blast] §aAbility selected!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            player.closeInventory();

        } else if (clickedItem.getType() == Material.ENDER_PEARL) {
            // Select Teleport
            blink.setEnabled(player, false);
            blast.setEnabled(player, false);
            if (!teleport.isEnabled(player)) {
                teleport.setEnabled(player, true);
                player.sendMessage("§9§l[Teleport] §aAbility selected!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            player.closeInventory();

        } else if (clickedItem.getType() == Material.BARRIER) {
            // Disable All
            blink.setEnabled(player, false);
            blast.setEnabled(player, false);
            teleport.setEnabled(player, false);
            player.sendMessage("§c§lAll abilities disabled.");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.5f);
            player.closeInventory();
        }
    }
}
