package com.uniqueskills.commands;

import com.uniqueskills.gui.SkillSelectorGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillCommand implements CommandExecutor {

    private final SkillSelectorGUI gui;

    public SkillCommand(SkillSelectorGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行できます");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage("§f ");
            player.sendMessage("§b§l§m-----§b§l  Unique Skills: ユーザーガイド  §b§l§m-----");
            player.sendMessage("§f ");
            player.sendMessage("§e§l1. スキルの選択方法");
            player.sendMessage("§7§e /uskills §7(または /ds) を入力し、GUIからアイコンをクリックしてください。");
            player.sendMessage("§7一度に有効にできるアビリティは §b1つだけ §7です。");
            player.sendMessage("§f ");
            player.sendMessage("§e§l2. 使い方 (発動アイテム)");
            player.sendMessage("§7スキルを選んだら、対応するアイテムを持って §6右クリック §7で発動します:");
            player.sendMessage("§f - §bブリンク§7: §f羽 §7を持って右クリック");
            player.sendMessage("§f - §eブラストパック§7: §cTNT付きトロッコ §7を持って右クリック");
            player.sendMessage("§f - §dテレポート§7: §5エンダーパール §7を持って右クリック");
            player.sendMessage("§f - §6ダッシュ§7: §6ブレイズロッド §7を持って右クリック (ON/OFF切替)");
            player.sendMessage("§f     §7* ダッシュ中にスニークするとスライディングが発動します。");
            player.sendMessage("§f ");
            player.sendMessage("§e§l3. 便利な機能");
            player.sendMessage("§f - §e/uskills §7: 選択GUIを開く");
            player.sendMessage("§f - §e/ds §7: メニューの略称");
            player.sendMessage("§f - §e/[スキル名] §7: スキルをコマンドで直接ON/OFF");
            player.sendMessage("§b§l§m---------------------------------------");
            return true;
        }

        gui.openGUI(player);
        return true;
    }
}
