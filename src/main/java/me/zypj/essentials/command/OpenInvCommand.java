package me.zypj.essentials.command;

import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.annotation.TabComplete;
import me.zypj.essentials.common.command.enums.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@RequiredArgsConstructor
public class OpenInvCommand {

    private final EssentialsPlugin plugin;

    @Command(
            name = "openinv",
            permission = "open-inv",
            sender = { SenderType.ALL }
    )
    public void onOpenInv(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter().getMessage("openinv.error-usage"));
            return;
        }

        var target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("player-not-found"));
            return;
        }

        if (!(sender instanceof Player opener)) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("openinv.error-console"));
            return;
        }

        opener.openInventory(target.getInventory());
    }

    @TabComplete(command = "openinv")
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 1
                ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList()
                : List.of();
    }
}
