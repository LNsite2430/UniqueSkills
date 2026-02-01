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

        boolean current = ability.isEnabled(player);
        if (current) {
            ability.setEnabled(player, false);
            player.sendMessage("§9§l[Teleport] §cアビリティを OFF にしました。");
        } else {
            plugin.disableAllAbilities(player);
            ability.setEnabled(player, true);
            player.sendMessage("§9§l[Teleport] §aアビリティを ON にしました。");
            player.sendMessage("§7(エンダーパールを持って右クリックで挙動開始)");
        }

        return true;
    }
}
