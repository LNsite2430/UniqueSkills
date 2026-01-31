package com.uniqueskills.commands;

import com.uniqueskills.abilities.BlinkAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.uniqueskills.UniqueSkillsPlugin;

public class BlinkCommand implements CommandExecutor {

    private final BlinkAbility ability;
    private final UniqueSkillsPlugin plugin;

    public BlinkCommand(BlinkAbility ability, UniqueSkillsPlugin plugin) {
        this.ability = ability;
        this.plugin = plugin;
    }

    // 古いコンストラクタを一応残すか、Pluginクラス側で呼び出しを変える必要がある。
    // 今回はPluginクラス側も変えるので、ここは新しいシグネチャにする。

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行できます");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("valorantskills.use")) {
            player.sendMessage("§cこのコマンドを実行する権限がありません");
            return true;
        }

        boolean newState = !ability.isEnabled(player);

        if (newState) {
            // 他のアビリティを無効化
            plugin.getBlastAbility().setEnabled(player, false);
            ability.setEnabled(player, true);
            player.sendMessage("§b§l[Blink] §aDash ability §aON §7(右クリックで発動)");
        } else {
            ability.setEnabled(player, false);
            player.sendMessage("§b§l[Blink] §cDash ability §cOFF");
        }

        return true;
    }
}
