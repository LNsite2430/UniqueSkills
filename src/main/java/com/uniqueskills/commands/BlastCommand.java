package com.uniqueskills.commands;

import com.uniqueskills.UniqueSkillsPlugin;
import com.uniqueskills.abilities.BlastAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlastCommand implements CommandExecutor {

    private final BlastAbility ability;
    private final UniqueSkillsPlugin plugin;

    public BlastCommand(BlastAbility ability, UniqueSkillsPlugin plugin) {
        this.ability = ability;
        this.plugin = plugin;
    }

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
            plugin.getBlinkAbility().setEnabled(player, false);
            ability.setEnabled(player, true);
            player.sendMessage("§e§l[Blast] §aBlast ability §aON §7(右クリックで投げる、再度右クリックで起爆)");
        } else {
            ability.setEnabled(player, false);
            player.sendMessage("§e§l[Blast] §cBlast ability §cOFF");
        }

        return true;
    }
}
