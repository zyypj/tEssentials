package me.zypj.essentials.command;

import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.annotation.TabComplete;
import me.zypj.essentials.common.command.enums.SenderType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TpaCommand {

    private final EssentialsPlugin plugin;

    @Command(
            name = "tpa",
            sender = {SenderType.PLAYER}
    )
    public void onTpa(CommandSender sender, String[] args) {
        var msgs = plugin.getBootstrap().getMessagesAdapter();
        if (args.length < 1) {
            sender.sendMessage(msgs.getMessage("tpa.error-usage"));
            return;
        }

        var req = (Player) sender;
        var target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            req.sendMessage(msgs.getMessage("player-not-found"));
            return;
        }
        if (!plugin.getBootstrap().getTpaService().send(req, target)) {
            req.sendMessage(msgs.getMessage("tpa.already-pending"));
            return;
        }

        req.sendMessage(
                msgs.getMessage("tpa.request-sent")
                        .replace("{TARGET}", target.getName())
        );

        var template = msgs.getMessage("tpa.request-received")
                .replace("{PLAYER}", req.getName());
        var parts = template.split("\\{BUTTONS\\}", -1);
        var before = parts[0];
        var after = parts.length > 1 ? parts[1] : "";

        var order = msgs.getMessages("tpa.button-order");
        var sep = msgs.getMessage("tpa.button-separator");
        var components = new ArrayList<TextComponent>();

        components.add(new TextComponent(before));

        var first = true;
        for (var key : order) {
            if (!first) components.add(new TextComponent(sep));
            first = false;

            var label = msgs.getMessage("tpa.buttons." + key);
            var btn = new TextComponent(label);
            var cmd = key.equals("accept") ? "/tpaccept " + req.getName()
                    : "/tpadeny " + req.getName();
            btn.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    cmd
            ));
            components.add(btn);
        }

        if (!after.isEmpty()) {
            components.add(new TextComponent(after));
        }

        target.spigot().sendMessage(components.toArray(new TextComponent[0]));
    }

    @Command(
            name = "tpaccept",
            sender = {SenderType.PLAYER}
    )
    public void onTpAccept(CommandSender sender, String[] args) {
        var msgs = plugin.getBootstrap().getMessagesAdapter();
        if (!(sender instanceof Player target)) return;

        if (args.length < 1) {
            target.sendMessage(msgs.getMessage("tpa.error-usage-response"));
            return;
        }
        var requester = args[0];
        if (!plugin.getBootstrap().getTpaService().accept(target, requester)) {
            target.sendMessage(
                    msgs.getMessage("tpa.accept-not-found")
                            .replace("{PLAYER}", requester)
            );
        }
    }

    @Command(
            name = "tpadeny",
            sender = {SenderType.PLAYER}
    )
    public void onTpDeny(CommandSender sender, String[] args) {
        var msgs = plugin.getBootstrap().getMessagesAdapter();
        if (!(sender instanceof Player target)) return;

        if (args.length < 1) {
            target.sendMessage(msgs.getMessage("tpa.error-usage-response"));
            return;
        }
        var requester = args[0];
        if (!plugin.getBootstrap().getTpaService().deny(target, requester)) {
            target.sendMessage(
                    msgs.getMessage("tpa.accept-not-found")
                            .replace("{PLAYER}", requester)
            );
        }
    }

    @TabComplete(command = "tpa")
    public List<String> onTabCompleteTpa(CommandSender sender, String[] args) {
        return args.length == 1
                ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList()
                : List.of();
    }

    @TabComplete(command = "tpaccept")
    public List<String> onTabCompleteAccept(CommandSender sender, String[] args) {
        return args.length == 1
                ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList()
                : List.of();
    }

    @TabComplete(command = "tpadeny")
    public List<String> onTabCompleteDeny(CommandSender sender, String[] args) {
        return args.length == 1
                ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList()
                : List.of();
    }
}
