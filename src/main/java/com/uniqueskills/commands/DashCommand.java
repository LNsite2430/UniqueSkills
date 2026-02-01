package com.uniqueskills.commands;

import com.uniqueskills.abilities.DashAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DashCommand implements CommandExecutor {

    private final DashAbility ability;

    public DashCommand(DashAbility ability) {
        this.ability = ability;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }

        Player player = (Player) sender;

        if (ability.isEnabled(player)) {
            ability.setEnabled(player, false);
            player.sendMessage("§e[Dash] §cDisabled.");
        } else {
            ability.setEnabled(player, true);
            player.sendMessage("§6[Dash] §aEnabled! Right-click to toggle run, sneak+click to slide!");
        }

        return true;
    }
}
