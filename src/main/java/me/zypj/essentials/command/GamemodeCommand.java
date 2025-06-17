package me.zypj.essentials.command;

import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.annotation.TabComplete;
import me.zypj.essentials.common.command.enums.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class GamemodeCommand {

    private final EssentialsPlugin plugin;

    @Command(
            name = "gamemode",
            permission = "gamemode",
            sender = {SenderType.ALL}
    )
    public void onGamemode(CommandSender sender, String[] args) {
        if (args.length < 1 || sender instanceof ConsoleCommandSender && args.length < 2) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("gamemode.error-usage"));
            return;
        }

        GameMode mode;
        try {
            mode = GameMode.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("gamemode.error-unknown-gamemode")
                    .replace("{MODE}", args[0]));
            return;
        }

        Player target = args.length >= 2
                ? Bukkit.getPlayerExact(args[1])
                : sender instanceof Player p ? p : null;
        if (target == null) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("player-not-found"));
            return;
        }

        target.setGameMode(mode);
        target.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                .getMessage("gamemode.self")
                .replace("{MODE}", mode.name().toLowerCase()));
        if (!target.equals(sender)) {
            sender.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("gamemode.other")
                    .replace("{PLAYER}", target.getName())
                    .replace("{MODE}", mode.name().toLowerCase()));
        }
    }

    @TabComplete(command = "gamemode")
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 1 -> Arrays.stream(GameMode.values())
                    .map(g -> g.name().toLowerCase())
                    .filter(m -> m.startsWith(args[0].toLowerCase()))
                    .toList();
            case 2 -> Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
            default -> List.of();
        };
    }
}
