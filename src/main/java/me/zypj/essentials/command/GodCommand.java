package me.zypj.essentials.command;

import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.annotation.TabComplete;
import me.zypj.essentials.common.command.enums.SenderType;
import me.zypj.essentials.service.GodService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GodCommand {

    private final EssentialsPlugin plugin;
    private final GodService godService;

    public GodCommand(EssentialsPlugin plugin) {
        this.plugin = plugin;
        this.godService = plugin.getBootstrap().getGodService();
    }

    @Command(
            name = "god",
            permission = "god",
            sender = { SenderType.ALL }
    )
    public void onGod(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender && args.length < 1) {
            sender.sendMessage(plugin.getBootstrap()
                    .getMessagesAdapter().getMessage("god.error-usage"));
            return;
        }

        var target = args.length >= 1
                ? Bukkit.getPlayerExact(args[0])
                : sender instanceof Player p ? p : null;
        if (target == null) {
            sender.sendMessage(plugin.getBootstrap()
                    .getMessagesAdapter().getMessage("player-not-found"));
            return;
        }

        var enabled = godService.changeMode(target);
        var baseKey = enabled ? "god.self-enabled" : "god.self-disabled";
        target.sendMessage(plugin.getBootstrap()
                .getMessagesAdapter().getMessage(baseKey));

        if (!target.equals(sender)) {
            var otherKey = enabled ? "god.other-enabled" : "god.other-disabled";
            sender.sendMessage(plugin.getBootstrap()
                    .getMessagesAdapter().getMessage(otherKey)
                    .replace("{PLAYER}", target.getName()));
        }
    }

    @TabComplete(command = "god")
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 1
                ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList()
                : List.of();
    }
}
