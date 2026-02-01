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
    private final String GUI_TITLE = "§8スキル選択";

    public SkillSelectorGUI(UniqueSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Blink Item
        ItemStack blinkItem = new ItemStack(Material.FEATHER);
        ItemMeta blinkMeta = blinkItem.getItemMeta();
        blinkMeta.setDisplayName("§b§lブリンク (Blink)");
        List<String> blinkLore = new ArrayList<>();
        blinkLore.add("§7クリックで選択。");
        blinkLore.add("");
        blinkLore.add("§e§l必要なアイテム: §f§l羽§e§l (FEATHER)");
        blinkLore.add("§f操作: §6右クリック§fで前方にダッシュ");
        blinkLore.add("§7性能: §f1回分チャージ。着地後に再チャージされます。");
        blinkLore.add("");
        if (plugin.getBlinkAbility().isEnabled(player)) {
            blinkLore.add("§a§l[ 選択中 ]");
        }
        blinkMeta.setLore(blinkLore);
        blinkItem.setItemMeta(blinkMeta);

        // Blast Item
        ItemStack blastItem = new ItemStack(Material.TNT_MINECART);
        ItemMeta blastMeta = blastItem.getItemMeta();
        blastMeta.setDisplayName("§e§lブラストパック (Blast)");
        List<String> blastLore = new ArrayList<>();
        blastLore.add("§7クリックで選択。");
        blastLore.add("");
        blastLore.add("§e§l必要なアイテム: §c§lTNT付きトロッコ§e§l (TNT MINECART)");
        blastLore.add("§f操作: §6右クリック§fでパックを投げる");
        blastLore.add("§7性能: §f爆風で自分を飛ばしたり、敵をノックバックさせます。");
        blastLore.add("");
        if (plugin.getBlastAbility().isEnabled(player)) {
            blastLore.add("§a§l[ 選択中 ]");
        }
        blastMeta.setLore(blastLore);
        blastItem.setItemMeta(blastMeta);

        // Teleport Item
        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        teleportMeta.setDisplayName("§d§lテレポート (Teleport)");
        List<String> teleportLore = new ArrayList<>();
        teleportLore.add("§7クリックで選択。");
        teleportLore.add("");
        teleportLore.add("§e§l必要なアイテム: §5§lエンダーパール§e§l (ENDER PEARL)");
        teleportLore.add("§f基本操作:");
        teleportLore.add("§7  1. §6右クリック §7でビーコンを投げます。");
        teleportLore.add("§7  2. §6再度右クリック §7でビーコンの位置へテレポートします。");
        teleportLore.add("");
        teleportLore.add("§f高度な操作:");
        teleportLore.add("§7  - §fスニーク+右クリック §7: 足元にビーコンを設置。");
        teleportLore.add("§7  - §f(設置中)スニーク+右クリック §7: テレポートせずにビーコンを解除 (フェイク)。");
        if (plugin.getTeleportAbility().isEnabled(player)) {
            teleportLore.add("");
            teleportLore.add("§a§l[ 選択中 ]");
        }
        teleportMeta.setLore(teleportLore);
        teleportItem.setItemMeta(teleportMeta);

        // Dash Item
        ItemStack dashItem = new ItemStack(Material.BLAZE_ROD);
        ItemMeta dashMeta = dashItem.getItemMeta();
        dashMeta.setDisplayName("§6§lダッシュ (Dash)");
        List<String> dashLore = new ArrayList<>();
        dashLore.add("§7クリックで選択。");
        dashLore.add("");
        dashLore.add("§e§l必要なアイテム: §6§lブレイズロッド§e§l (BLAZE ROD)");
        dashLore.add("§f操作:");
        dashLore.add("§7  - §6右クリック §7: 移動モードのON/OFF切り替え。");
        dashLore.add("§7  - §fスニーク §7: 走行中に高速スライディング。");
        dashLore.add("");
        dashLore.add("§7性能: §fエネルギーを消費して高速移動。停止中は消費されません。");
        if (plugin.getDashAbility().isEnabled(player)) {
            dashLore.add("");
            dashLore.add("§a§l[ 選択中 ]");
        }
        dashMeta.setLore(dashLore);
        dashItem.setItemMeta(dashMeta);

        // Disable Item
        ItemStack disableItem = new ItemStack(Material.BARRIER);
        ItemMeta disableMeta = disableItem.getItemMeta();
        disableMeta.setDisplayName("§c§lアビリティを全て解除");
        List<String> disableLore = new ArrayList<>();
        disableLore.add("§7現在有効なアビリティをすべて無効化します。");
        disableMeta.setLore(disableLore);
        disableItem.setItemMeta(disableMeta);

        // Information Item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6§l Unique Skills ヘルプ");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7現在のバージョン: §e1.0.26");
        infoLore.add("");
        infoLore.add("§f§l[ 使いかた ]");
        infoLore.add("§71. 下のアイコンをクリックしてスキルを有効化。");
        infoLore.add("§72. 対応するアイテムを手に持って右クリックで発動。");
        infoLore.add("§73. 一度に使えるスキルは1つだけです。");
        infoLore.add("");
        infoLore.add("§7- チャットに §e/uskills help §7で詳細を表示できます。");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);

        // Place items
        gui.setItem(4, infoItem);
        gui.setItem(10, blinkItem);
        gui.setItem(12, blastItem);
        gui.setItem(14, teleportItem);
        gui.setItem(16, dashItem);
        gui.setItem(22, disableItem);

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
            boolean current = blink.isEnabled(player);
            plugin.disableAllAbilities(player);
            blink.setEnabled(player, !current);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            openGUI(player);
        } else if (clickedItem.getType() == Material.TNT_MINECART) {
            boolean current = blast.isEnabled(player);
            plugin.disableAllAbilities(player);
            blast.setEnabled(player, !current);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            openGUI(player);
        } else if (clickedItem.getType() == Material.ENDER_PEARL) {
            boolean current = teleport.isEnabled(player);
            plugin.disableAllAbilities(player);
            teleport.setEnabled(player, !current);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            openGUI(player);
        } else if (clickedItem.getType() == Material.BLAZE_ROD) {
            boolean current = dash.isEnabled(player);
            plugin.disableAllAbilities(player);
            dash.setEnabled(player, !current);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            openGUI(player);
        } else if (clickedItem.getType() == Material.BARRIER) {
            plugin.disableAllAbilities(player);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1f);
            openGUI(player);
        }
    }
}
