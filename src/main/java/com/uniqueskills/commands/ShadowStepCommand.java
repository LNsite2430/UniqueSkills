package com.uniqueskills.commands;

import com.uniqueskills.abilities.ShadowStepAbility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShadowStepCommand implements CommandExecutor {

    private final ShadowStepAbility ability;

    public ShadowStepCommand(ShadowStepAbility ability) {
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
            player.sendMessage("§5[Shadow Step] §cDisabled.");
        } else {
            ability.setEnabled(player, true);
            player.sendMessage("§5[Shadow Step] §aEnabled!");
        }

        return true;
    }
}
