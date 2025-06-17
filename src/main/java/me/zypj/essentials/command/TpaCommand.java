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
        var service = plugin.getBootstrap().getTpaService();

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
        if (!service.send(req, target)) {
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
        var comps = new ArrayList<TextComponent>();

        comps.add(new TextComponent(before));

        var first = true;
        for (var key : order) {
            if (!first) comps.add(new TextComponent(sep));
            first = false;

            var label = msgs.getMessage("tpa.buttons." + key);
            var btn = new TextComponent(label);
            var cmd = key.equals("accept")
                    ? "/tpaccept " + req.getName()
                    : "/tpadeny " + req.getName();
            btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
            comps.add(btn);
        }

        if (!after.isEmpty()) {
            comps.add(new TextComponent(after));
        }

        target.spigot().sendMessage(comps.toArray(new TextComponent[0]));
    }

    @Command(
            name = "tpaccept",
            sender = {SenderType.PLAYER}
    )
    public void onTpAccept(CommandSender sender, String[] args) {
        var msgs = plugin.getBootstrap().getMessagesAdapter();
        var service = plugin.getBootstrap().getTpaService();
        var target = (Player) sender;

        if (args.length < 1) {
            target.sendMessage(msgs.getMessage("tpa.error-usage-response"));
            return;
        }
        var reqName = args[0];
        if (service.accept(target, reqName)) {
            target.sendMessage(
                    msgs.getMessage("tpa.accepted.target")
                            .replace("{PLAYER}", reqName)
            );
            var req = Bukkit.getPlayerExact(reqName);
            if (req != null) {
                req.sendMessage(
                        msgs.getMessage("tpa.accepted.sender")
                                .replace("{TARGET}", target.getName())
                );
            }
        } else {
            target.sendMessage(
                    msgs.getMessage("tpa.accept-not-found")
                            .replace("{PLAYER}", reqName)
            );
        }
    }

    @Command(
            name = "tpadeny",
            sender = {SenderType.PLAYER}
    )
    public void onTpDeny(CommandSender sender, String[] args) {
        var msgs = plugin.getBootstrap().getMessagesAdapter();
        var service = plugin.getBootstrap().getTpaService();
        var target = (Player) sender;

        if (args.length < 1) {
            target.sendMessage(msgs.getMessage("tpa.error-usage-response"));
            return;
        }
        var reqName = args[0];
        if (service.deny(target, reqName)) {
            target.sendMessage(
                    msgs.getMessage("tpa.denied.target")
                            .replace("{PLAYER}", reqName)
            );
            var req = Bukkit.getPlayerExact(reqName);
            if (req != null) {
                req.sendMessage(
                        msgs.getMessage("tpa.denied.sender")
                                .replace("{TARGET}", target.getName())
                );
            }
        } else {
            target.sendMessage(
                    msgs.getMessage("tpa.accept-not-found")
                            .replace("{PLAYER}", reqName)
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
