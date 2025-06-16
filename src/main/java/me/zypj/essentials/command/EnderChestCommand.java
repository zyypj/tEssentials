package me.zypj.essentials.command;

import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.api.command.annotation.Command;
import me.zypj.essentials.api.command.annotation.TabComplete;
import me.zypj.essentials.api.command.enums.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@RequiredArgsConstructor
public class EnderChestCommand {

    private final EssentialsPlugin plugin;

    @Command(
            name = "enderchest",
            permission = "enderchest",
            sender = {SenderType.ALL}
    )
    public void onEnderChest(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender && args.length < 1) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("enderchest.error-usage"));
            return;
        }

        Player target = args.length >= 1
                ? Bukkit.getPlayerExact(args[0])
                : sender instanceof Player p ? p : null;
        if (target == null) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("player-not-found"));
            return;
        }

        if (!(sender instanceof Player opener)) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("enderchest.error-console"));
            return;
        }

        opener.openInventory(target.getEnderChest());
    }

    @TabComplete(command = "enderchest")
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 1
                ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList()
                : List.of();
    }
}
