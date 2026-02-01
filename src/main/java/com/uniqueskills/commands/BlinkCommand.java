package com.uniqueskills.commands;

import com.uniqueskills.UniqueSkillsPlugin;
import com.uniqueskills.abilities.BlinkAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlinkCommand implements CommandExecutor {

    private final BlinkAbility ability;
    private final UniqueSkillsPlugin plugin;

    public BlinkCommand(BlinkAbility ability, UniqueSkillsPlugin plugin) {
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

        boolean current = ability.isEnabled(player);
        if (current) {
            ability.setEnabled(player, false);
            player.sendMessage("§b§l[Blink] §cアビリティを OFF にしました。");
        } else {
            plugin.disableAllAbilities(player);
            ability.setEnabled(player, true);
            player.sendMessage("§b§l[Blink] §aアビリティを ON にしました。");
            player.sendMessage("§7(羽を持って右クリックでダッシュ)");
        }

        return true;
    }
}
