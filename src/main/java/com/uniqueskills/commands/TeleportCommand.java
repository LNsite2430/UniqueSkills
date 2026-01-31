package com.uniqueskills.commands;

import com.uniqueskills.UniqueSkillsPlugin;
import com.uniqueskills.abilities.TeleportAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand implements CommandExecutor {

    private final TeleportAbility ability;
    private final UniqueSkillsPlugin plugin;

    public TeleportCommand(TeleportAbility ability, UniqueSkillsPlugin plugin) {
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
            plugin.getBlastAbility().setEnabled(player, false);
            ability.setEnabled(player, true);
            player.sendMessage("§9§l[Teleport] §aTeleport ability §aON §7(右クリックでテレポート)");
        } else {
            ability.setEnabled(player, false);
            player.sendMessage("§9§l[Teleport] §cTeleport ability §cOFF");
        }

        return true;
    }
}
