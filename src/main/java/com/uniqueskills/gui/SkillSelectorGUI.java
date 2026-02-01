package com.uniqueskills.gui;

import com.uniqueskills.UniqueSkillsPlugin;
import com.uniqueskills.abilities.BlinkAbility;
import com.uniqueskills.abilities.BlastAbility;
import com.uniqueskills.abilities.TeleportAbility;
import com.uniqueskills.abilities.DashAbility;
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

    @SuppressWarnings("deprecation")
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Blink Item
        ItemStack blinkItem = new ItemStack(Material.FEATHER);
        ItemMeta blinkMeta = blinkItem.getItemMeta();
        blinkMeta.setDisplayName("§b§lBlink");
        List<String> blinkLore = new ArrayList<>();
        blinkLore.add("§7Click to select Blink ability.");
        blinkLore.add("");
        blinkLore.add("§e[English]");
        blinkLore.add("§7Dash forward with Feather.");
        blinkLore.add("§72 charges, recharge when landing.");
        blinkLore.add("");
        blinkLore.add("§e[日本語]");
        blinkLore.add("§7羽を持って前方にダッシュ。");
        blinkLore.add("§72チャージ、着地で再チャージ。");
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
        blastLore.add("§7Throw explosive pack with TNT Minecart.");
        blastLore.add("§7Right-click again to detonate.");
        blastLore.add("");
        blastLore.add("§e[日本語]");
        blastLore.add("§7TNTトロッコで爆破パックを投げる。");
        blastLore.add("§7もう一度右クリックで起爆。");
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
        teleportLore.add("§7Place beacon with Ender Pearl, teleport to it.");
        teleportLore.add("§7Shift+Right-click to place on spot or fake teleport.");
        teleportLore.add("");
        teleportLore.add("§e[日本語]");
        teleportLore.add("§7エンダーパールでビーコンを設置、そこへテレポート。");
        teleportLore.add("§7Shift+右クリックでその場設置 or フェイクTP。");
        teleportLore.add("");
        if (plugin.getTeleportAbility().isEnabled(player)) {
            teleportLore.add("§a§l[SELECTED]");
        }
        teleportMeta.setLore(teleportLore);
        teleportItem.setItemMeta(teleportMeta);

        // Dash Item
        ItemStack dashItem = new ItemStack(Material.BLAZE_ROD);
        ItemMeta dashMeta = dashItem.getItemMeta();
        dashMeta.setDisplayName("§6§lDash");
        List<String> dashLore = new ArrayList<>();
        dashLore.add("§7Click to select Dash ability.");
        dashLore.add("");
        dashLore.add("§e[English]");
        dashLore.add("§7Right-click to toggle run (drains energy).");
        dashLore.add("§7Sneak while running to slide.");
        dashLore.add("");
        dashLore.add("§e[日本語]");
        dashLore.add("§7右クリックで移動モード切り替え（エネルギー消費）。");
        dashLore.add("§7走っている最中にスニークでスライド。");
        dashLore.add("");
        if (plugin.getDashAbility().isEnabled(player)) {
            dashLore.add("§a§l[SELECTED]");
        }
        dashMeta.setLore(dashLore);
        dashItem.setItemMeta(dashMeta);

        // Disable Item
        ItemStack disableItem = new ItemStack(Material.BARRIER);
        ItemMeta disableMeta = disableItem.getItemMeta();
        disableMeta.setDisplayName("§c§lDisable All");
        List<String> disableLore = new ArrayList<>();
        disableLore.add("§7Click to disable all abilities.");
        disableMeta.setLore(disableLore);
        disableItem.setItemMeta(disableMeta);

        // Place items in a symmetric layout (row 2)
        gui.setItem(10, blinkItem); // Left
        gui.setItem(12, blastItem); // Center-left
        gui.setItem(14, teleportItem); // Center-right
        gui.setItem(16, dashItem); // Right
        gui.setItem(22, disableItem); // Bottom center

        player.openInventory(gui);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
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
        DashAbility dash = plugin.getDashAbility();

        if (clickedItem.getType() == Material.FEATHER) {
            // Select Blink
            blast.cleanup(player.getUniqueId());
            teleport.cleanup(player.getUniqueId());
            dash.cleanup(player.getUniqueId());
            if (!blink.isEnabled(player)) {
                blink.cleanup(player.getUniqueId()); // clean state
                blink.setEnabled(player, true);
                player.sendMessage("§b§l[Blink] §aAbility selected!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            player.closeInventory();

        } else if (clickedItem.getType() == Material.TNT_MINECART) {
            // Select Blast
            blink.cleanup(player.getUniqueId());
            teleport.cleanup(player.getUniqueId());
            dash.cleanup(player.getUniqueId());
            if (!blast.isEnabled(player)) {
                blast.cleanup(player.getUniqueId()); // clean state
                blast.setEnabled(player, true);
                player.sendMessage("§e§l[Blast] §aAbility selected!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            player.closeInventory();

        } else if (clickedItem.getType() == Material.ENDER_PEARL) {
            // Select Teleport
            blink.cleanup(player.getUniqueId());
            blast.cleanup(player.getUniqueId());
            dash.cleanup(player.getUniqueId());
            if (!teleport.isEnabled(player)) {
                teleport.cleanup(player.getUniqueId()); // clean state
                teleport.setEnabled(player, true);
                player.sendMessage("§9§l[Teleport] §aAbility selected!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            player.closeInventory();

        } else if (clickedItem.getType() == Material.BLAZE_ROD) {
            // Select Dash
            blink.cleanup(player.getUniqueId());
            blast.cleanup(player.getUniqueId());
            teleport.cleanup(player.getUniqueId());
            if (!dash.isEnabled(player)) {
                dash.cleanup(player.getUniqueId()); // clean state
                dash.setEnabled(player, true);
                player.sendMessage("§6§l[Dash] §aAbility selected!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            player.closeInventory();

        } else if (clickedItem.getType() == Material.BARRIER) {
            // Disable All
            blink.cleanup(player.getUniqueId());
            blast.cleanup(player.getUniqueId());
            teleport.cleanup(player.getUniqueId());
            dash.cleanup(player.getUniqueId());
            player.sendMessage("§c§lAll abilities disabled.");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.5f);
            player.closeInventory();
        }
    }
}
