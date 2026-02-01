package com.uniqueskills.commands;

import com.uniqueskills.UniqueSkillsPlugin;
import com.uniqueskills.abilities.DashAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DashCommand implements CommandExecutor {

    private final DashAbility ability;
    private final UniqueSkillsPlugin plugin;

    public DashCommand(DashAbility ability, UniqueSkillsPlugin plugin) {
        this.ability = ability;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
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
            player.sendMessage("§6§l[Dash] §cアビリティを OFF にしました。");
        } else {
            plugin.disableAllAbilities(player);
            ability.setEnabled(player, true);
            player.sendMessage("§6§l[Dash] §aアビリティを ON にしました。");
            player.sendMessage("§7(ブレイズロッドを持って右クリックで走行モード切替)");
        }

        return true;
    }
}
